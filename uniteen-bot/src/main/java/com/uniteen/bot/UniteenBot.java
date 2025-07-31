package com.uniteen.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "deprecation", "unused" })
public class UniteenBot extends TelegramLongPollingBot {

    private static final String CHANNEL_USERNAME = "t.me/uniteenuz"; // <-- Kanal username
    private static final long ADMIN_ID = 1943895887; // <-- Admin Telegram ID

    @Override
    public String getBotUsername() {
        return "UniteenBot"; // Bot username
    }

    @Override
    public String getBotToken() {
        return "7844397071:AAFFcpZoQupuh4GiS_PexUmu7xpnpbWnmNA"; // <-- Bot token
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            if (message.hasText()) {
                String text = message.getText();

                if (text.equals("/start")) {
                    if (!isUserSubscribed(chatId)) {
                        sendSubscribeMessage(chatId);
                        return;
                    }

                    requestPhoneNumber(chatId);
                } else if (text.equals("Price") || text.equals("Information") ||
                        text.equals("Taklif va Murojaat") || text.equals("Courses")) {
                    sendMessage(chatId, "Siz tanlagan bo‘lim: " + text);
                } else {
                    sendMessage(chatId, "Iltimos, quyidagi tugmalardan foydalaning.");
                }

            } else if (message.hasContact()) {
                Contact contact = message.getContact();
                sendMessageToAdmin("📞 Telefon: " + contact.getPhoneNumber() + "\n👤 Ism: " + contact.getFirstName(), chatId);
                requestLocation(chatId);
            } else if (message.hasLocation()) {
                Location location = message.getLocation();
                sendMessageToAdmin("📍 Geolokatsiya: https://maps.google.com/?q=" +
                        location.getLatitude() + "," + location.getLongitude(), chatId);
                showMainMenu(chatId);
            }
        }
    }

    private boolean isUserSubscribed(Long chatId) {
        try {
            ChatMember member = execute(new org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember(CHANNEL_USERNAME, chatId));
            return member.getStatus().equals("member") || member.getStatus().equals("administrator") || member.getStatus().equals("creator");
        } catch (TelegramApiException e) {
            return false;
        }
    }

    private void sendSubscribeMessage(Long chatId) {
        String msg = "📢 Botdan foydalanish uchun avval " + CHANNEL_USERNAME + " kanaliga obuna bo‘ling.\nObuna bo‘lganingizdan so‘ng /start buyrug‘ini qayta yuboring.";
        sendMessage(chatId, msg);
    }

    private void requestPhoneNumber(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📲 Iltimos, telefon raqamingizni yuboring:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("📞 Raqamni yuborish");
        button.setRequestContact(true);
        row.add(button);
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void requestLocation(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📍 Iltimos, yashash manzilingizni yuboring:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("📍 Manzilni yuborish");
        button.setRequestLocation(true);
        row.add(button);
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAdmin(String text, Long fromUserId) {
        SendMessage message = new SendMessage();
        message.setChatId(ADMIN_ID);
        message.setText("🆕 Yangi ma’lumotlar:\nFrom: " + fromUserId + "\n" + text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Quyidagilardan birini tanlang:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Price");
        row1.add("Information");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Taklif va Murojaat");
        row2.add("Courses");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Main method (optional if you're launching the bot from main class)
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new UniteenBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
