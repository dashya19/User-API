package com.example.userapi.exception;

public class RoleInUseException extends RuntimeException {
    public RoleInUseException(String message) {
        super(message);
    }
}
