package org.example.telegrambot.service;

import org.example.telegrambot.entity.Book;
import org.example.telegrambot.entity.Channel;
import org.example.telegrambot.entity.User;
import org.example.telegrambot.handler.AdminHandler;
import org.example.telegrambot.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Service
public class BotService extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    private final CommandHandler commandHandler;
    private final AdminHandler adminHandler;
    private final UserService userService;
    private final BookService bookService;
    private final ChannelService channelService;

    private final Map<Long, String[]> bookCreationState = new HashMap<>();
    private final Map<Long, Boolean> searchBookState = new HashMap<>();
    private final Map<Long, Boolean> deleteBookState = new HashMap<>();
    private final Map<Long, List<Book>> userBookSearches = new HashMap<>();
    private final Map<Long, List<Integer>> userMessageIds = new HashMap<>();
    private final Map<Long, Boolean> deleteChannelState = new HashMap<>(); // Kanal o'chirish jarayonini boshqarish uchun
    private final Map<Long, Boolean> addChannelState = new HashMap<>();
    private final Map<Long, Boolean> adminUsernameState = new HashMap<>();
    private final Map<Long, Boolean> deleteAdminState = new HashMap<>();


    private final Long channelChatId = -1002330171742L;

    public BotService(CommandHandler commandHandler, AdminHandler adminHandler, UserService userService, BookService bookService,ChannelService channelService) {
        this.commandHandler = commandHandler;
        this.adminHandler = adminHandler;
        this.userService = userService;
        this.bookService = bookService;
        this.channelService = channelService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message != null) {
                String text = message.getText();
                Long chatId = message.getChatId();
                SendMessage response = null;

                if (message.hasText()) {
                    try {
                        response = handleTextMessage(update, chatId, text, response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        response = new SendMessage(chatId.toString(), "Xatolik yuz berdi. Iltimos, qayta urinib ko'ring.");
                    }
                }
                else if (message.hasDocument()) {
                    response = handleDocumentMessage(update, chatId);
                }
                if (response != null) {
                    sendResponse(response);
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery != null && callbackQuery.getMessage() != null) {
                Long chatId = callbackQuery.getMessage().getChatId();
                String callbackData = callbackQuery.getData();

                handleCallbackQuery(update, chatId, callbackData);
            }
        }
    }

    private SendMessage handleTextMessage(Update update, Long chatId, String text, SendMessage response) throws TelegramApiException {

        if (text != null && text.equalsIgnoreCase("/start")) {
            boolean allSubscribed = true;
            List<String> notSubscribedChannels = new ArrayList<>();

            try {
                List<Channel> channels = channelService.getAllChannels();
                for (Channel channel : channels) {
                    if (!checkSubscription(chatId, channel.getLink())) {
                        allSubscribed = false;
                        notSubscribedChannels.add(channel.getLink());
                    }
                }
            } catch (Exception e) {
                System.err.println("Obuna tekshirishda xatolik yuz berdi: " + e.getMessage());
                response = new SendMessage(chatId.toString(), "⚠️ Obuna tekshirishda xatolik yuz berdi. Iltimos, keyinroq urinib ko'ring.");
                return response;
            }

            if (allSubscribed) {
                try {
                    clearAllMessages(chatId);
                    String username = update.getMessage().getFrom().getUserName();
                    if (username != null) {
                        response = commandHandler.handleStartCommand(String.valueOf(chatId), username);
                    } else {
                        response = new SendMessage(chatId.toString(), "⚠️ Foydalanuvchining username'i mavjud emas.");
                    }
                } catch (Exception e) {
                    System.err.println("Start komandasi ishlashida xatolik yuz berdi: " + e.getMessage());
                    response = new SendMessage(chatId.toString(), "⚠️ Start komandasi ishlashida xatolik yuz berdi.");
                }
            } else {
                StringBuilder errorMessage = new StringBuilder("❌ Obuna bo'lmagansiz! Quyidagi kanallarga obuna bo'ling:\n\n");
                for (String channelLink : notSubscribedChannels) {
                    errorMessage.append("- ").append(channelLink).append("\n");
                }
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton checkButton = new InlineKeyboardButton();
                checkButton.setText("Tekshirish");
                checkButton.setCallbackData("check_subscription");
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                keyboard.add(Collections.singletonList(checkButton));
                keyboardMarkup.setKeyboard(keyboard);
                response = new SendMessage(chatId.toString(), errorMessage.toString());
                response.setReplyMarkup(keyboardMarkup);
            }

        }

        else if (text.equalsIgnoreCase("Statistika")) {
            long bookCount = bookService.getNumberBooks();
            long userCount = userService.getCountUsers();
            String responseText = String.format("Statistika:\nKitoblar soni: %d\nFoydalanuvchilar soni: %d", bookCount, userCount);
            response = new SendMessage(chatId.toString(), responseText);
        }
        else if (text.equalsIgnoreCase("📚Kitob qo'shish")) {
            bookCreationState.put(chatId, new String[2]);
            response = new SendMessage(chatId.toString(), "📚 Kitob qo'shish bo'limiga xush kelibsiz! Iltimos, kitob nomini kiriting.");
        } else if (bookCreationState.containsKey(chatId)) {
                response = handleBookCreation(chatId, text, null);
        } else if (text.equalsIgnoreCase("❌Kitob o'chirish")) {
            deleteBookState.put(chatId, true);
            response = new SendMessage(chatId.toString(), "❌ Iltimos, o'chiriladigan kitob id sini kiriting:");
        } else if (deleteBookState.getOrDefault(chatId, false)) {
            deleteBookState.remove(chatId);
            response = adminHandler.deleteBook(update.getMessage(), Long.valueOf(text));
        } else if (text.equalsIgnoreCase("🔍Kitob qidirish")) {
            searchBookState.put(chatId, true);
            response = new SendMessage(chatId.toString(), "🔍 Iltimos, qidirilayotgan kitob nomini kiriting:");
        } else if (searchBookState.getOrDefault(chatId, false)) {
            handleBookSearch(chatId, text);
            searchBookState.remove(chatId);
            response = new SendMessage(chatId.toString(), "🔍 Qidirish yakunlandi.");
        }
        else if (text.equalsIgnoreCase("📘Qo'llanma")) {
            response = new SendMessage(chatId.toString(), "\uD83D\uDCD8 Qo'llanma:\n" +
                    "\n" +
                    "\uD83D\uDCDA Bu bot bepul online kutubxona xizmatini taklif etadi. Kitoblarni osonlik bilan qidirib toping va yuklab oling!\n" +
                    "\n" +
                    "\uD83D\uDE80 Asosiy imkoniyatlar:\n" +
                    "- Kitoblarni tez va qulay qidirish.\n" +
                    "- Yangi va qiziqarli kitoblar har kuni yangilanadi.\n" +
                    "- Kitoblar qo'shish va o'chirish imkoniyatlari.\n" +
                    "\n" +
                    "\uD83D\uDCE9 Dasturchi:: @XDasturchi!\n" +
                    "\n" +
                    "Foydalanuvchilar uchun qulay va jozibali tajriba taqdim etish uchun botni ishlating!\n");
        } else if (text.equalsIgnoreCase("🌐Ijtimoiy tarmoqlar")) {
            response = new SendMessage(chatId.toString(), "🌐 Bizni ijtimoiy tarmoqlarda topishingiz mumkin:\n\n- Facebook: [facebook_link]\n- Instagram: [instagram_link]\n- Twitter: [twitter_link]");
        } else if (text.equalsIgnoreCase("/panel")) {
            response = handleAdminAccess(update, chatId);
        }else if (text.equalsIgnoreCase("❌Admin o'chirish")) {
            List<User> adminList = userService.getAdmins();
            StringBuilder adminsMessage = new StringBuilder("Adminlar ro'yxati:\n");

            for (User admin : adminList) {
                adminsMessage.append(String.format("ID: %d - Username: @%s\n", admin.getId(), admin.getUsername()));
            }

            response = new SendMessage(chatId.toString(), adminsMessage.toString());
            deleteAdminState.put(chatId, true);
        }
        else if (deleteAdminState.getOrDefault(chatId, false)) {
            try {
                Long adminIdToDelete = Long.valueOf(text);
                userService.removeAdmin(adminIdToDelete);
                deleteAdminState.remove(chatId);
                response = new SendMessage(chatId.toString(), "Admin muvaffaqiyatli o'chirildi.");
            } catch (NumberFormatException e) {
                response = new SendMessage(chatId.toString(), "Iltimos, haqiqiy admin ID-sini kiriting.");
            }
        }
        else if (text.equalsIgnoreCase("➕Admin qo'shish")) {
            response = new SendMessage(chatId.toString(), "Foydalanuvchilar ro'yxatini ko'rsatyapman...");
            showUsersList(chatId);
            adminUsernameState.put(chatId, true);
        } else if (adminUsernameState.getOrDefault(chatId, false)) {
            Long selectedUserId = Long.valueOf(text);
            boolean success = userService.assignAdminById(selectedUserId);

            if (success) {
                adminUsernameState.remove(chatId);
                response = new SendMessage(chatId.toString(), "Admin muvaffaqiyatli qo'shildi!");
            } else {
                response = new SendMessage(chatId.toString(), "Foydalanuvchi topilmadi yoki admin qilishda xato yuz berdi.");
            }
        }else if (text.equalsIgnoreCase("📢Kanal qo'shish")) {
            response = new SendMessage(chatId.toString(), "Iltimos, kanal usernamesini yuboring:");
            addChannelState.put(chatId, true);
        } else if (Boolean.TRUE.equals(addChannelState.get(chatId))) {
            if (text.startsWith("@")) {
                channelService.addChannel(text.substring(1));
                addChannelState.remove(chatId);
                response = new SendMessage(chatId.toString(), "✅ Kanal muvaffaqiyatli qo'shildi: " + text);
            } else {
                response = new SendMessage(chatId.toString(), "❌ Iltimos, to'g'ri formatda usernames kiriting (@username).");
            }
        }
        else if (text.equalsIgnoreCase("❌Kanal o'chirish")) {
            List<Channel> channels = channelService.getAllChannels();
            if (channels == null || channels.isEmpty()) {
                response = new SendMessage(chatId.toString(), "⚠️ Hozircha hech qanday kanal mavjud emas.");
            } else {
                StringBuilder channelsList = new StringBuilder("O'chirish uchun kanal ID sini kiriting:\n\n");
                for (Channel channel : channels) {
                    channelsList.append(String.format("ID: %d, Link: %s\n",
                            channel.getId(), channel.getLink()));
                }
                response = new SendMessage(chatId.toString(), channelsList.toString());
                deleteChannelState.put(chatId, true);
            }
        } else if (deleteChannelState.getOrDefault(chatId, false)) {
            try {
                long channelId = Long.parseLong(text);
                boolean removed = channelService.removeChannel(channelId);
                deleteChannelState.remove(chatId);
                response = removed ?
                        new SendMessage(chatId.toString(), "Kanal muvaffaqiyatli o'chirildi.") :
                        new SendMessage(chatId.toString(), "❌ Bunday ID ga ega kanal topilmadi.");
            } catch (NumberFormatException e) {
                response = new SendMessage(chatId.toString(), "❌ Iltimos, to'g'ri ID kiriting.");
            }
        }
        else if (text.equalsIgnoreCase("📜Kanallar ro'yxati")) {
            List<Channel> channels = channelService.getAllChannels();
            if (channels.isEmpty()) {
                response = new SendMessage(chatId.toString(), "Kanallar ro'yxati bo'sh.");
            } else {
                StringBuilder channelsList = new StringBuilder("📜 Kanallar ro'yxati:\n");
                for (Channel channel : channels) {
                    channelsList.append("- ").append(channel.getLink()).append("\n");
                }
                response = new SendMessage(chatId.toString(), channelsList.toString());
            }
        }
        else {
            response = new SendMessage(chatId.toString(), "Kechirasiz, bu buyruq tanilmaydi. 😅");
        }
        return response;
    }

    public void showUsersList(Long chatId) {
        List<User> users = userService.getAllUsers();
        StringBuilder usersList = new StringBuilder();
        for (User user : users) {
            usersList.append(user.getId()).append(" - ").append(user.getUsername()).append("\n");
        }
        if (usersList.length() == 0) {
            usersList.append("Hozirda foydalanuvchilar mavjud emas.");
        }

        SendMessage sendMessage = new SendMessage(chatId.toString(), usersList.toString());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private SendMessage handleBookCreation(Long chatId, String text, Document document) {
        String[] bookData = bookCreationState.get(chatId);
        SendMessage response;

        InlineKeyboardButton cancelButton = new InlineKeyboardButton("❌ Kitob yaratishni bekor qilish");
        cancelButton.setCallbackData("cancel_book_creation");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(Collections.singletonList(cancelButton)));

        if (bookData[0] == null) {
            bookData[0] = text;
            response = new SendMessage(chatId.toString(), "📄 Iltimos, kitobning PDF faylini yuboring.");
            response.setReplyMarkup(inlineKeyboardMarkup);
        } else if (document != null) {
            String fileId = document.getFileId();
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(channelChatId.toString());
            sendDocument.setDocument(new InputFile(fileId));

            try {
                var sentMessage = execute(sendDocument);
                Integer messageId = sentMessage.getMessageId();
                response = adminHandler.addBook(chatId, bookData[0], fileId, messageId);
                bookCreationState.remove(chatId);
            } catch (TelegramApiException e) {
                response = new SendMessage(chatId.toString(), "Faylni kanalga yuborishda xato yuz berdi: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            response = new SendMessage(chatId.toString(), "📤 Iltimos, PDF faylni yuboring.");
            response.setReplyMarkup(inlineKeyboardMarkup);
        }

        return response;
    }




    private SendMessage handleAdminAccess(Update update, Long chatId) {
        User user = userService.findByChatId(chatId);
        if (user != null && "admin".equalsIgnoreCase(user.getRole().toString())) {
            return adminHandler.handleAdminCommand(update.getMessage());
        } else {
            return new SendMessage(chatId.toString(), "🚫 Afsuski, sizda ushbu buyruqdan foydalanish huquqi yo'q.");
        }
    }

    private void handleBookSearch(Long chatId, String bookName) {
        List<Book> books = bookService.findBookByTitle(bookName);
        if (books != null && !books.isEmpty()) {
            userBookSearches.put(chatId, books);
            StringBuilder messageBuilder = new StringBuilder("📚 Qidirilgan kitoblar:\n");
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                messageBuilder.append(String.format("%d. %s \n", i + 1, book.getTitle()));
            }
            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            for (int i = 0; i < books.size(); i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i + 1));
                button.setCallbackData("book_" + i);
                rowList.add(Collections.singletonList(button));
            }
            keyboardMarkup.setKeyboard(rowList);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText(messageBuilder.toString());
            sendMessage.setReplyMarkup(keyboardMarkup);
            sendResponse(sendMessage);
        } else {
            SendMessage sendMessage = new SendMessage(chatId.toString(), "❌ Kechirasiz, kitob topilmadi.");
            sendResponse(sendMessage);
        }
    }

    private boolean checkSubscription(long userId, String channel) {
        try {
            var member = execute(new org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember(channel,userId));
            String status = member.getStatus();
            return status.equals("member") || status.equals("administrator") || status.equals("creator");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

//    private SendMessage sendSubscriptionReminder(Long chatId) {
//        List<Channel> channels = channelService.getAllChannels();
//        if (channels == null || channels.isEmpty()) {
//            return new SendMessage(chatId.toString(), "⚠️ Hozircha kanallarga obuna shart emas.");
//        }
//
//        StringBuilder reminderText = new StringBuilder("⚠️ Eslatma: Iltimos, quyidagi kanallarga obuna bo'ling:\n\n");
//        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//
//        for (int i = 0; i < channels.size(); i++) {
//            Channel channel = channels.get(i);
//            if (channel.getLink() == null || channel.getLink().isEmpty()) {
//                continue; // Kanal havolasi mavjud bo'lmasa, uni o'tkazib yuboramiz
//            }
//
//            reminderText.append(String.format("%d. (%s)\n", i + 1, channel.getLink()));
//            InlineKeyboardButton subscribeButton = new InlineKeyboardButton();
//            subscribeButton.setText("Kanalga obuna bo'lish");
//            subscribeButton.setUrl(channel.getLink());
//            keyboard.add(Collections.singletonList(subscribeButton));
//        }
//
//        InlineKeyboardButton checkButton = new InlineKeyboardButton();
//        checkButton.setText("Tekshirish");
//        checkButton.setCallbackData("check_subscription");
//        keyboard.add(Collections.singletonList(checkButton));
//
//        keyboardMarkup.setKeyboard(keyboard);
//
//        SendMessage reminderMessage = new SendMessage();
//        reminderMessage.setChatId(chatId.toString());
//        reminderMessage.setText(reminderText.toString());
//        reminderMessage.enableMarkdown(true); // Markdownni yoqish
//        reminderMessage.setReplyMarkup(keyboardMarkup);
//
//        return reminderMessage;
//    }

    private void handleCallbackQuery(Update update, Long chatId, String callbackData) {
         if (callbackData.equals("check_subscription")) {
            List<Channel> channels = channelService.getAllChannels();
            List<String> notSubscribedChannels = new ArrayList<>();
            for (Channel channel : channels) {
                if (!checkSubscription(chatId, channel.getLink())) {
                    notSubscribedChannels.add(channel.getLink());
                }
            }

            if (notSubscribedChannels.isEmpty()) {
                clearAllMessages(chatId);
                String username = update.getCallbackQuery().getFrom().getUserName();
                SendMessage reminderMessage = commandHandler.handleStartCommand(String.valueOf(chatId), username);
                sendResponse(reminderMessage);
            } else {
                StringBuilder errorMessage = new StringBuilder("❌ Obuna bo'lmagansiz! Quyidagi kanallarga obuna bo'ling:\n\n");
                for (String channelLink : notSubscribedChannels) {
                    errorMessage.append("- ").append(channelLink).append("\n");
                }
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton checkButton = new InlineKeyboardButton();
                checkButton.setText("Tekshirish");
                checkButton.setCallbackData("check_subscription");
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                keyboard.add(Collections.singletonList(checkButton));
                keyboardMarkup.setKeyboard(keyboard);
                sendErrorMessage(chatId, errorMessage.toString());
                sendErrorMessage(chatId, "Obuna bo'lmagan kanallarni yana tekshirish uchun quyidagi tugmani bosing.");
            }
        }

        else if (callbackData.equals("cancel_book_creation")) {
            bookCreationState.remove(chatId);
            SendMessage response = new SendMessage(chatId.toString(), "Kitob yaratish jarayoni bekor qilindi. Asosiy menyu:");
            Message message = update.getCallbackQuery().getMessage();

            if (message != null) {
                SendMessage userMenuMessage = adminHandler.handleAdminCommand(message);
                sendResponse(response);
                sendResponse(userMenuMessage);
            } else {
                sendResponse(response);
            }
        }
    }

    private void sendBookDetails(Long chatId, Book selectedBook) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText(String.format("📚 Kitob haqida:\n\n*Nom:* %s", selectedBook.getTitle()));
        response.enableMarkdown(true);
        sendResponse(response);

        Integer fileId = selectedBook.getMessageId();
        if (fileId != null) {
            try {
                CopyMessage copyMessage = new CopyMessage();
                copyMessage.setChatId(chatId.toString());
                copyMessage.setFromChatId(channelChatId);
                copyMessage.setMessageId(fileId);
                execute(copyMessage);
            } catch (TelegramApiException e) {
                sendErrorMessage(chatId, "❌ Fayl yuborishda xato yuz berdi. Iltimos, keyinroq urinib ko'ring.");
            }
        } else {
            sendErrorMessage(chatId, "❌ Fayl ID topilmadi. Iltimos, ma'mur bilan bog'laning.");
        }
    }

    private void sendErrorMessage(Long chatId, String errorMessage) {
        SendMessage errorResponse = new SendMessage();
        errorResponse.setChatId(chatId.toString());
        errorResponse.setText(errorMessage);
        sendResponse(errorResponse);
    }

//    private boolean checkSubscription(Long chatId) {
//        List<Channel> channels = channelService.getAllChannels();
//        if (channels == null || channels.isEmpty()) {
//            return false; // Kanal ro'yxati bo'sh bo'lsa, foydalanuvchini obuna deb hisoblamaymiz
//        }
//        if (chatId == null) {
//            throw new IllegalArgumentException("Chat ID null bo'lishi mumkin emas");
//        }
//
//        for (Channel channel : channels) {
//            try {
//                if (channel.getId() == null) {
//                    continue; // Kanal ID null bo'lsa, o'tkazib yuboramiz
//                }
//
//                GetChatMember chatMember = new GetChatMember();
//                chatMember.setChatId(channel.getId().toString());
//                chatMember.setUserId(chatId);
//
//                ChatMember member = execute(chatMember);
//
//                if (member == null || !isSubscribed(member.getStatus())) {
//                    return false; // Agar foydalanuvchi obuna bo'lmasa, false qaytaramiz
//                }
//            } catch (TelegramApiException e) {
//                System.err.println("Kanalni tekshirishda xatolik yuz berdi: " + e.getMessage());
//                return false; // Agar xatolik yuz bersa, false qaytaramiz
//            }
//        }
//        return true;
//    }
//
//    private boolean isSubscribed(String status) {
//        return "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
//    }



    private void clearAllMessages(Long chatId) {
        List<Integer> messageIds = userMessageIds.get(chatId);
        if (messageIds != null) {
            for (Integer messageId : messageIds) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId.toString());
                deleteMessage.setMessageId(messageId);
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            userMessageIds.remove(chatId);
        }
    }

    private SendMessage handleDocumentMessage(Update update, Long chatId) {
        Document document = update.getMessage().getDocument();

        if (bookCreationState.containsKey(chatId)) {
            return handleBookCreation(chatId, null, document);
        } else {
            return new SendMessage(chatId.toString(), "Hujjatlarni qabul qilish faqat kitob yaratishda mavjud.");
        }
    }

    public void sendResponse(SendMessage response) {
        try {
            if (response != null) {
                Message sentMessage = execute(response);
                Long chatId = Long.parseLong(response.getChatId());
                userMessageIds.computeIfAbsent(chatId, k -> new ArrayList<>()).add(sentMessage.getMessageId());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "https://t.me/ekitobuz_bot";
    }

    @Override
    public String getBotToken() {
        return "7509152656:AAFgOccu9ecAQ9IJGtUqjuvgxbp__wY2E6M";
    }
}
