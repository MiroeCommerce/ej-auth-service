package com.ecommerce.auth.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
    private final String fieldName;

    public UserAlreadyExistsException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }
}
