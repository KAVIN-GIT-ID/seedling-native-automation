package com.company.automation.config;

public enum ExecutionMode {
    LOCAL,
    CI,
    BROWSERSTACK,
    LAMBDATEST;

    public static ExecutionMode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LOCAL;
        }
        try {
            return ExecutionMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOCAL;
        }
    }
}
