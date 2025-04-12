package org.example.video_hosting.controller;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.enums.ContentAuditory;
import org.example.video_hosting.entity.enums.ContentCategory;
import org.example.video_hosting.payload.request.ContentUploadRequest;
import org.example.video_hosting.security.CurrentUser;
import org.example.video_hosting.service.ContentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadContent(
            @CurrentUser User uploader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") ContentCategory category,
            @RequestParam("auditory") ContentAuditory auditory,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("thumbnail") Long thumbnailId
    ) {
        try {
            ContentUploadRequest uploadRequest = new ContentUploadRequest(thumbnailId, title, description);
            return ResponseEntity.ok(contentService.uploadContent(uploader, file, category, auditory, uploadRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid metadata format");
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        return contentService.downloadFile(fileId);
    }
}
