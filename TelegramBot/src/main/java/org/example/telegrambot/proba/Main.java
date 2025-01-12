package org.example.telegrambot.proba;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main extends TelegramLongPollingBot {

    public static void main(String[] args) {
        Main main = new Main();
        main.getChatId();
    }

    public void getChatId() {
        SendMessage message = new SendMessage();
        message.setChatId("@ebooky_test"); // Kanal username'si
        message.setText("Bu test xabari");

        try {
            // Xabarni yuborish va qaytgan ma'lumotni olish
            Message sentMessage = execute(message);
            Long chatId = sentMessage.getChatId(); // Kanalning chat_id sini olish
            System.out.println("Kanal Chat ID: " + chatId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "@ekitobuz_bot"; // Bot username'ini kiritish
    }

    @Override
    public String getBotToken() {
        return "7509152656:AAFgOccu9ecAQ9IJGtUqjuvgxbp__wY2E6M"; // Bot tokeningiz
    }

    @Override
    public void onUpdateReceived(org.telegram.telegrambots.meta.api.objects.Update update) {
        // Bu yerda xabarlar qabul qilish uchun kod yoziladi (hozircha kerak emas)
    }
}
