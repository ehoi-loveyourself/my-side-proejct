package com.project.ecommerce.common.exception;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
}