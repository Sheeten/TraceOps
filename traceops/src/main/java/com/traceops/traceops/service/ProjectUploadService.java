package com.traceops.traceops.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectUploadService {

    private final ProjectAnalyzerService projectAnalyzerService;
    private final ProjectIncidentAnalyzerService projectIncidentAnalyzerService;

    private final Path uploadDirectory =
            Paths.get("uploaded-projects");

    public ProjectUploadService(
            ProjectAnalyzerService projectAnalyzerService,
            ProjectIncidentAnalyzerService projectIncidentAnalyzerService) {

        this.projectAnalyzerService =
                projectAnalyzerService;

        this.projectIncidentAnalyzerService =
                projectIncidentAnalyzerService;
    }

    public void uploadProject(MultipartFile file) {

        try {

            Files.createDirectories(
                    uploadDirectory
            );

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null) {
                throw new RuntimeException(
                        "Invalid file name"
                );
            }

            String projectName =
                    originalFileName
                            .replaceFirst(
                                    "(?i)\\.zip$",
                                    ""
                            );

            Path zipFile =
                    uploadDirectory.resolve(
                            originalFileName
                    );

            file.transferTo(zipFile);

            System.out.println(
                    "Uploading project: "
                            + projectName
            );

            extractZip(
                    zipFile,
                    projectName
            );

            System.out.println(
                    "Project extracted: "
                            + projectName
            );

            /*
             * Analyze project structure.
             */
            projectAnalyzerService
                    .analyzeAndSaveProject(
                            projectName
                    );

            /*
             * Analyze project for possible incidents.
             */
            List<?> incidents =
                    projectIncidentAnalyzerService
                            .analyzeProject(
                                    projectName
                            );

            System.out.println(
                    "Incidents detected: "
                            + incidents.size()
            );

            if (incidents.isEmpty()) {

                System.out.println(
                        "No incidents detected in: "
                                + projectName
                );

            } else {

                System.out.println(
                        "Incident analysis completed for: "
                                + projectName
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload project",
                    e
            );
        }
    }

    private void extractZip(
            Path zipFile,
            String projectName)
            throws IOException {

        Path extractDirectory =
                uploadDirectory.resolve(
                        projectName
                );

        Files.createDirectories(
                extractDirectory
        );

        try (
                InputStream inputStream =
                        Files.newInputStream(
                                zipFile
                        );

                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                inputStream
                        )
        ) {

            ZipEntry entry;

            while (
                    (entry =
                            zipInputStream.getNextEntry())
                            != null
            ) {

                Path outputPath =
                        extractDirectory
                                .resolve(
                                        entry.getName()
                                )
                                .normalize();

                if (!outputPath.startsWith(
                        extractDirectory.normalize()
                )) {

                    throw new IOException(
                            "Invalid ZIP entry"
                    );
                }

                if (entry.isDirectory()) {

                    Files.createDirectories(
                            outputPath
                    );

                } else {

                    Path parent =
                            outputPath.getParent();

                    if (parent != null) {

                        Files.createDirectories(
                                parent
                        );
                    }

                    Files.copy(
                            zipInputStream,
                            outputPath,
                            StandardCopyOption
                                    .REPLACE_EXISTING
                    );
                }

                zipInputStream.closeEntry();
            }
        }
    }
}