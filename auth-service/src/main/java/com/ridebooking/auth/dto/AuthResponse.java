package com.ridebooking.auth.dto;

/**
 * ============================================================
 * AUTH RESPONSE DTO
 * ============================================================
 *
 * After successful login, the server returns:
 * - token: The JWT access token (short-lived, ~15 minutes)
 * - refreshToken: A longer-lived token (~7 days) used to get
 *   a new access token without re-entering credentials
 * - userId: The user's ID, useful for the frontend
 * - email: The user's email
 */
public record AuthResponse(
    String token,
    String refreshToken,
    String userId,
    String email,
    String fullName
) {}
