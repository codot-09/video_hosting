package org.example.video_hosting.payload.response;

import lombok.Builder;
import org.example.video_hosting.entity.enums.URole;

/**
 * Response for successful login containing access and refresh tokens.
 */
@Builder
public record LoginResponse(
        Long userid,
        String accessToken,
        URole role
) {
    public static LoginResponse of(Long userId,String accessToken,URole role) {
        return new LoginResponse(userId,accessToken,role);
    }
}
