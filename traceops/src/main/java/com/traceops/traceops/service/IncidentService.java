package com.traceops.traceops.service;

import com.traceops.traceops.model.DependencyEdge;
import com.traceops.traceops.model.IncidentDetails;
import com.traceops.traceops.model.IncidentSummary;
import com.traceops.traceops.model.ProjectRequest;
import com.traceops.traceops.repo.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(
            IncidentRepository incidentRepository) {

        this.incidentRepository =
                incidentRepository;
    }

    public List<IncidentDetails> getIncidentDetails(
            String incidentId) {

        return incidentRepository
                .getIncidentDetails(incidentId);
    }

    public List<String> getImpactedServices(
            String incidentId) {

        return incidentRepository
                .findImpactedServices(incidentId);
    }

    public List<IncidentDetails> getRca(
            String incidentId) {

        return incidentRepository
                .getRca(incidentId);
    }

    public List<DependencyEdge> getDependencyGraph(
            String incidentId) {

        return incidentRepository
                .findDependencyGraph(incidentId);
    }

    public void createProject(
            ProjectRequest request) {

        incidentRepository
                .createProject(request);
    }

    public List<String> getProjects() {

        return incidentRepository
                .getProjects();
    }

    public List<IncidentSummary> getAllIncidents() {

        return incidentRepository
                .getAllIncidents();
    }

    public void createIncident(
            String incidentId,
            String title,
            String service,
            String error,
            String cause,
            String solution,
            String database) {

        incidentRepository.createIncident(
                incidentId,
                title,
                service,
                error,
                cause,
                solution,
                database
        );
    }
}