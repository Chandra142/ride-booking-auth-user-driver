package com.ridebooking.user.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for creating/updating a user profile.
 * Only the fields the client can edit are included.
 * userId is NOT here — it comes from the X-User-Id header.
 */
public record UserProfileRequest(
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    String phoneNumber,

    String profilePictureUrl,

    @Size(max = 255, message = "Home address must not exceed 255 characters")
    String homeAddress,

    @Size(max = 255, message = "Work address must not exceed 255 characters")
    String workAddress
) {}
