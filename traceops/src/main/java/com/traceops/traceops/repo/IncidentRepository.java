package com.traceops.traceops.repo;

import com.traceops.traceops.model.*;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class IncidentRepository {

    private final Neo4jClient neo4jClient;

    public IncidentRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public List<IncidentDetails> getIncidentDetails(String incidentId) {

        String query = """
                MATCH (incident:Incident {id: $incidentId})
                OPTIONAL MATCH (incident)-[:AFFECTS]->(service:Service)
                OPTIONAL MATCH (incident)-[:HAS_ERROR]->(error:Error)
                OPTIONAL MATCH (error)-[:HAS_CAUSE]->(cause:Cause)
                OPTIONAL MATCH (cause)-[:HAS_SOLUTION]->(solution:Solution)
                OPTIONAL MATCH (solution)-[:APPLIES_TO]->(database:Database)
                RETURN incident.title AS incident,
                       service.name AS service,
                       error.code AS error,
                       cause.name AS cause,
                       solution.name AS solution,
                       database.name AS database
                """;

        return neo4jClient.query(query)
                .bind(incidentId)
                .to("incidentId")
                .fetch()
                .all()
                .stream()
                .map(row -> new IncidentDetails(
                        (String) row.get("incident"),
                        (String) row.get("service"),
                        (String) row.get("error"),
                        (String) row.get("cause"),
                        (String) row.get("solution"),
                        (String) row.get("database")
                ))
                .toList();
    }

    public List<String> findImpactedServices(String incidentId) {

        String query = """
                MATCH (incident:Incident {id: $incidentId})
                -[:AFFECTS]->
                (service:Service)
                MATCH (service)-[:DEPENDS_ON*0..3]->
                (affected:Service)
                RETURN DISTINCT affected.name AS serviceName
                """;

        return neo4jClient.query(query)
                .bind(incidentId)
                .to("incidentId")
                .fetch()
                .all()
                .stream()
                .map(row -> (String) row.get("serviceName"))
                .toList();
    }

    public List<IncidentDetails> getRca(String incidentId) {

        String query = """
                MATCH (incident:Incident {id: $incidentId})
                OPTIONAL MATCH (incident)-[:HAS_ERROR]->(error:Error)
                OPTIONAL MATCH (error)-[:HAS_CAUSE]->(cause:Cause)
                OPTIONAL MATCH (cause)-[:HAS_SOLUTION]->(solution:Solution)
                OPTIONAL MATCH (solution)-[:APPLIES_TO]->(database:Database)
                RETURN incident.title AS incident,
                       error.code AS error,
                       cause.name AS cause,
                       solution.name AS solution,
                       database.name AS database
                """;

        return neo4jClient.query(query)
                .bind(incidentId)
                .to("incidentId")
                .fetch()
                .all()
                .stream()
                .map(row -> new IncidentDetails(
                        (String) row.get("incident"),
                        null,
                        (String) row.get("error"),
                        (String) row.get("cause"),
                        (String) row.get("solution"),
                        (String) row.get("database")
                ))
                .toList();
    }

    public List<DependencyEdge> findDependencyGraph(String incidentId) {

        String query = """
                MATCH (incident:Incident {id: $incidentId})
                -[:AFFECTS]->
                (service:Service)
                MATCH path =
                    (service)-[:DEPENDS_ON*1..3]->
                    (dependency:Service)
                UNWIND relationships(path) AS rel
                RETURN DISTINCT
                       startNode(rel).name AS source,
                       endNode(rel).name AS target,
                       type(rel) AS relationship
                """;

        return neo4jClient.query(query)
                .bind(incidentId)
                .to("incidentId")
                .fetch()
                .all()
                .stream()
                .map(row -> new DependencyEdge(
                        (String) row.get("source"),
                        (String) row.get("target"),
                        (String) row.get("relationship")
                ))
                .toList();
    }

    public void createProject(ProjectRequest request) {

        String query = """
                MERGE (project:Project {
                    name: $project
                })

                WITH project, $services AS services

                UNWIND services AS serviceData

                MERGE (service:Service {
                    name: serviceData.name
                })

                MERGE (project)-[:HAS_SERVICE]->(service)

                WITH service, serviceData

                UNWIND serviceData.dependsOn AS dependencyName

                MERGE (dependency:Service {
                    name: dependencyName
                })

                MERGE (service)-[:DEPENDS_ON]->(dependency)
                """;

        List<Map<String, Object>> services =
                request.getServices()
                        .stream()
                        .map(service ->
                                Map.of(
                                        "name", service.getName(),
                                        "dependsOn", service.getDependsOn()
                                )
                        )
                        .toList();

        neo4jClient.query(query)
                .bind(request.getProject())
                .to("project")
                .bind(services)
                .to("services")
                .run();
    }

    public List<String> getProjects() {

        String query = """
                MATCH (project:Project)
                RETURN project.name AS project
                ORDER BY project.name
                """;

        return neo4jClient.query(query)
                .fetch()
                .all()
                .stream()
                .map(row -> (String) row.get("project"))
                .toList();
    }

    public List<IncidentSummary> getAllIncidents() {

        String query = """
                MATCH (incident:Incident)
                RETURN incident.id AS id,
                       incident.title AS title
                ORDER BY incident.id
                """;

        return neo4jClient.query(query)
                .fetch()
                .all()
                .stream()
                .map(row -> new IncidentSummary(
                        (String) row.get("id"),
                        (String) row.get("title")
                ))
                .toList();
    }

    public void createIncident(
            String incidentId,
            String title,
            String service,
            String error,
            String cause,
            String solution,
            String database) {

        String query = """
                MERGE (incident:Incident {
                    id: $incidentId
                })

                SET incident.title = $title

                MERGE (service:Service {
                    name: $service
                })

                MERGE (error:Error {
                    code: $error
                })

                MERGE (cause:Cause {
                    name: $cause
                })

                MERGE (solution:Solution {
                    name: $solution
                })

                MERGE (database:Database {
                    name: $database
                })

                MERGE (incident)-[:AFFECTS]->(service)
                MERGE (incident)-[:HAS_ERROR]->(error)
                MERGE (error)-[:HAS_CAUSE]->(cause)
                MERGE (cause)-[:HAS_SOLUTION]->(solution)
                MERGE (solution)-[:APPLIES_TO]->(database)
                """;

        neo4jClient.query(query)
                .bind(incidentId)
                .to("incidentId")
                .bind(title)
                .to("title")
                .bind(service)
                .to("service")
                .bind(error)
                .to("error")
                .bind(cause)
                .to("cause")
                .bind(solution)
                .to("solution")
                .bind(database)
                .to("database")
                .run();
    }

    /*
     * Save the code dependency graph into CognoDB.
     *
     * Each Java class becomes a CodeClass node.
     *
     * Example:
     *
     * BookController
     *       |
     *       | CODE_DEPENDS_ON
     *       ↓
     * BookService
     *
     * The project and module are stored on every
     * CodeClass node so classes from different
     * projects do not get mixed together.
     */
    public void saveCodeDependencies(
            String projectName,
            String moduleName,
            List<CodeDependency> dependencies) {

        /*
         * Remove the old code graph for this
         * particular project/module.
         *
         * This prevents stale classes and
         * relationships from previous uploads.
         */
        String deleteQuery = """
                MATCH (c:CodeClass)
                WHERE c.project = $project
                  AND c.module = $module
                DETACH DELETE c
                """;

        neo4jClient.query(deleteQuery)
                .bind(projectName)
                .to("project")
                .bind(moduleName)
                .to("module")
                .run();

        /*
         * Convert CodeDependency objects into
         * simple maps that Neo4j can receive.
         */
        List<Map<String, Object>> dependencyData =
                dependencies.stream()
                        .map(dependency ->
                                Map.<String, Object>of(
                                        "source",
                                        dependency.getSource(),
                                        "target",
                                        dependency.getTarget(),
                                        "relationship",
                                        dependency.getRelationship()
                                )
                        )
                        .toList();

        /*
         * Create CodeClass nodes and relationships.
         */
        String saveQuery = """
                UNWIND $dependencies AS dependency

                MERGE (source:CodeClass {
                    name: dependency.source,
                    project: $project,
                    module: $module
                })

                MERGE (target:CodeClass {
                    name: dependency.target,
                    project: $project,
                    module: $module
                })

                MERGE (source)-[rel:CODE_DEPENDS_ON {
                    relationship: dependency.relationship
                }]->(target)

                RETURN count(rel) AS relationshipsCreated
                """;

        neo4jClient.query(saveQuery)
                .bind(dependencyData)
                .to("dependencies")
                .bind(projectName)
                .to("project")
                .bind(moduleName)
                .to("module")
                .run();

        System.out.println(
                "Saved "
                        + dependencies.size()
                        + " code dependencies for "
                        + projectName
                        + "/"
                        + moduleName
        );
    }

    public List<DependencyEdge> findCodeDependencyGraph(
            String projectName,
            String moduleName,
            String className) {

        String query = """
            MATCH (source:CodeClass {
                project: $project,
                module: $module,
                name: $className
            })

            MATCH path =
                (source)-[:CODE_DEPENDS_ON*1..3]->
                (target:CodeClass {
                    project: $project,
                    module: $module
                })

            UNWIND relationships(path) AS rel

            RETURN DISTINCT
                   startNode(rel).name AS source,
                   endNode(rel).name AS target,
                   rel.relationship AS relationship
            """;

        return neo4jClient.query(query)
                .bind(projectName)
                .to("project")
                .bind(moduleName)
                .to("module")
                .bind(className)
                .to("className")
                .fetch()
                .all()
                .stream()
                .map(row -> new DependencyEdge(
                        (String) row.get("source"),
                        (String) row.get("target"),
                        (String) row.get("relationship")
                ))
                .toList();
    }

    public List<String> findDependentClasses(
            String projectName,
            String moduleName,
            String className) {

        String query = """
            MATCH (target:CodeClass {
                project: $project,
                module: $module,
                name: $className
            })

            MATCH (dependent:CodeClass {
                project: $project,
                module: $module
            })
            -[:CODE_DEPENDS_ON*1..3]->
            (target)

            RETURN DISTINCT dependent.name AS className
            ORDER BY className
            """;

        return neo4jClient.query(query)
                .bind(projectName)
                .to("project")
                .bind(moduleName)
                .to("module")
                .bind(className)
                .to("className")
                .fetch()
                .all()
                .stream()
                .map(row -> (String) row.get("className"))
                .toList();
    }

    public List<DependencyEdge> findCompleteDependencyPaths(
            String projectName,
            String moduleName,
            String className) {

        String query = """
            MATCH path =
                (start:CodeClass {
                    project: $project,
                    module: $module,
                    name: $className
                })
                -[:CODE_DEPENDS_ON*1..3]->
                (end:CodeClass {
                    project: $project,
                    module: $module
                })

            UNWIND relationships(path) AS rel

            RETURN DISTINCT
                   startNode(rel).name AS source,
                   endNode(rel).name AS target,
                   rel.relationship AS relationship
            ORDER BY source, target
            """;

        return neo4jClient.query(query)
                .bind(projectName)
                .to("project")
                .bind(moduleName)
                .to("module")
                .bind(className)
                .to("className")
                .fetch()
                .all()
                .stream()
                .map(row -> new DependencyEdge(
                        (String) row.get("source"),
                        (String) row.get("target"),
                        (String) row.get("relationship")
                ))
                .toList();
    }
}