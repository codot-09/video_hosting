package org.example.video_hosting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Content;
import org.example.video_hosting.entity.File;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.View;
import org.example.video_hosting.entity.enums.ContentAuditory;
import org.example.video_hosting.entity.enums.ContentCategory;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.request.ContentUploadRequest;
import org.example.video_hosting.payload.response.UserAnalytics;
import org.example.video_hosting.repository.ContentRepository;
import org.example.video_hosting.repository.FileRepository;
import org.example.video_hosting.repository.UserRepository;
import org.example.video_hosting.repository.ViewRepository;
import org.example.video_hosting.service.telegram_bot.TelegramBot;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;
    private final ViewRepository viewRepository;
    private final AnalyticService analyticService;

    @Transactional
    public ApiResponse<?> uploadContent(User user, MultipartFile multipartFile, ContentCategory category, ContentAuditory auditory, ContentUploadRequest contentUploadRequest){
        Optional<File> file = fileRepository.findById(contentUploadRequest.thumbnailId());
        if(file.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("File"));
        }

        Optional<File> contentFile = telegramBot.uploadToTelegram(multipartFile);
        if(contentFile.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("File"));
        }

        Content content = Content.builder()
                .title(contentUploadRequest.title())
                .description(contentUploadRequest.description())
                .status(auditory)
                .uploader(user)
                .category(category)
                .thumbnail(file.get())
                .contentFile(contentFile.get())
                .build();

        contentRepository.save(content);

        return ApiResponse.ok(ResponseSuccess.uploaded(),contentFile.get().getId());
    }

    public ResponseEntity<byte[]> downloadFile(Long fileId) {
        try {
            Optional<File> optionalFile = fileRepository.findById(fileId);
            if (optionalFile.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            File file = optionalFile.get();
            org.telegram.telegrambots.meta.api.objects.File telegramFile =
                    telegramBot.getTelegramFileByFileId(file.getTelegramFileId());

            String fileUrl = "https://api.telegram.org/file/bot" +
                    telegramBot.getBotToken() + "/" + telegramFile.getFilePath();

            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + file.getFileName());

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
