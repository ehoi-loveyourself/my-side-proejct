package com.project.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class CartException extends RuntimeException {

    private final HttpStatus status;

    public CartException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}