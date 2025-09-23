package com.ecommerce.auth.exception;

public class PasswordConfirmNotMatchException extends RuntimeException {
    public PasswordConfirmNotMatchException(String message) {
        super(message);
    }
}
