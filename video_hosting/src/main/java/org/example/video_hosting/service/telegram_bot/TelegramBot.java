package org.example.video_hosting.service.telegram_bot;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.File;
import org.example.video_hosting.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
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

    public Optional<File> uploadToTelegram(org.springframework.web.multipart.MultipartFile multipartFile) {
        try {
            java.io.File file = java.io.File.createTempFile("upload_", multipartFile.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(multipartFile.getBytes());
            }

            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(channelChatId);
            sendVideo.setVideo(new InputFile(file));
            sendVideo.setCaption(multipartFile.getOriginalFilename());

            Message message = execute(sendVideo);

            File fileEntity = File.builder()
                    .fileName(multipartFile.getOriginalFilename())
                    .telegramFileId(message.getVideo().getFileId())
                    .telegramMessageId(message.getMessageId())
                    .build();

            return Optional.of(fileRepository.save(fileEntity));
        } catch (IOException | TelegramApiException e) {
            return Optional.empty();
        }
    }

//    public boolean resendByMessageId(Integer messageId, String targetChatId) {
//        return fileRepository.findByTelegramMessageId(messageId)
//                .map(file -> {
//                    SendVideo sendVideo = new SendVideo();
//                    sendVideo.setChatId(targetChatId);
//                    sendVideo.setVideo(new InputFile(file.getTelegramFileId()));
//                    sendVideo.setCaption(file.getFileName());
//                    try {
//                        execute(sendVideo);
//                        return true;
//                    } catch (TelegramApiException e) {
//                        return false;
//                    }
//                }).orElse(false);
//    }

    public boolean deleteFromTelegram(Integer messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage(channelChatId, messageId);
            execute(deleteMessage);
            fileRepository.deleteByTelegramMessageId(messageId);
            return true;
        } catch (TelegramApiException e) {
            return false;
        }
    }
}
