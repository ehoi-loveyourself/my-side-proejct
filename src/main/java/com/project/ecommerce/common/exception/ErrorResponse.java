package com.project.ecommerce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@Builder
@AllArgsConstructor
public class ErrorResponse {
    private String status;
    private ErrorDetails error;
}