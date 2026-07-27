package com.coursehub.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends CourseHubException {
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
    public BadRequestException(String message) {
        super("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}
