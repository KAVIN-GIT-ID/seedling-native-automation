package com.company.automation.config;

public enum Environment {
    QA, PROD;

    public static Environment from(String value) {
        return Environment.valueOf(value.trim().toUpperCase());
    }
}
