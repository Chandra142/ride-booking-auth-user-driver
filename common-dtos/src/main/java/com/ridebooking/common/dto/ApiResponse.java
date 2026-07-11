package com.ridebooking.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * ============================================================
 * STANDARD API RESPONSE WRAPPER
 * ============================================================
 *
 * Every endpoint in the system returns responses wrapped in this
 * class. This gives the frontend a consistent envelope to parse:
 *
 *   {
 *       "success": true,
 *       "message": "User registered",
 *       "data": { ... },
 *       "timestamp": "2024-01-15T10:30:00"
 *   }
 *
 * WHY THIS MATTERS:
 * Without a standard response wrapper, different services return
 * different shapes — some return the object directly, some return
 * a Map, some return error text. The frontend has to special-case
 * every service. With ApiResponse, every service returns the same
 * JSON structure, which is easy to handle in a generic interceptor.
 *
 * @param <T> The type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// ^^^ If a field is null, Jackson skips it in JSON output.
//     This avoids sending "data": null for error responses.
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Private constructor — users call the static factory methods
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // ========================
    // Static Factory Methods
    // ========================

    /**
     * Success response WITH a data payload.
     * Example: After registration, return the user profile.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    /**
     * Success response WITHOUT data (e.g., deletion confirmation).
     */
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        return response;
    }

    /**
     * Error response.
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    // ========================
    // Getters
    // ========================

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
