package com.traceops.traceops.model;

import lombok.Data;

import java.util.List;

@Data
public class ProjectRequest {

    private String project;
    private List<ServiceRequest> services;
}