package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDetails {

    private String incident;
    private String service;
    private String error;
    private String cause;
    private String solution;
    private String database;
}