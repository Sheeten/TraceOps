package com.traceops.traceops.service;

import com.traceops.traceops.model.CodeDependency;
import com.traceops.traceops.repo.IncidentRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectCodeAnalyzerService {

    private final Path uploadDirectory =
            Paths.get("uploaded-projects");

    private final IncidentRepository incidentRepository;

    public ProjectCodeAnalyzerService(
            IncidentRepository incidentRepository) {

        this.incidentRepository =
                incidentRepository;
    }

    public List<CodeDependency> analyzeProject(
            String projectName,
            String moduleName) {

        Path projectRoot =
                findProjectRoot(
                        projectName,
                        moduleName
                );

        if (projectRoot == null) {
            throw new RuntimeException(
                    "Project/module not found: "
                            + projectName
                            + "/"
                            + moduleName
            );
        }

        System.out.println(
                "Analyzing project: "
                        + projectRoot
        );

        List<Path> javaFiles =
                new ArrayList<>();

        try {

            Files.walk(projectRoot)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .toLowerCase()
                                    .endsWith(".java")
                    )
                    .forEach(javaFiles::add);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not read project files",
                    e
            );
        }

        System.out.println(
                "Java files found: "
                        + javaFiles.size()
        );

        Set<String> classNames =
                new HashSet<>();

        for (Path file : javaFiles) {

            String className =
                    getClassName(file);

            if (className != null) {
                classNames.add(className);
            }
        }

        System.out.println(
                "Classes found: "
                        + classNames
        );

        List<CodeDependency> dependencies =
                new ArrayList<>();

        for (Path file : javaFiles) {

            String sourceClass =
                    getClassName(file);

            if (sourceClass == null) {
                continue;
            }

            analyzeImports(
                    file,
                    sourceClass,
                    classNames,
                    dependencies
            );

            analyzeFields(
                    file,
                    sourceClass,
                    classNames,
                    dependencies
            );

            analyzeConstructor(
                    file,
                    sourceClass,
                    classNames,
                    dependencies
            );

            analyzeClassUsage(
                    file,
                    sourceClass,
                    classNames,
                    dependencies
            );
        }

        System.out.println(
                "Dependencies found: "
                        + dependencies.size()
        );

        List<CodeDependency> result =
                removeDuplicates(
                        dependencies
                );

        System.out.println(
                "Dependencies after removing duplicates: "
                        + result.size()
        );

        /*
         * Save the analyzed code dependency graph
         * into CognoDB.
         */
        incidentRepository.saveCodeDependencies(
                projectName,
                moduleName,
                result
        );

        System.out.println(
                "Code dependency graph saved to CognoDB."
        );

        return result;
    }

    private Path findProjectRoot(
            String projectName,
            String moduleName) {

        Path projectDirectory =
                uploadDirectory.resolve(
                        projectName
                );

        System.out.println(
                "Looking for project: "
                        + projectDirectory
        );

        if (!Files.exists(projectDirectory)) {
            return null;
        }

        try {

            Optional<Path> module =
                    Files.walk(projectDirectory)
                            .filter(Files::isDirectory)
                            .filter(path -> {

                                Path fileName =
                                        path.getFileName();

                                return fileName != null
                                        && fileName
                                        .toString()
                                        .equalsIgnoreCase(
                                                moduleName
                                        )
                                        && containsJavaFiles(
                                        path
                                );
                            })
                            .findFirst();

            if (module.isPresent()) {

                System.out.println(
                        "Module found: "
                                + module.get()
                );

                return module.get();
            }

            if (containsJavaFiles(
                    projectDirectory
            )) {

                System.out.println(
                        "Using project directory: "
                                + projectDirectory
                );

                return projectDirectory;
            }

            Optional<Path> javaRoot =
                    Files.walk(projectDirectory)
                            .filter(Files::isDirectory)
                            .filter(
                                    this::containsJavaFiles
                            )
                            .findFirst();

            if (javaRoot.isPresent()) {

                System.out.println(
                        "Java root found: "
                                + javaRoot.get()
                );

                return javaRoot.get();
            }

            return null;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error searching uploaded project",
                    e
            );
        }
    }

    private boolean containsJavaFiles(
            Path directory) {

        try {

            return Files.walk(directory)
                    .anyMatch(path ->
                            Files.isRegularFile(path)
                                    && path.toString()
                                    .toLowerCase()
                                    .endsWith(".java")
                    );

        } catch (IOException e) {

            return false;
        }
    }

    private void analyzeImports(
            Path file,
            String sourceClass,
            Set<String> classNames,
            List<CodeDependency> dependencies) {

        try {

            String content =
                    Files.readString(file);

            Pattern pattern =
                    Pattern.compile(
                            "import\\s+[\\w.]+\\.([A-Za-z_][A-Za-z0-9_]*)\\s*;"
                    );

            Matcher matcher =
                    pattern.matcher(content);

            while (matcher.find()) {

                String importedClass =
                        matcher.group(1);

                if (classNames.contains(
                        importedClass
                )
                        && !sourceClass.equals(
                        importedClass
                )) {

                    addDependency(
                            dependencies,
                            sourceClass,
                            importedClass,
                            "IMPORTS"
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read: "
                            + file
            );
        }
    }

    private void analyzeFields(
            Path file,
            String sourceClass,
            Set<String> classNames,
            List<CodeDependency> dependencies) {

        try {

            String content =
                    Files.readString(file);

            Pattern pattern =
                    Pattern.compile(
                            "\\b([A-Z][A-Za-z0-9_]*)\\s+"
                                    + "[a-zA-Z0-9_]+\\s*;"
                    );

            Matcher matcher =
                    pattern.matcher(content);

            while (matcher.find()) {

                String dependency =
                        matcher.group(1);

                if (classNames.contains(
                        dependency
                )
                        && !sourceClass.equals(
                        dependency
                )) {

                    addDependency(
                            dependencies,
                            sourceClass,
                            dependency,
                            "USES"
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read: "
                            + file
            );
        }
    }

    private void analyzeConstructor(
            Path file,
            String sourceClass,
            Set<String> classNames,
            List<CodeDependency> dependencies) {

        try {

            String content =
                    Files.readString(file);

            Pattern pattern =
                    Pattern.compile(
                            "(?:public\\s+)?"
                                    + Pattern.quote(
                                    sourceClass
                            )
                                    + "\\s*\\(([^)]*)\\)"
                    );

            Matcher matcher =
                    pattern.matcher(content);

            while (matcher.find()) {

                String parameters =
                        matcher.group(1);

                Pattern parameterPattern =
                        Pattern.compile(
                                "\\b([A-Z][A-Za-z0-9_]*)\\s+"
                                        + "[a-zA-Z0-9_]+"
                        );

                Matcher parameterMatcher =
                        parameterPattern.matcher(
                                parameters
                        );

                while (
                        parameterMatcher.find()
                ) {

                    String dependency =
                            parameterMatcher.group(1);

                    if (classNames.contains(
                            dependency
                    )
                            && !sourceClass.equals(
                            dependency
                    )) {

                        addDependency(
                                dependencies,
                                sourceClass,
                                dependency,
                                "INJECTS"
                        );
                    }
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read: "
                            + file
            );
        }
    }

    /*
     * Detects usage of another project class
     * anywhere inside the Java source file.
     *
     * Example:
     *
     * Book book = new Book();
     * Book getBook()
     * save(Book book)
     * new Book()
     */
    private void analyzeClassUsage(
            Path file,
            String sourceClass,
            Set<String> classNames,
            List<CodeDependency> dependencies) {

        try {

            String content =
                    Files.readString(file);

            for (String className :
                    classNames) {

                if (className.equals(
                        sourceClass
                )) {
                    continue;
                }

                Pattern pattern =
                        Pattern.compile(
                                "\\b"
                                        + Pattern.quote(
                                        className
                                )
                                        + "\\b"
                        );

                Matcher matcher =
                        pattern.matcher(content);

                if (matcher.find()) {

                    addDependency(
                            dependencies,
                            sourceClass,
                            className,
                            "USES"
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read: "
                            + file
            );
        }
    }

    private void addDependency(
            List<CodeDependency> dependencies,
            String source,
            String target,
            String relationship) {

        boolean exists =
                dependencies.stream()
                        .anyMatch(d ->
                                d.getSource()
                                        .equals(source)
                                        && d.getTarget()
                                        .equals(target)
                        );

        if (!exists) {

            dependencies.add(
                    new CodeDependency(
                            source,
                            target,
                            relationship
                    )
            );
        }
    }

    private List<CodeDependency> removeDuplicates(
            List<CodeDependency> dependencies) {

        Map<String, CodeDependency> unique =
                new LinkedHashMap<>();

        for (CodeDependency dependency :
                dependencies) {

            String key =
                    dependency.getSource()
                            + "->"
                            + dependency.getTarget();

            unique.putIfAbsent(
                    key,
                    dependency
            );
        }

        return new ArrayList<>(
                unique.values()
        );
    }

    private String getClassName(
            Path file) {

        try {

            String content =
                    Files.readString(file);

            Pattern pattern =
                    Pattern.compile(
                            "\\b(?:public\\s+|private\\s+|protected\\s+)?"
                                    + "(?:abstract\\s+|final\\s+)?"
                                    + "(?:class|interface|enum)\\s+"
                                    + "([A-Za-z_][A-Za-z0-9_]*)"
                    );

            Matcher matcher =
                    pattern.matcher(content);

            if (matcher.find()) {
                return matcher.group(1);
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read: "
                            + file
            );
        }

        return null;
    }
}