package org.example.video_hosting.controller;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.service.AnalyticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytic")
public class AnalyticController {
    private final AnalyticService analyticService;

    @GetMapping("/all")
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(analyticService.getAllAnalytics());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAnalytics(
            @PathVariable Long userId
    ){
        return ResponseEntity.ok(analyticService.getUserAnalytics(userId));
    }
}
