package org.example.video_hosting.service.telegram_bot;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.File;
import org.example.video_hosting.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final FileRepository fileRepository;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.channel.id}")
    private String channelChatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    @Transactional
    public Optional<File> uploadToTelegram(MultipartFile multipartFile) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("upload_", multipartFile.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(multipartFile.getBytes());
            }

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(channelChatId);
            sendDocument.setDocument(new InputFile(tempFile));
            sendDocument.setCaption(multipartFile.getOriginalFilename());

            Message message = execute(sendDocument);

            File fileEntity = File.builder()
                    .fileName(multipartFile.getOriginalFilename())
                    .telegramFileId(message.getDocument().getFileId())
                    .telegramMessageId(message.getMessageId())
                    .build();

            return Optional.of(fileRepository.save(fileEntity));
        } catch (IOException | TelegramApiException e) {
            return Optional.empty();
        }
    }

    public Optional<String> getDownloadUrl(Integer messageId, String fileId) {
        Optional<File> fileOptional = Optional.empty();

        if (messageId != null) {
            fileOptional = Optional.ofNullable(fileRepository.findByTelegramMessageId(messageId));
        } else if (fileId != null && !fileId.isEmpty()) {
            fileOptional = fileRepository.findByTelegramFileId(fileId);
        }

        return fileOptional.flatMap(file -> {
            try {
                GetFile getFile = new GetFile(file.getTelegramFileId());
                org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);
                String downloadUrl = "https://api.telegram.org/file/bot" + botToken + "/" + telegramFile.getFilePath();
                return Optional.of(downloadUrl);
            } catch (TelegramApiException e) {
                return Optional.empty();
            }
        });
    }

    public org.telegram.telegrambots.meta.api.objects.File getTelegramFileByFileId(String fileId) throws TelegramApiException {
        GetFile getFile = new GetFile(fileId);
        return execute(getFile);
    }
}
