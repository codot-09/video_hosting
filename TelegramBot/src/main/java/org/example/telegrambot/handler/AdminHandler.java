package org.example.telegrambot.handler;

import org.example.telegrambot.entity.Book;
import org.example.telegrambot.service.BookService;
import org.example.telegrambot.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminHandler {

    private final BookService bookService;
    private final UserService userService;

    public AdminHandler(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    public SendMessage addBook(Long chatId, String title, String fileId, Integer messageId) throws Exception {
        Book book = new Book();
        book.setTitle(title);
        book.setFileId(fileId);
        book.setMessageId(messageId);

        bookService.save(book);

        return createSendMessage(String.valueOf(chatId), "Kitob muvaffaqiyatli qo'shildi.");
    }

    public SendMessage deleteBook(Message message, Long id) {
        bookService.deleteBookById(id);
        return createSendMessage(message.getChatId().toString(), "Kitob muvaffaqiyatli o'chirildi!");
    }

    public SendMessage handleAdminCommand(Message message) {
        String chatId = message.getChatId().toString();
        ReplyKeyboardMarkup keyboardMarkup = createAdminKeyboard();
        return createSendMessage(chatId, "Admin panel\nQuyidagi buyruqlardan birini tanlang:", keyboardMarkup);
    }

    public ReplyKeyboardMarkup createAdminKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(createKeyboardRow("📚Kitob qo'shish", "❌Kitob o'chirish"));
        keyboard.add(createKeyboardRow("🔍Kitob qidirish", "Statistika"));
        keyboard.add(createKeyboardRow("➕Admin qo'shish", "❌Admin o'chirish"));
        keyboard.add(createKeyboardRow("📢Kanal qo'shish", "❌Kanal o'chirish"));
        keyboard.add(createKeyboardRow("📜Kanallar ro'yxati"));

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private KeyboardRow createKeyboardRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for (String button : buttons) {
            row.add(new KeyboardButton(button));
        }
        return row;
    }

    private SendMessage createSendMessage(String chatId, String text) {
        return createSendMessage(chatId, text, null);
    }

    private SendMessage createSendMessage(String chatId, String text, ReplyKeyboardMarkup replyMarkup) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        if (replyMarkup != null) {
            sendMessage.setReplyMarkup(replyMarkup);
        }
        return sendMessage;
    }
}
