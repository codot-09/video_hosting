package org.example.video_hosting.payload.response;

import lombok.Builder;
import java.util.List;

@Builder
public record ResContent (
        Long id,
        String title,
        String description,
        String uploaderUsername,
        Long thumbnailId,
        Long fileId,
        long commentCount,
        long likeCount,
        String link,
        List<Long> comments
){
}
