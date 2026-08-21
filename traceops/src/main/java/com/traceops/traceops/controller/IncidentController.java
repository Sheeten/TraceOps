package com.traceops.traceops.controller;

import com.traceops.traceops.model.*;
import com.traceops.traceops.service.IncidentService;
import com.traceops.traceops.service.ProjectCodeAnalyzerService;
import com.traceops.traceops.service.ProjectImpactAnalyzerService;
import com.traceops.traceops.service.ProjectIncidentAnalyzerService;
import com.traceops.traceops.service.ProjectRcaAnalyzerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://traceops-xis6.onrender.com/api/projects"
})
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    private final ProjectCodeAnalyzerService
            projectCodeAnalyzerService;

    private final ProjectIncidentAnalyzerService
            projectIncidentAnalyzerService;

    private final ProjectImpactAnalyzerService
            projectImpactAnalyzerService;

    private final ProjectRcaAnalyzerService
            projectRcaAnalyzerService;

    public IncidentController(
            IncidentService incidentService,
            ProjectCodeAnalyzerService projectCodeAnalyzerService,
            ProjectIncidentAnalyzerService projectIncidentAnalyzerService,
            ProjectImpactAnalyzerService projectImpactAnalyzerService,
            ProjectRcaAnalyzerService projectRcaAnalyzerService) {

        this.incidentService =
                incidentService;

        this.projectCodeAnalyzerService =
                projectCodeAnalyzerService;

        this.projectIncidentAnalyzerService =
                projectIncidentAnalyzerService;

        this.projectImpactAnalyzerService =
                projectImpactAnalyzerService;

        this.projectRcaAnalyzerService =
                projectRcaAnalyzerService;
    }

    @GetMapping("/{incidentId}")
    public List<IncidentDetails> getIncident(
            @PathVariable String incidentId) {

        return incidentService
                .getIncidentDetails(incidentId);
    }

    @GetMapping("/{incidentId}/impact")
    public List<String> getImpact(
            @PathVariable String incidentId) {

        return incidentService
                .getImpactedServices(incidentId);
    }

    @GetMapping("/{incidentId}/rca")
    public List<IncidentDetails> getRca(
            @PathVariable String incidentId) {

        return incidentService
                .getRca(incidentId);
    }

    @GetMapping("/{incidentId}/graph")
    public List<DependencyEdge> getDependencyGraph(
            @PathVariable String incidentId) {

        return incidentService
                .getDependencyGraph(incidentId);
    }

    @PostMapping("/projects")
    public String createProject(
            @RequestBody ProjectRequest request) {

        incidentService.createProject(request);

        return "Project created successfully";
    }

    @GetMapping("/projects")
    public List<String> getProjects() {

        return incidentService
                .getProjects();
    }

    @GetMapping(
            "/projects/{projectName}/{moduleName}/code-graph"
    )
    public List<CodeDependency> getCodeGraph(
            @PathVariable String projectName,
            @PathVariable String moduleName) {

        return projectCodeAnalyzerService
                .analyzeProject(
                        projectName,
                        moduleName
                );
    }

    @GetMapping
    public List<IncidentSummary> getAllIncidents() {

        return incidentService
                .getAllIncidents();
    }

    @GetMapping(
            "/projects/{projectName}/incidents"
    )
    public List<DetectedIncident> getProjectIncidents(
            @PathVariable String projectName) {

        return projectIncidentAnalyzerService
                .analyzeProject(projectName);
    }

    @GetMapping(
            "/projects/{projectName}/incidents/{className}/impact"
    )
    public ProjectImpact analyzeImpact(
            @PathVariable String projectName,
            @PathVariable String className,
            @RequestParam String methodName) {

        return projectImpactAnalyzerService
                .analyze(
                        projectName,
                        className,
                        methodName
                );
    }

    @GetMapping(
            "/projects/{projectName}/incidents/{className}/rca"
    )
    public RootCauseAnalysis analyzeRca(
            @PathVariable String projectName,
            @PathVariable String className,
            @RequestParam String methodName) {

        List<DetectedIncident> incidents =
                projectIncidentAnalyzerService
                        .analyzeProject(projectName);

        for (DetectedIncident incident : incidents) {

            if (className.equals(
                    incident.getClassName()
            )
                    && methodName.equals(
                    incident.getMethodName()
            )) {

                return projectRcaAnalyzerService
                        .analyze(incident);
            }
        }

        throw new RuntimeException(
                "Incident not found for "
                        + className
                        + "."
                        + methodName
        );
    }
}