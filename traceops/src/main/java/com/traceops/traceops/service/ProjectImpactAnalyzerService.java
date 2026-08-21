package com.traceops.traceops.service;

import com.traceops.traceops.model.ProjectImpact;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectImpactAnalyzerService {

    private final Path uploadDirectory =
            Paths.get("uploaded-projects");

    public ProjectImpact analyze(
            String projectName,
            String className,
            String methodName) {

        Path projectPath =
                uploadDirectory.resolve(projectName);

        if (!Files.exists(projectPath)) {
            throw new RuntimeException(
                    "Project not found: " + projectName
            );
        }

        List<String> dependencies = new ArrayList<>();
        List<String> usedBy = new ArrayList<>();

        try {

            Map<String, Path> projectClasses =
                    findProjectClasses(projectPath);

            Path selectedClass =
                    projectClasses.get(className);

            if (selectedClass == null) {
                throw new RuntimeException(
                        "Class not found: " + className
                );
            }

            String content =
                    Files.readString(selectedClass);

            String methodBody =
                    extractMethodBody(
                            content,
                            methodName
                    );

            if (methodBody == null) {
                throw new RuntimeException(
                        "Method not found: " + methodName
                );
            }

            // Find dependencies used inside the selected method
            for (Map.Entry<String, Path> entry :
                    projectClasses.entrySet()) {

                String projectClass =
                        entry.getKey();

                if (projectClass.equals(className)) {
                    continue;
                }

                Pattern classPattern =
                        Pattern.compile(
                                "\\b"
                                        + Pattern.quote(projectClass)
                                        + "\\b"
                        );

                Matcher matcher =
                        classPattern.matcher(methodBody);

                if (matcher.find()) {
                    dependencies.add(projectClass);
                }
            }

            // Find classes that actually call the selected method
            for (Map.Entry<String, Path> entry :
                    projectClasses.entrySet()) {

                String currentClass =
                        entry.getKey();

                if (currentClass.equals(className)) {
                    continue;
                }

                String currentContent =
                        Files.readString(entry.getValue());

                // Do not treat interfaces as callers
                if (isInterface(currentContent)) {
                    continue;
                }

                if (callsMethod(
                        currentContent,
                        methodName)) {

                    usedBy.add(currentClass);
                }
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to analyze project impact",
                    e
            );
        }

        return new ProjectImpact(
                className,
                methodName,
                dependencies,
                usedBy
        );
    }

    private Map<String, Path> findProjectClasses(
            Path projectPath) throws IOException {

        Map<String, Path> classes =
                new HashMap<>();

        Files.walk(projectPath)
                .filter(Files::isRegularFile)
                .filter(file ->
                        file.toString()
                                .endsWith(".java"))
                .forEach(file -> {

                    String className =
                            file.getFileName()
                                    .toString()
                                    .replace(
                                            ".java",
                                            ""
                                    );

                    classes.put(
                            className,
                            file
                    );
                });

        return classes;
    }

    private boolean isInterface(String content) {

        String code =
                removeComments(content);

        return Pattern.compile(
                "\\binterface\\s+[A-Za-z_$][A-Za-z0-9_$]*"
        ).matcher(code).find();
    }

    private String extractMethodBody(
            String content,
            String methodName) {

        String code =
                removeComments(content);

        Pattern pattern =
                Pattern.compile(
                        "\\b"
                                + Pattern.quote(methodName)
                                + "\\s*\\([^)]*\\)"
                                + "\\s*\\{"
                );

        Matcher matcher =
                pattern.matcher(code);

        if (!matcher.find()) {
            return null;
        }

        int bodyStart =
                matcher.end();

        int braceCount = 1;

        for (int i = bodyStart;
             i < code.length();
             i++) {

            char character =
                    code.charAt(i);

            if (character == '{') {
                braceCount++;
            }

            if (character == '}') {
                braceCount--;
            }

            if (braceCount == 0) {

                return code.substring(
                        bodyStart,
                        i
                );
            }
        }

        return null;
    }

    private boolean callsMethod(
            String content,
            String methodName) {

        String code =
                removeComments(content);

        /*
         * We specifically look for an actual method call:
         *
         * service.updatePatient(...)
         *
         * or
         *
         * updatePatient(...)
         *
         * but not:
         *
         * updatePatient(...)
         * as a method declaration.
         */

        Pattern callPattern =
                Pattern.compile(
                        "(?:\\b[A-Za-z_$][A-Za-z0-9_$]*\\s*\\.\\s*)?"
                                + "\\b"
                                + Pattern.quote(methodName)
                                + "\\s*\\("
                );

        Matcher matcher =
                callPattern.matcher(code);

        while (matcher.find()) {

            int start =
                    matcher.start();

            int end =
                    matcher.end();

            /*
             * Check what appears before the method name.
             */
            String before =
                    code.substring(
                            Math.max(
                                    0,
                                    start - 100
                            ),
                            start
                    );

            /*
             * Check what appears after the opening '('.
             */
            String after =
                    code.substring(
                            end
                    );

            /*
             * Find the beginning of the current line.
             */
            int lineStart =
                    code.lastIndexOf(
                            '\n',
                            start
                    ) + 1;

            int lineEnd =
                    code.indexOf(
                            '\n',
                            start
                    );

            if (lineEnd == -1) {
                lineEnd = code.length();
            }

            String line =
                    code.substring(
                            lineStart,
                            lineEnd
                    ).trim();

            /*
             * Ignore declarations such as:
             *
             * public Patient updatePatient(...)
             *
             * protected Patient updatePatient(...)
             *
             * Patient updatePatient(...)
             *
             */
            if (isMethodDeclaration(
                    line,
                    methodName)) {

                continue;
            }

            /*
             * Ignore interface/abstract declarations.
             */
            if (before.matches(
                    "(?s).*\\b(interface|abstract)\\s*$"
            )) {
                continue;
            }

            /*
             * Ignore method signatures that contain
             * modifiers and the method name.
             */
            if (line.matches(
                    ".*\\b(public|private|protected|static|final|abstract|default|synchronized)\\b.*"
                            + Pattern.quote(methodName)
                            + "\\s*\\(.*"
            )) {
                continue;
            }

            /*
             * If this is an actual invocation, return true.
             */
            return true;
        }

        return false;
    }

    private boolean isMethodDeclaration(
            String line,
            String methodName) {

        String normalized =
                line.trim();

        if (!normalized.contains(methodName)) {
            return false;
        }

        /*
         * Examples:
         *
         * public Patient updatePatient(...)
         * private Patient updatePatient(...)
         * protected Patient updatePatient(...)
         * public void updatePatient(...)
         */
        if (normalized.matches(
                ".*\\b(public|private|protected|static|final|abstract|default|synchronized)\\b.*"
                        + Pattern.quote(methodName)
                        + "\\s*\\(.*"
        )) {
            return true;
        }

        /*
         * Method declaration without modifier:
         *
         * Patient updatePatient(...)
         */
        if (normalized.matches(
                ".*\\b[A-Za-z_$][A-Za-z0-9_$<>\\[\\], ?]*\\s+"
                        + Pattern.quote(methodName)
                        + "\\s*\\(.*"
        )) {
            return true;
        }

        return false;
    }

    private String removeComments(String content) {

        String code =
                content.replaceAll(
                        "(?s)/\\*.*?\\*/",
                        ""
                );

        code =
                code.replaceAll(
                        "//.*",
                        ""
                );

        return code;
    }
}