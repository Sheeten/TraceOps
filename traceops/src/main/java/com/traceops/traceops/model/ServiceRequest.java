package com.traceops.traceops.model;

import lombok.Data;

import java.util.List;

@Data
public class ServiceRequest {

    private String name;
    private List<String> dependsOn;
}
