package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RootCauseAnalysis {

    private String type;
    private String className;
    private String methodName;
    private String issue;
    private String likelyCause;
    private String trigger;
    private String suggestedResolution;
}