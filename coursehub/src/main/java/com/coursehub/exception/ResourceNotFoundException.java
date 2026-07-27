package com.coursehub.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CourseHubException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s không tìm thấy với %s: '%s'", resource, field, value),
              HttpStatus.NOT_FOUND);
    }
}
