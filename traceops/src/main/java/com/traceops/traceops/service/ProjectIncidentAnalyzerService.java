package com.traceops.traceops.service;

import com.traceops.traceops.model.DetectedIncident;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectIncidentAnalyzerService {

    private final Path uploadDirectory =
            Paths.get("uploaded-projects");

    public List<DetectedIncident> analyzeProject(
            String projectName) {

        Path projectPath =
                uploadDirectory.resolve(projectName);

        if (!Files.exists(projectPath)) {
            throw new RuntimeException(
                    "Project not found: " + projectName
            );
        }

        List<DetectedIncident> incidents =
                new ArrayList<>();

        try {
            Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .forEach(file ->
                            analyzeFile(file, incidents)
                    );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to analyze project",
                    e
            );
        }

        return incidents;
    }

    private void analyzeFile(
            Path file,
            List<DetectedIncident> incidents) {

        String fileName =
                file.getFileName()
                        .toString()
                        .toLowerCase();

        if (!(fileName.endsWith(".java")
                || fileName.endsWith(".log")
                || fileName.endsWith(".txt"))) {
            return;
        }

        try {

            String content =
                    Files.readString(file);

            String className =
                    findClassName(content);

            String currentMethod = null;

            String[] lines =
                    content.split("\\R");

            for (int i = 0; i < lines.length; i++) {

                String line = lines[i];

                String detectedMethod =
                        findMethodName(line);

                if (detectedMethod != null) {
                    currentMethod = detectedMethod;
                }

                checkLine(
                        line,
                        file,
                        i + 1,
                        className,
                        currentMethod,
                        incidents
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read file: " + file
            );
        }
    }

    private void checkLine(
            String line,
            Path file,
            int lineNumber,
            String className,
            String methodName,
            List<DetectedIncident> incidents) {

        String trimmed = line.trim();
        String lower = trimmed.toLowerCase();

        if (trimmed.isEmpty()
                || trimmed.startsWith("//")
                || trimmed.startsWith("*")
                || trimmed.startsWith("/*")
                || trimmed.startsWith("import ")
                || trimmed.startsWith("package ")) {
            return;
        }

        /*
         * 1. Runtime errors already recorded
         *    in logs or source.
         */
        if (lower.contains("logger.error(")
                || lower.contains("log.error(")
                || lower.contains("error:")
                || lower.contains("fatal error")
                || lower.contains("connection refused")
                || lower.contains("timeout")) {

            addIncident(
                    "RUNTIME_ERROR",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 2. Broad exception handling.
         *
         * Example:
         *
         * catch (Exception e)
         *
         * This can hide the actual cause of an error.
         */
        if (lower.matches(
                ".*\\bcatch\\s*\\(\\s*exception\\s+\\w+\\s*\\).*")) {

            addIncident(
                    "BROAD_EXCEPTION_HANDLING",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 3. Catching Throwable is even broader
         *    than catching Exception.
         */
        if (lower.matches(
                ".*\\bcatch\\s*\\(\\s*throwable\\s+\\w+\\s*\\).*")) {

            addIncident(
                    "BROAD_EXCEPTION_HANDLING",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 4. Explicit exception throwing.
         *
         * Example:
         *
         * throw new RuntimeException(...);
         */
        if (lower.matches(
                ".*\\bthrow\\s+new\\s+[a-zA-Z_$][a-zA-Z0-9_$]*exception\\b.*")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 5. Optional.orElseThrow()
         */
        if (lower.contains("orelsethrow(")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 6. Optional.get()
         */
        if (lower.contains(".get()")
                && !lower.contains("getclass()")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 7. Array access.
         *
         * patients[0]       -> detected
         * users[index]      -> detected
         * String[] args     -> NOT detected
         */
        if (lower.matches(
                ".*\\b\\w+\\s*\\[\\s*[^\\]]+\\s*\\].*")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 8. List.get(index)
         */
        if (lower.matches(
                ".*\\.get\\s*\\(\\s*\\w+\\s*\\).*")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 9. Number parsing.
         */
        if (lower.contains("integer.parseint(")
                || lower.contains("long.parselong(")
                || lower.contains("double.parsedouble(")
                || lower.contains("float.parsefloat(")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 10. Explicit division by zero.
         */
        if (lower.matches(
                ".*\\/\\s*0\\b.*")) {

            addIncident(
                    "POTENTIAL_EXCEPTION",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );

            return;
        }

        /*
         * 11. Optional.of() can throw
         *     NullPointerException when passed null.
         */
        if (lower.contains("optional.of(")) {

            addIncident(
                    "POTENTIAL_NULL_POINTER",
                    trimmed,
                    file,
                    lineNumber,
                    className,
                    methodName,
                    incidents
            );
        }
    }

    private void addIncident(
            String type,
            String message,
            Path file,
            int lineNumber,
            String className,
            String methodName,
            List<DetectedIncident> incidents) {

        incidents.add(
                new DetectedIncident(
                        type,
                        message,
                        file.toString(),
                        String.valueOf(lineNumber),
                        className,
                        methodName == null
                                ? "Unknown"
                                : methodName
                )
        );
    }

    private String findClassName(
            String content) {

        Pattern pattern =
                Pattern.compile(
                        "\\bclass\\s+([A-Za-z_$][A-Za-z0-9_$]*)"
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "Unknown";
    }

    private String findMethodName(
            String line) {

        String trimmed =
                line.trim();

        if (trimmed.startsWith("if ")
                || trimmed.startsWith("if(")
                || trimmed.startsWith("for ")
                || trimmed.startsWith("for(")
                || trimmed.startsWith("while ")
                || trimmed.startsWith("while(")
                || trimmed.startsWith("switch ")
                || trimmed.startsWith("switch(")
                || trimmed.startsWith("catch ")
                || trimmed.startsWith("catch(")) {

            return null;
        }

        Pattern pattern =
                Pattern.compile(
                        "(?:public|private|protected|static|final|synchronized|native|abstract|\\s)+"
                                + "[\\w<>\\[\\], ?]+\\s+"
                                + "([A-Za-z_$][A-Za-z0-9_$]*)\\s*\\("
                );

        Matcher matcher =
                pattern.matcher(trimmed);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}