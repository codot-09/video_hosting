package org.example.video_hosting.payload.request;

public record ReqComplaint (
        Long contentId,
        Long userId,
        String description
){
}
