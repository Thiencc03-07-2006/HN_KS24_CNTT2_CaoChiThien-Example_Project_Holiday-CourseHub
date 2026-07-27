package com.coursehub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base class for all CourseHub business exceptions.
 */
public class CourseHubException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public CourseHubException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }
}
