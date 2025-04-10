package org.example.video_hosting.payload.request;

public record AuthRequest (
        String phoneNumber,
        String username,
        String password
){
}
