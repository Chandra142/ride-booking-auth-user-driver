package com.ridebooking.user.dto;

import com.ridebooking.user.entity.UserProfile;

/**
 * DTO for returning profile data.
 *
 * We use a separate response DTO (not the entity) because:
 * 1. We control what data is exposed (e.g., we might exclude
 *    internal fields like the auto-increment "id")
 * 2. We can add computed fields without polluting the entity
 * 3. Changes to the entity don't break the API contract
 */
public record UserProfileResponse(
    String userId,
    String fullName,
    String phoneNumber,
    String profilePictureUrl,
    String homeAddress,
    String workAddress,
    String createdAt
) {
    /**
     * Factory method that converts an Entity into a Response DTO.
     * This pattern keeps the conversion logic in one place.
     */
    public static UserProfileResponse fromEntity(UserProfile profile) {
        return new UserProfileResponse(
            profile.getUserId(),
            profile.getFullName(),
            profile.getPhoneNumber(),
            profile.getProfilePictureUrl(),
            profile.getHomeAddress(),
            profile.getWorkAddress(),
            profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null
        );
    }
}
