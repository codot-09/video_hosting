package org.example.video_hosting.payload.request;

public record ContentUploadRequest (
        Long thumbnailId,
        String title,
        String description
){
}
