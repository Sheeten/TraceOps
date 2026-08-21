package com.traceops.traceops.controller;

import com.traceops.traceops.model.ProjectInfo;
import com.traceops.traceops.service.ProjectAnalyzerService;
import com.traceops.traceops.service.ProjectUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://traceops-xis6.onrender.com/api/projects"
})
public class ProjectController {

    private final ProjectUploadService projectUploadService;
    private final ProjectAnalyzerService projectAnalyzerService;

    public ProjectController(
            ProjectUploadService projectUploadService,
            ProjectAnalyzerService projectAnalyzerService) {

        this.projectUploadService = projectUploadService;
        this.projectAnalyzerService = projectAnalyzerService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProject(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please upload a file");
        }

        if (file.getOriginalFilename() == null ||
                !file.getOriginalFilename()
                        .toLowerCase()
                        .endsWith(".zip")) {

            return ResponseEntity.badRequest()
                    .body("Only ZIP files are supported");
        }

        projectUploadService.uploadProject(file);

        return ResponseEntity.ok(
                "Project uploaded successfully"
        );
    }

    @GetMapping
    public List<String> getProjects() {
        return projectAnalyzerService.getProjects();
    }

    @GetMapping("/{projectName}/services")
    public List<String> getServices(
            @PathVariable String projectName) {

        return projectAnalyzerService
                .detectServices(projectName);
    }

    @GetMapping("/{projectName}/{moduleName}/info")
    public ProjectInfo getProjectInfo(
            @PathVariable String projectName,
            @PathVariable String moduleName) {

        return projectAnalyzerService
                .analyzePom(
                        projectName,
                        moduleName
                );
    }
}