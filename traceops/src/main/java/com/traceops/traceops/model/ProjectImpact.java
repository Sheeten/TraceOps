package com.traceops.traceops.model;

import java.util.List;

public class ProjectImpact {

    private String className;
    private String methodName;
    private List<String> dependencies;
    private List<String> usedBy;

    public ProjectImpact() {
    }

    public ProjectImpact(
            String className,
            String methodName,
            List<String> dependencies,
            List<String> usedBy) {

        this.className = className;
        this.methodName = methodName;
        this.dependencies = dependencies;
        this.usedBy = usedBy;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<String> usedBy) {
        this.usedBy = usedBy;
    }
}