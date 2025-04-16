package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Content;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.View;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.repository.ContentRepository;
import org.example.video_hosting.repository.UserRepository;
import org.example.video_hosting.repository.ViewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.example.video_hosting.payload.response.UserAnalytics;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ViewService {
    private final UserRepository userRepository;
    private final ViewRepository viewRepository;
    private final ContentRepository contentRepository;

    public ApiResponse<?> viewContent(Long contentId,User user,boolean isLike){
        Optional<Content> content = contentRepository.findById(contentId);
        if(content.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Content"));
        }

        View view = View.builder()
                .contentId(content.get().getId())
                .userId(user.getId())
                .isLike(i)
                .build();

        viewRepository.save(view);
        return ApiResponse.ok(ResponseSuccess.operationDone("Content view"));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoClearViews(){
        List<View> removeViews = viewRepository.findViewsOlderThan7Days();
        viewRepository.deleteAll(removeViews);
    }
}
