package com.project.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class AddressException extends RuntimeException {

    private final HttpStatus status;

    public AddressException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}