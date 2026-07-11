package com.ridebooking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ============================================================
 * REGISTER REQUEST DTO
 * ============================================================
 *
 * This is what the client sends in the POST body when registering.
 * We use Jakarta Validation annotations to automatically validate
 * the input BEFORE it reaches the service layer.
 *
 * @NotBlank  — field must not be null AND must have at least one
 *              non-whitespace character
 * @Email     — validates the string looks like an email address
 * @Size      — restricts length (min/max)
 *
 * WHY VALIDATE HERE?
 * If we don't validate at the API boundary, an attacker could send
 * a 10MB string as "email" and crash the database. Always validate
 * early — at the controller/DTO level.
 *
 * We use a RECORD (Java 16+) because DTOs are simple data carriers.
 * Records automatically generate: constructor, getters, equals,
 * hashCode, toString — no boilerplate.
 */
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName
) {}
