package org.example.video_hosting.payload.request;

public record UpdateProfileRequest (
        String username,
        String bio,
        String email,
        String password,
        Long newImageId
){
}
