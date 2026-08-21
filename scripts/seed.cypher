// ============================================================
// TraceOps - CognoDB Seed Data
// ============================================================


// ============================================================
// 1. PROJECT + SERVICE DEPENDENCY GRAPH
// ============================================================

MERGE (project:Project {
    name: 'PatientManagementSystem'
})

MERGE (patientController:Service {
    name: 'PatientController'
})

MERGE (patientService:Service {
    name: 'PatientService'
})

MERGE (patientRepository:Service {
    name: 'PatientRepository'
})

MERGE (databaseService:Service {
    name: 'PatientDatabaseService'
})

MERGE (project)-[:HAS_SERVICE]->(patientController)
MERGE (project)-[:HAS_SERVICE]->(patientService)
MERGE (project)-[:HAS_SERVICE]->(patientRepository)
MERGE (project)-[:HAS_SERVICE]->(databaseService)

MERGE (patientController)-[:DEPENDS_ON]->(patientService)
MERGE (patientService)-[:DEPENDS_ON]->(patientRepository)
MERGE (patientRepository)-[:DEPENDS_ON]->(databaseService);


// ============================================================
// 2. CODE CLASS DEPENDENCY GRAPH
// ============================================================

MERGE (controller:CodeClass {
    name: 'PatientController',
    project: 'PatientManagementSystem',
    module: 'PatientManagementSystem'
})

MERGE (service:CodeClass {
    name: 'PatientService',
    project: 'PatientManagementSystem',
    module: 'PatientManagementSystem'
})

MERGE (serviceImpl:CodeClass {
    name: 'PatientServiceImpl',
    project: 'PatientManagementSystem',
    module: 'PatientManagementSystem'
})

MERGE (repository:CodeClass {
    name: 'PatientRepository',
    project: 'PatientManagementSystem',
    module: 'PatientManagementSystem'
})

MERGE (model:CodeClass {
    name: 'Patient',
    project: 'PatientManagementSystem',
    module: 'PatientManagementSystem'
})

MERGE (controller)-[:CODE_DEPENDS_ON {
    relationship: 'USES'
}]->(service)

MERGE (service)-[:CODE_DEPENDS_ON {
    relationship: 'USES'
}]->(serviceImpl)

MERGE (serviceImpl)-[:CODE_DEPENDS_ON {
    relationship: 'USES'
}]->(repository)

MERGE (repository)-[:CODE_DEPENDS_ON {
    relationship: 'USES'
}]->(model);


// ============================================================
// 3. SECOND PROJECT - SAMSTRACK API
// ============================================================

MERGE (samsProject:Project {
    name: 'SAMSTRACK_API'
})

MERGE (attendanceService:Service {
    name: 'AttendanceService'
})

MERGE (studentService:Service {
    name: 'StudentService'
})

MERGE (userService:Service {
    name: 'UserService'
})

MERGE (samsProject)-[:HAS_SERVICE]->(attendanceService)
MERGE (samsProject)-[:HAS_SERVICE]->(studentService)
MERGE (samsProject)-[:HAS_SERVICE]->(userService)

MERGE (attendanceService)-[:DEPENDS_ON]->(studentService)
MERGE (studentService)-[:DEPENDS_ON]->(userService);


// ============================================================
// 4. INCIDENT + RCA GRAPH
// ============================================================

MERGE (incident:Incident {
    id: 'INC-001'
})

SET incident.title =
    'Patient lookup failed because requested patient was not found'

MERGE (incidentService:Service {
    name: 'PatientService'
})

MERGE (error:Error {
    code: 'PATIENT_NOT_FOUND'
})

MERGE (cause:Cause {
    name: 'Requested patient does not exist'
})

MERGE (solution:Solution {
    name: 'Return HTTP 404 when patient is not found'
})

MERGE (database:Database {
    name: 'PatientDB'
})

MERGE (incident)-[:AFFECTS]->(incidentService)
MERGE (incident)-[:HAS_ERROR]->(error)
MERGE (error)-[:HAS_CAUSE]->(cause)
MERGE (cause)-[:HAS_SOLUTION]->(solution)
MERGE (solution)-[:APPLIES_TO]->(database);


// ============================================================
// 5. SECOND INCIDENT
// ============================================================

MERGE (incident2:Incident {
    id: 'INC-002'
})

SET incident2.title =
    'Database connection timeout'

MERGE (databaseService2:Service {
    name: 'PatientDatabaseService'
})

MERGE (error2:Error {
    code: 'DB_TIMEOUT'
})

MERGE (cause2:Cause {
    name: 'Database operation exceeded configured timeout'
})

MERGE (solution2:Solution {
    name: 'Review database timeout and connection configuration'
})

MERGE (database2:Database {
    name: 'PatientDB'
})

MERGE (incident2)-[:AFFECTS]->(databaseService2)
MERGE (incident2)-[:HAS_ERROR]->(error2)
MERGE (error2)-[:HAS_CAUSE]->(cause2)
MERGE (cause2)-[:HAS_SOLUTION]->(solution2)
MERGE (solution2)-[:APPLIES_TO]->(database2);


// ============================================================
// 6. REQUIRED MULTI-HOP QUERY
// ============================================================
//
// Finds dependencies up to 3 hops away from PatientController.
//

MATCH path =
    (start:CodeClass {
        project: 'PatientManagementSystem',
        module: 'PatientManagementSystem',
        name: 'PatientController'
    })
    -[:CODE_DEPENDS_ON*1..3]->
    (target:CodeClass {
        project: 'PatientManagementSystem',
        module: 'PatientManagementSystem'
    })

RETURN
    [node IN nodes(path) | node.name] AS dependencyPath;


// ============================================================
// 7. DEPENDENCY IMPACT QUERY
// ============================================================
//
// Finds classes that depend on PatientRepository
// within three graph hops.
//

MATCH path =
    (dependent:CodeClass {
        project: 'PatientManagementSystem',
        module: 'PatientManagementSystem'
    })
    -[:CODE_DEPENDS_ON*1..3]->
    (target:CodeClass {
        project: 'PatientManagementSystem',
        module: 'PatientManagementSystem',
        name: 'PatientRepository'
    })

RETURN DISTINCT
    dependent.name AS dependentClass;


// ============================================================
// 8. INCIDENT IMPACT QUERY
// ============================================================
//
// Finds services affected by an incident and their
// dependency chain up to three hops.
//

MATCH (incident:Incident {
    id: 'INC-001'
})
-[:AFFECTS]->
(service:Service)
-[:DEPENDS_ON*0..3]->
(affected:Service)

RETURN DISTINCT
    incident.id AS incident,
    affected.name AS affectedService;


// ============================================================
// 9. INCIDENT RCA QUERY
// ============================================================

MATCH (incident:Incident {
    id: 'INC-001'
})
-[:HAS_ERROR]->
(error:Error)
-[:HAS_CAUSE]->
(cause:Cause)
-[:HAS_SOLUTION]->
(solution:Solution)
-[:APPLIES_TO]->
(database:Database)

RETURN
    incident.title AS incident,
    error.code AS error,
    cause.name AS cause,
    solution.name AS solution,
    database.name AS database;


// ============================================================
// 10. GRAPH OVERVIEW QUERY
// ============================================================

MATCH (n)
RETURN
    labels(n) AS nodeType,
    count(n) AS total
ORDER BY nodeType;