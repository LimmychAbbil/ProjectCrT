package net.lim.telegram;

import lombok.extern.slf4j.Slf4j;
import net.lim.model.SubscriberImpl;
import net.lim.model.Task;
import net.lim.model.taskers.Tasker;
import net.lim.model.taskers.UaTasker;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CryptoFollowerBot extends TelegramLongPollingCommandBot {
    private static Tasker observer;
    private static CryptoFollowerBot instance;

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message message = update.getMessage();
        System.out.println(message.isCommand());
        //TODO if regex matches command - process, else - construct from several messages
        Task taskFromMessage = Task.fromString(message);
        if (taskFromMessage != null) {
            sendMsg(update.getMessage().getChatId().toString(), "OK. Following for " + taskFromMessage.getCrCode() + " price.");
            observer.registerSubscriber(new SubscriberImpl(taskFromMessage));
        } else {
            sendMsg(update.getMessage().getChatId().toString(), "Error, request message should be CRCOD <price>+/-");
        }

    }

    private synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
            e.printStackTrace();
        }
    }

    public static void sendMsg(Long authorId, String message) {
        instance.sendMsg(authorId.toString(), message);
    }

    @Override
    public String getBotUsername() {
        return "CryptoFollowerBot";
    }

    public static void main(String[] args) {
        instance = new CryptoFollowerBot();
        observer = new UaTasker();
        new Thread(observer).start();
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new CryptoFollowerBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegister() {
        register(new BotCommand("info", "Show bot usage") {
            @Override
            public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
                SendMessage message = new SendMessage();
                message.setChatId(chat.getId().toString());
                message.setText("Select the crypto currency to follow price");

                // Create ReplyKeyboardMarkup object
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                // Create the keyboard (list of keyboard rows)
                List<KeyboardRow> keyboard = new ArrayList<>();


                // Create a keyboard row
                KeyboardRow row = new KeyboardRow();

                for (String key : observer.getCryptoKeys()) {
                    row.add(key);

                    if (row.size() == 5) {
                        keyboard.add(row);
                        row = new KeyboardRow();
                    }
                }
                if (!row.isEmpty()) {
                    keyboard.add(row);
                }
                // Set the keyboard to the markup
                keyboardMarkup.setKeyboard(keyboard);
                // Add it to the message
                message.setReplyMarkup(keyboardMarkup);

                try {
                    CryptoFollowerBot.this.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });

        register(new BotCommand("start", "") {
            @Override
            public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
                sendMsg(chat.getId(),
                        "You can subscribe for a crypto value on btc-trade.com.ua. Request code is CRCOD <price>+/-" +
                                "\nIn example: BTC 150000.05+");
            }
        });
    }


}
