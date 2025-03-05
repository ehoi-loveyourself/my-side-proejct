package com.project.ecommerce.common.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorDetails {
    private String message;
    private Map<String, String> details;

    public ErrorDetails(String message) {
        this.message = message;
    }

    public ErrorDetails(String message, Map<String, String> details) {
        this.message = message;
        this.details = details;
    }
}