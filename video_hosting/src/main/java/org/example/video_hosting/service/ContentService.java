package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.repository.ContentRepository;
import org.jvnet.hk2.annotations.Service;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;

    public ApiResponse<?> uploadVideo(){
        return null;
    }
}
