package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.response.AnalyticResponse;
import org.example.video_hosting.payload.response.UserAnalytics;
import org.example.video_hosting.repository.ComplaintRepository;
import org.example.video_hosting.repository.ContentRepository;
import org.example.video_hosting.repository.UserRepository;
import org.example.video_hosting.repository.ViewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticService {
    private final ViewRepository viewRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    public ApiResponse<?> getUserAnalytics(Long userId){
        String mostViewed = viewRepository.findTopCategoryByUserId(userId);
        Integer peakViewHour = viewRepository.findPeakViewHourByUserId(userId);
        Long topUploader = viewRepository.findTopUploaderViewedByUserId(userId);

        LocalTime time = LocalTime.of(peakViewHour,0);
        return ApiResponse.ok(ResponseSuccess.fetched("User analytics"),new UserAnalytics(mostViewed,time,topUploader));
    }

    public ApiResponse<?> getAllAnalytics(){
        List<String> topCategory = contentRepository.topCategories();
        List<String> topUsers = userRepository.getTopUsersByFollowersCountLimit5();
        long countContents = contentRepository.count();
        long premiumUsers = userRepository.countByPremiumTrue();
        long bannedUsers = userRepository.countByBannedTrue();
        long totalUsers = userRepository.count();
        long totalViews = viewRepository.count();
        long totalComplaints = complaintRepository.count();

        AnalyticResponse analyticResponse = AnalyticResponse.builder()
                .topCategory(topCategory)
                .topUsers(topUsers)
                .totalContents(countContents)
                .premiumUsers(premiumUsers)
                .bannedUsers(bannedUsers)
                .totalUsers(totalUsers)
                .totalViews(totalViews)
                .totalComplaints(totalComplaints)
                .build();

        return ApiResponse.ok(ResponseSuccess.fetched("Analizlar"),analyticResponse);
    }
}
