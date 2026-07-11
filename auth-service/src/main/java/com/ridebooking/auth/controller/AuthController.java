package com.ridebooking.auth.controller;

import com.ridebooking.auth.dto.*;
import com.ridebooking.auth.service.AuthService;
import com.ridebooking.common.constant.ServiceConstants;
import com.ridebooking.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * AUTH CONTROLLER — REST ENDPOINTS
 * ============================================================
 *
 * This is the HTTP entry point for authentication operations.
 * The controller is THIN — it:
 * 1. Receives the HTTP request
 * 2. Delegates to the service layer
 * 3. Returns an HTTP response with proper status code
 *
 * It does NOT contain business logic. If you find yourself
 * writing "if" statements here, move them to the service.
 *
 * @RestController = @Controller + @ResponseBody
 * Every method automatically serializes its return value to JSON.
 *
 * @RequestMapping sets the base URL for all endpoints in this class.
 */
@RestController
@RequestMapping(ServiceConstants.AUTH_BASE_PATH)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/register
     *
     * Creates a new user account.
     *
     * @Valid tells Spring to validate the request body using the
     * validation annotations on RegisterRequest (@NotBlank, @Email, etc.)
     * If validation fails, Spring automatically returns 400 Bad Request.
     *
     * @RequestBody tells Spring to deserialize the JSON body into
     * a RegisterRequest record.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                // ^^^ 201 Created — more specific than 200 OK for creation
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * POST /api/v1/auth/login
     *
     * Authenticates existing user and returns JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/v1/auth/refresh
     *
     * Gets a new access token using a refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    /**
     * GET /api/v1/auth/test
     *
     * Health check endpoint — no auth required.
     * Useful for verifying the service is running.
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Auth Service is running"));
    }
}
