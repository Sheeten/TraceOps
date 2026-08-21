package com.traceops.traceops.service;

import com.traceops.traceops.model.DependencyEdge;
import com.traceops.traceops.repo.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphService {

    private final IncidentRepository incidentRepository;

    public GraphService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<DependencyEdge> getDependencies(
            String project,
            String module,
            String className) {

        return incidentRepository.findCodeDependencyGraph(
                project,
                module,
                className
        );
    }

    public List<String> getDependents(
            String project,
            String module,
            String className) {

        return incidentRepository.findDependentClasses(
                project,
                module,
                className
        );
    }

    public List<DependencyEdge> getCompleteDependencyPaths(
            String project,
            String module,
            String className) {

        return incidentRepository.findCompleteDependencyPaths(
                project,
                module,
                className
        );
    }
}
