package org.uniteen;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

@SuppressWarnings("deprecation")
public class UniteenBot extends TelegramLongPollingBot {

    private final long ADMIN_ID = 1943895887;
    private final String CHANNEL_USERNAME = "@uniteenuz";
    private final Map<Long, Boolean> waitingForCourseInput = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "UniteenBot";
    }

    @Override
    public String getBotToken() {
        return "7844397071:AAFFcpZoQupuh4GiS_PexUmu7xpnpbWnmNA";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message msg = update.getMessage();
            long chatId = msg.getChatId();

            if (waitingForCourseInput.getOrDefault(chatId, false) && msg.hasText()) {
                forwardToAdmin(chatId, "Kursga yozilish so'rovi: " + msg.getText(), msg.getFrom());
                sendMessage(chatId, "✅ Kursga yozilish so'rovingiz qabul qilindi, Tez orada siz bilan bog'lanishadi.");
                waitingForCourseInput.remove(chatId);
                return;
            }

            if (msg.hasText()) {
                String text = msg.getText();

                switch (text) {
                    case "/start":
                        if (isUserSubscribed(chatId)) {
                            sendPhoneRequest(chatId);
                        } else {
                            sendMessage(chatId, "Botdan foydalanish uchun quyidagi kanalga obuna bo‘ling: " + CHANNEL_USERNAME);
                        }
                        break;
                    case "Information":
                        sendMessage(chatId, "📌Uniteen Academy o’quv markazi\r\n" + //
                                                        "rasmiy kanali ✅\r\n" + //
                                                        "🔵 General English\r\n" + //
                                                        "🔵 MATEMATIKA\r\n" + //
                                                        "🔵 Multi Level \r\n" + //
                                                        "🔵 RUS TILI\r\n" + //
                                                        "🔵 IELTS   \r\n" + //
                                                        "🔵 SAT \r\n" + //
                                                        "🔵 IT\r\n" + //
                                                        "\r\n" + //
                                                        "Murojaat uchun : +998337033032 yoki @uniteenacademyuz\r\n" + //
                                                        "\r\n" + //
                                                        "Manzil: Toshkent еshahri, Yashnobod tumani , Qorasuv ko’chasi 35");
                        break;
                    case "Courses":
                        sendCourseButtons(chatId);
                        break;
                    case "Taklif va Murojaat":
                        sendMessage(chatId, "✉️ Iltimos, o‘z taklif yoki murojaatingizni yozib yuboring. Adminlar 24 soat ichida sizga javob qaytaradi.");
                        break;
                    default:
                        if (isUserSubscribed(chatId)) {
                            forwardToAdmin(chatId, msg.getText(), msg.getFrom());
                            sendMessage(chatId, "✅ Murojaatingiz qabul qilindi.");
                        }
                        break;
                }

            } else if (msg.hasContact()) {
                Contact contact = msg.getContact();
                sendMessage(ADMIN_ID, "📞 Telefon raqami: " + contact.getPhoneNumber() + "\n👤 Ism: " + contact.getFirstName());
                sendLocationRequest(chatId);
            } else if (msg.hasLocation()) {
                Location loc = msg.getLocation();
                String locationUrl = "https://maps.google.com/?q=" + loc.getLatitude() + "," + loc.getLongitude();
                String text = "📍 Yangi joylashuv:\n" + locationUrl;
                sendMessage(ADMIN_ID, text);
                showMainMenu(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void sendCourseButtons(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("\uD83D\uDCD6 Quyidagi kurslardan birini tanlang:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Arrays.asList(createButton("\uD83C\uDF1F English Standard", "english_standard")));
        rows.add(Arrays.asList(createButton("\uD83D\uDD25 English Intensive", "english_intensive")));
        rows.add(Arrays.asList(createButton("\uD83C\uDF93 President Maktab", "president")));
        rows.add(Arrays.asList(createButton("\u270D\uFE0F SAT", "sat")));
        rows.add(Arrays.asList(createButton("\u2797 Matematika", "matematika")));
        rows.add(Arrays.asList(createButton("\uD83C\uDDF7\uD83C\uDDFA Rus tili", "russian")));
        rows.add(Arrays.asList(createButton("\uD83D\uDCBB IT", "it")));

        markup.setKeyboard(rows);
        msg.setReplyMarkup(markup);
        executeSafely(msg);
    }

    private InlineKeyboardButton createButton(String label, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(label)
                .callbackData(callbackData)
                .build();
    }

    private void handleCallback(CallbackQuery callback) {
    long chatId = callback.getMessage().getChatId();
    String data = callback.getData();

    // Agar tugma kursga yozilish tugmasi bo‘lsa
    if (data.startsWith("register_course_")) {
        String courseName = data.replace("register_course_", "").replace("_", " ");
        waitingForCourseInput.put(chatId, true);
        sendMessage(chatId, "✍️ \"" + courseName + "\" kursga yozilish uchun sizdan talab qilinadi: " + "Telefon raqam:*\n" + "Kurs nomi:*\n ");
        return;
    }

    // Kurslar haqida ma'lumot beruvchi tugmalar
    String response = switch (data) {
        case "english_standard" -> "\uD83C\uDF1F *English Standard kurslari:*\n\n" +
                "\uD83D\uDCDA Beginner – Elementary ➤ *650,000 so'm*\n" +
                "\uD83D\uDCDA Intermediate – Upper-Intermediate ➤ *650,000 so'm*\n" +
                "\uD83D\uDCDA Pre-IELTS – Advanced ➤ *750,000 so'm*\n" +
                "\uD83D\uDCDA IELTS - Multi Level ➤ *750,000 so'm*";
        case "english_intensive" -> "\uD83D\uDD25 *English Intensive:*\n\n" +
                "Beginner – Elementary ➤ *750,000 so'm*\n" +
                "Pre-Intermediate – Intermediate ➤ *750,000 so'm*\n" +
                "Upper-Intermediate ➤ *750,000 so'm*\n" +
                "IELTS Boost ➤ *900,000 so'm*\n" +
                "IELTS 7+ ➤ *900,000 so'm*";
        case "president" -> "\uD83C\uDF93 *President maktab tayyorlov:*\n\n" +
                "Matematika ➤ *750,000 so'm*\n" +
                "Ingliz tili ➤ *750,000 so'm*\n" +
                "Matematika + Ingliz tili ➤ *1,400,000 so'm*";
        case "sat" -> "\u270D\uFE0F *SAT kurslari:*\n\n" +
                "Matematika ➤ *750,000 so'm*\n" +
                "Ingliz tili ➤ *750,000 so'm*\n" +
                "Matematika + Ingliz tili ➤ *1,400,000 so'm*";
        case "matematika" -> "\u2797 *Matematika:*\n\nUmumiy ➤ *650,000 so'm*";
        case "russian" -> "\uD83C\uDDF7\uD83C\uDDFA *Rus tili:*\n\nOddiy ➤ *650,000 so'm*\nIntensive ➤ *1,200,000 so'm*";
        case "it" -> "\uD83D\uDCBB *IT:*\n\nIT ➤ *950,000 so'm*\nIT Kids (10–14 yosh) ➤ *750,000 so'm*";
        default -> null;
    };

    if (response != null) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(response);
        msg.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Kursga yozilish");
        registerButton.setCallbackData("register_course_" + data);
        markup.setKeyboard(List.of(List.of(registerButton)));

        msg.setReplyMarkup(markup);
        executeSafely(msg);
    }
}

    private void forwardToAdmin(long userId, String message, User fromUser) {
        String fullMsg = "✉️ Yangi murojaat/taklif:\n\n" +
                message +
                "\n\n👤 Yuboruvchi: @" + (fromUser.getUserName() != null ? fromUser.getUserName() : fromUser.getFirstName());
        sendMessage(ADMIN_ID, fullMsg);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        executeSafely(msg);
    }

    private void sendPhoneRequest(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("\uD83D\uDCF2 Iltimos, telefon raqamingizni yuboring:");

        KeyboardButton phoneButton = new KeyboardButton("\uD83D\uDCF1 Raqamni yuborish");
        phoneButton.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(phoneButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);

        msg.setReplyMarkup(markup);
        executeSafely(msg);
    }

    private void sendLocationRequest(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("\uD83D\uDCCD Endi, iltimos, joylashuvingizni yuboring:");

        KeyboardButton locationButton = new KeyboardButton("\uD83D\uDCCD Joylashuvni yuborish");
        locationButton.setRequestLocation(true);
        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);

        msg.setReplyMarkup(markup);
        executeSafely(msg);
    }

    private void showMainMenu(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Asosiy menyu:");

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Information"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Taklif va Murojaat"));
        row2.add(new KeyboardButton("Courses"));

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);

        msg.setReplyMarkup(markup);
        executeSafely(msg);
    }

    private boolean isUserSubscribed(long userId) {
        try {
            GetChatMember getChatMember = new GetChatMember(CHANNEL_USERNAME, userId);
            ChatMember member = execute(getChatMember);
            String status = member.getStatus();
            return status.equals("member") || status.equals("administrator") || status.equals("creator");
        } catch (TelegramApiException e) {
            return false;
        }
    }

    private void executeSafely(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new UniteenBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}