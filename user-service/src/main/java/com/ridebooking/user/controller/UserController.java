package com.ridebooking.user.controller;

import com.ridebooking.common.constant.ServiceConstants;
import com.ridebooking.common.dto.ApiResponse;
import com.ridebooking.user.dto.UserProfileRequest;
import com.ridebooking.user.dto.UserProfileResponse;
import com.ridebooking.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * USER CONTROLLER
 * ============================================================
 *
 * Notice: There is NO @Valid or authentication in this controller.
 * The user's identity comes from the X-User-Id header, which is
 * added by the API Gateway after validating the JWT token.
 *
 * This service is "trusting" — it trusts whatever the Gateway says.
 * If the Gateway is properly secured (which Member 1 handles),
 * this service is safe. This is the "Gatekeeper Pattern":
 *
 *   [Client] → [API Gateway + Auth] → { X-User-Id header } → [User Service]
 *                 ^^^ validates JWT here          ^^^ trusts this header
 */
@RestController
@RequestMapping(ServiceConstants.USER_BASE_PATH)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/v1/users/profile
     *
     * Returns the authenticated user's profile.
     * The userId is extracted from the X-User-Id header.
     *
     * @RequestHeader("X-User-Id") tells Spring to read the
     * userId from the HTTP header with that name.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @RequestHeader(ServiceConstants.HEADER_USER_ID) String userId) {

        UserProfileResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
    }

    /**
     * PUT /api/v1/users/profile
     *
     * Creates or updates the authenticated user's profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader(ServiceConstants.HEADER_USER_ID) String userId,
            @Valid @RequestBody UserProfileRequest request) {

        UserProfileResponse profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    /**
     * GET /api/v1/users/test
     *
     * Health check endpoint.
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("User Service is running"));
    }
}
