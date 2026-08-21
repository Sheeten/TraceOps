package com.traceops.traceops.service;

import com.traceops.traceops.model.ProjectInfo;
import com.traceops.traceops.model.ProjectRequest;
import com.traceops.traceops.model.ServiceRequest;
import com.traceops.traceops.repo.IncidentRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectAnalyzerService {

    private final IncidentRepository incidentRepository;

    public ProjectAnalyzerService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<String> getProjects() {
        Path uploadDirectory = Paths.get("uploaded-projects");

        if (!Files.exists(uploadDirectory)) {
            return List.of();
        }

        try {
            return Files.list(uploadDirectory)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read uploaded projects",
                    e
            );
        }
    }

    public void analyzeAndSaveProject(String projectName) {

        System.out.println(
                "Starting automatic project analysis: " + projectName
        );

        List<String> services = detectServices(projectName);

        List<ServiceRequest> serviceRequests =
                services.stream()
                        .map(service -> {

                            ServiceRequest request =
                                    new ServiceRequest();

                            request.setName(service);
                            request.setDependsOn(List.of());

                            return request;
                        })
                        .toList();

        ProjectRequest projectRequest =
                new ProjectRequest();

        projectRequest.setProject(projectName);
        projectRequest.setServices(serviceRequests);

        incidentRepository.createProject(projectRequest);

        System.out.println(
                "Project saved to Neo4j: " + projectName
        );
    }

    public List<String> detectServices(String projectName) {

        Path projectPath = Paths.get(
                "uploaded-projects",
                projectName
        );

        if (!Files.exists(projectPath)) {
            throw new RuntimeException(
                    "Project not found: " + projectPath
            );
        }

        try {

            /*
             * Find the root pom.xml.
             */
            Path rootPom = findRootPom(projectPath);

            if (rootPom == null) {
                return List.of();
            }

            String pom =
                    Files.readString(rootPom);

            /*
             * Check whether the root pom contains
             * a <modules> section.
             */
            List<String> modules =
                    findModules(pom);

            /*
             * If there are no Maven modules,
             * this is a single-module project.
             *
             * Return the project name as the module name.
             */
            if (modules.isEmpty()) {
                return List.of(
                        projectPath.getFileName().toString()
                );
            }

            /*
             * Return only actual module names.
             */
            return modules.stream()
                    .map(module ->
                            Paths.get(module)
                                    .getFileName()
                                    .toString()
                    )
                    .distinct()
                    .toList();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to analyze project",
                    e
            );
        }
    }

    private Path findRootPom(Path projectPath) {

        try {

            /*
             * First look for pom.xml directly inside
             * the uploaded project directory.
             */
            Path directPom =
                    projectPath.resolve("pom.xml");

            if (Files.exists(directPom)) {
                return directPom;
            }

            /*
             * Some ZIP files contain another top-level
             * folder.
             *
             * Example:
             *
             * uploaded-projects/
             *   MyProject/
             *      SamsTrack/
             *          pom.xml
             */
            return Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .equalsIgnoreCase("pom.xml")
                    )
                    .findFirst()
                    .orElse(null);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to find root pom.xml",
                    e
            );
        }
    }

    private List<String> findModules(String content) {

        List<String> modules =
                new ArrayList<>();

        Pattern modulesPattern =
                Pattern.compile(
                        "<modules>\\s*(.*?)\\s*</modules>",
                        Pattern.DOTALL
                );

        Matcher modulesMatcher =
                modulesPattern.matcher(content);

        if (!modulesMatcher.find()) {
            return modules;
        }

        String modulesContent =
                modulesMatcher.group(1);

        Pattern modulePattern =
                Pattern.compile(
                        "<module>\\s*(.*?)\\s*</module>",
                        Pattern.DOTALL
                );

        Matcher moduleMatcher =
                modulePattern.matcher(modulesContent);

        while (moduleMatcher.find()) {

            String module =
                    moduleMatcher.group(1).trim();

            if (!module.isEmpty()) {
                modules.add(module);
            }
        }

        return modules;
    }

    public ProjectInfo analyzePom(
            String projectName,
            String moduleName) {

        Path projectPath = Paths.get(
                "uploaded-projects",
                projectName
        );

        if (!Files.exists(projectPath)) {
            throw new RuntimeException(
                    "Project not found: " + projectName
            );
        }

        Path pomPath =
                findPomFile(
                        projectPath,
                        moduleName
                );

        if (pomPath == null) {
            throw new RuntimeException(
                    "pom.xml not found for module: "
                            + moduleName
            );
        }

        try {

            String pom =
                    Files.readString(pomPath);

            String groupId =
                    findValue(pom, "groupId");

            String artifactId =
                    findValue(pom, "artifactId");

            String javaVersion =
                    findProperty(
                            pom,
                            "java.version"
                    );

            String springBootVersion =
                    findParentVersion(pom);

            List<String> dependencies =
                    findDependencies(pom);

            return new ProjectInfo(
                    moduleName,
                    groupId,
                    artifactId,
                    javaVersion,
                    springBootVersion,
                    dependencies
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read pom.xml",
                    e
            );
        }
    }

    private Path findPomFile(
            Path projectPath,
            String moduleName) {

        try {

            /*
             * If the selected module is the project itself,
             * use the first/root pom.xml.
             */
            if (moduleName.equalsIgnoreCase(
                    projectPath.getFileName().toString()
            )) {

                Path directPom =
                        projectPath.resolve("pom.xml");

                if (Files.exists(directPom)) {
                    return directPom;
                }

                return Files.walk(projectPath)
                        .filter(Files::isRegularFile)
                        .filter(path ->
                                path.getFileName()
                                        .toString()
                                        .equalsIgnoreCase("pom.xml")
                        )
                        .findFirst()
                        .orElse(null);
            }

            /*
             * Normal multi-module case.
             */
            return Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .equalsIgnoreCase("pom.xml")
                    )
                    .filter(path -> {

                        Path parent =
                                path.getParent();

                        return parent != null &&
                                parent.getFileName()
                                        .toString()
                                        .equalsIgnoreCase(
                                                moduleName
                                        );
                    })
                    .findFirst()
                    .orElse(null);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to search project files",
                    e
            );
        }
    }

    private String findValue(
            String content,
            String tag) {

        Pattern pattern =
                Pattern.compile(
                        "<" + tag + ">\\s*(.*?)\\s*</" +
                                tag + ">",
                        Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String findProperty(
            String content,
            String property) {

        Pattern pattern =
                Pattern.compile(
                        "<" + Pattern.quote(property) +
                                ">\\s*(.*?)\\s*</" +
                                Pattern.quote(property) +
                                ">",
                        Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String findParentVersion(
            String content) {

        Pattern pattern =
                Pattern.compile(
                        "<parent>\\s*(.*?)\\s*</parent>",
                        Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {

            String parent =
                    matcher.group(1);

            return findValue(
                    parent,
                    "version"
            );
        }

        return null;
    }

    private List<String> findDependencies(
            String content) {

        List<String> dependencies =
                new ArrayList<>();

        Pattern pattern =
                Pattern.compile(
                        "<dependency>\\s*(.*?)\\s*</dependency>",
                        Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(content);

        while (matcher.find()) {

            String dependency =
                    matcher.group(1);

            String groupId =
                    findValue(
                            dependency,
                            "groupId"
                    );

            String artifactId =
                    findValue(
                            dependency,
                            "artifactId"
                    );

            if (groupId != null &&
                    artifactId != null) {

                dependencies.add(
                        groupId + ":" + artifactId
                );
            }
        }

        return dependencies;
    }
}