package org.example.video_hosting.payload.response;

import lombok.Builder;

/**
 * Response for successful login containing access and refresh tokens.
 */
@Builder
public record LoginResponse(
        Long userid,
        String accessToken
) {
    public static LoginResponse of(Long userId,String accessToken) {
        return new LoginResponse(userId,accessToken);
    }
}
