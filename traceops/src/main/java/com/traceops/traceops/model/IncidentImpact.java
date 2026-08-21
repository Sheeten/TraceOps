package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentImpact {

    private String className;
    private String methodName;
    private List<String> dependencies;
    private List<String> usedBy;
}