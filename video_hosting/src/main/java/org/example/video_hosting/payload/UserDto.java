package org.example.video_hosting.payload;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String username,
        String phoneNumber,
        String bio,
        String email,
        boolean isPremium,
        Long profileImage,
        int followers
) {
}