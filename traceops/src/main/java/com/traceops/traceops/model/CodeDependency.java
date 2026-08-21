package com.traceops.traceops.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeDependency {

    private String source;
    private String target;
    private String relationship;
}