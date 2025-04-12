package org.example.video_hosting.payload.response;

import lombok.Builder;
import java.util.List;

@Builder
public record AnalyticResponse (
        List<String> topCategory,
        List<String> topUsers,
        long totalContents,
        long premiumUsers,
        long bannedUsers,
        long totalUsers,
        long totalViews,
        long totalComplaints
){
}
