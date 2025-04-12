package org.example.video_hosting.service.telegram_bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class TelegramChatIdFetcher {

    public static void main(String[] args) {
        String botToken = "7597765571:AAGzNn1pSh49v8mX5GpKNq2KW0MqgY2aUII";
        String channelUsername = "@out_base"; // Masalan: "@video_hosting_channel"

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            DefaultAbsSender sender = new DefaultAbsSender(new DefaultBotOptions()) {
                @Override
                public String getBotToken() {
                    return botToken;
                }
            };

            GetChat getChat = new GetChat(channelUsername);
            Chat chat = sender.execute(getChat);
            System.out.println("Chat ID: " + chat.getId());
        } catch (TelegramApiException e) {
            System.err.println("Xatolik: " + e.getMessage());
        }
    }
}
