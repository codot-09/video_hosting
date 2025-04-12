package org.example.video_hosting.payload.response;

import java.time.LocalTime;

public record UserAnalytics (
        String mostViewedContentTypes,
        LocalTime mostViewedTime,
        Long mostViewedUserId
){
}
