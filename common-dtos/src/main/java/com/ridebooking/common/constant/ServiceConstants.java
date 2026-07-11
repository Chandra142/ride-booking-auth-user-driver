package com.ridebooking.common.constant;

/**
 * ============================================================
 * SERVICE-WIDE CONSTANTS
 * ============================================================
 *
 * Central place for magic strings so they're not scattered
 * across 20 files. If you rename a header or change a URL
 * prefix, you change it here — not in every controller.
 */
public final class ServiceConstants {

    private ServiceConstants() {
        // Private constructor prevents instantiation (utility class pattern)
    }

    // ========================
    // API Path Prefixes
    // ========================

    /** All auth endpoints start with this path */
    public static final String AUTH_BASE_PATH = "/api/v1/auth";

    /** All user endpoints start with this path */
    public static final String USER_BASE_PATH = "/api/v1/users";

    /** All driver endpoints start with this path */
    public static final String DRIVER_BASE_PATH = "/api/v1/drivers";

    // ========================
    // Header Names
    // ========================

    /**
     * After the API Gateway / Auth Service validates a JWT token,
     * it adds this header with the authenticated user's ID.
     * Downstream services (User Service) read this header instead
     * of parsing the JWT themselves.
     *
     * WHY THIS MATTERS:
     * - Auth Service is the ONLY service that touches JWT secrets
     * - User Service never needs to know about tokens or passwords
     * - Smaller attack surface — if User Service is compromised,
     *   the JWT secret is still safe inside Auth Service
     */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** Standard Authorization header for JWT bearer tokens */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";
}
