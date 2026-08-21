package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfo {

    private String projectName;
    private String groupId;
    private String artifactId;
    private String javaVersion;
    private String springBootVersion;
    private List<String> dependencies;
}