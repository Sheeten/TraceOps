package com.traceops.traceops.controller;

import com.traceops.traceops.model.DependencyEdge;
import com.traceops.traceops.service.GraphService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://traceops-xis6.onrender.com/api/projects"
})
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/dependencies")
    public List<DependencyEdge> getDependencies(
            @RequestParam String project,
            @RequestParam String module,
            @RequestParam String className) {

        return graphService.getDependencies(
                project,
                module,
                className
        );
    }

    @GetMapping("/dependents")
    public List<String> getDependents(
            @RequestParam String project,
            @RequestParam String module,
            @RequestParam String className) {

        return graphService.getDependents(
                project,
                module,
                className
        );
    }

    @GetMapping("/complete")
    public List<DependencyEdge> getCompleteDependencyPaths(
            @RequestParam String project,
            @RequestParam String module,
            @RequestParam String className) {

        return graphService.getCompleteDependencyPaths(
                project,
                module,
                className
        );
    }
}