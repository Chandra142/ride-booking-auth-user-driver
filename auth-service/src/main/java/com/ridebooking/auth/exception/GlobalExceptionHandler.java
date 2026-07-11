package com.ridebooking.auth.exception;

import com.ridebooking.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ============================================================
 * GLOBAL EXCEPTION HANDLER
 * ============================================================
 *
 * Without this class, when an exception is thrown:
 * 1. Spring catches it
 * 2. Returns a default error page (ugly HTML) with status 500
 * 3. The client gets no useful information
 *
 * With @RestControllerAdvice, we intercept ALL exceptions from
 * ALL controllers in this service and return a consistent JSON
 * response every time.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * It's like an AOP "around" advice for every controller method.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors (@Valid failures).
     *
     * When @Valid fails, Spring throws MethodArgumentNotValidException.
     * This method catches it and returns a 400 Bad Request with
     * a message listing ALL validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        /*
         * Collect all field errors into a single message string.
         * Example: "email: Invalid email format, password: Password must be between 6 and 100 characters"
         */
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * Handles "Invalid email or password" from login.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles "Email already registered" and similar.
     * Returns 400 Bad Request or 409 Conflict.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Catch-all for any unhandled exception.
     * Returns 500 Internal Server Error.
     * NEVER expose stack traces to the client — it's a security risk.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
