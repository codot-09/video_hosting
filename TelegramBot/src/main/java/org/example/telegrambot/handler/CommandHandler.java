package org.example.telegrambot.handler;

import org.example.telegrambot.entity.User;
import org.example.telegrambot.service.BookService;
import org.example.telegrambot.service.BotService;
import org.example.telegrambot.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommandHandler {

    private final UserService userService;
    private final BookService bookService;
    private final BotService botService;

    public CommandHandler(UserService userService, BookService bookService, @Lazy BotService botService) {
        this.userService = userService;
        this.bookService = bookService;
        this.botService = botService;
    }

    public SendMessage sendGreetingMessage(String chatId,String username) {
        userService.saveUserIfNotExists(chatId,username);
        String responseText =
                "👋 Xush kelibsiz! Bizning kutubxonamizga qo'shildingiz. 🎉\n\n" +
                "Botimizdan foydalanish uchun shunchaki variantni tanlang! 😊📖";

        SendMessage welcomeMessage = new SendMessage(chatId, responseText);

        return welcomeMessage;
    }

    public SendMessage sendUserMenu(String chatId) {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔍Kitob qidirish"));
        row1.add(new KeyboardButton("📘Qo'llanma"));
        keyboardRows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🌐Ijtimoiy tarmoqlar"));
        keyboardRows.add(row2);

        keyboardMarkup.setKeyboard(keyboardRows);

        SendMessage userMenuMessage = new SendMessage(chatId, "Iltimos, quyidagi variantlardan birini tanlang:");
        userMenuMessage.setReplyMarkup(keyboardMarkup);

        return userMenuMessage;
    }

    public SendMessage handleStartCommand(String chatId,String username) {
        SendMessage greetingMessage = sendGreetingMessage(chatId,username);
        SendMessage userMenuMessage = sendUserMenu(chatId);

        botService.sendResponse(greetingMessage);
        return userMenuMessage;
    }
}
