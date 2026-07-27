package com.coursehub.exception;

import com.coursehub.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Unit Tests")
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleCourseHubException — returns correct response body and status")
    void handleCourseHubException_returnsError() {
        CourseHubException ex = new CourseHubException("ERR_001", "Business logic error", HttpStatus.PAYMENT_REQUIRED);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCourseHubException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ERR_001");
        assertThat(response.getBody().getMessage()).isEqualTo("Business logic error");
    }

    @Test
    @DisplayName("handleBadCredentialsException — returns bad credentials status and body")
    void handleBadCredentialsException_returnsError() {
        BadCredentialsException ex = new BadCredentialsException("Invalid password");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadCredentialsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTH_002");
    }

    @Test
    @DisplayName("handleLockedException — returns locked account details")
    void handleLockedException_returnsError() {
        LockedException ex = new LockedException("Locked");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleLockedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTH_007");
    }

    @Test
    @DisplayName("handleAccessDeniedException — returns forbidden status")
    void handleAccessDeniedException_returnsError() {
        AccessDeniedException ex = new AccessDeniedException("Denied");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("handleGeneralException — returns internal server error status")
    void handleGeneralException_returnsError() {
        Exception ex = new Exception("Fatal database disconnection");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("handleValidationExceptions — extracts fields errors")
    void handleValidationExceptions_returnsMappedErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                null,
                bindingResult
        );

        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getData()).containsEntry("email", "must not be blank");
    }
}
