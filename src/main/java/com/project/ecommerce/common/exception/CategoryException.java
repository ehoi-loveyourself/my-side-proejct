package com.project.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class CategoryException extends RuntimeException {

    private final HttpStatus status;

    public CategoryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}