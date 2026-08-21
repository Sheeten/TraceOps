package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectedIncident {

    private String type;
    private String message;
    private String file;
    private String line;
    private String className;
    private String methodName;
}