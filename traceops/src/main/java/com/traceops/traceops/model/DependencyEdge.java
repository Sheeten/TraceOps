package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DependencyEdge {

    private String source;
    private String target;
    private String relationship;

}