package net.lim.telegram;

import lombok.extern.slf4j.Slf4j;
import net.lim.model.SubscriberImpl;
import net.lim.model.Task;
import net.lim.model.taskers.Tasker;
import net.lim.model.taskers.UaTasker;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class CryptoFollowerBot extends TelegramLongPollingBot {
    private static Tasker observer;
    private static CryptoFollowerBot instance;

    @Override
    public String getBotToken() {
        return null;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.getText().startsWith("/")) {
            processCommand(message);
        } else {
            Task taskFromMessage = Task.fromString(message);
            if (taskFromMessage != null) {
                sendMsg(update.getMessage().getChatId().toString(), "OK. Following for " + taskFromMessage.getCrCode() + " price.");
                observer.registerSubscriber(new SubscriberImpl(taskFromMessage));
            } else {
                sendMsg(update.getMessage().getChatId().toString(), "Error, request message should be CRCOD <price>+/-");
            }
        }
    }

    private void processCommand(Message message) {
        switch (message.getText()) {
            case "/start": sendMsg(message.getChatId(),
                    "You can subscribe for a crypto value on btc-trade.com.ua. Request code is CRCOD <price>+/-" +
                            "\nIn example: BTC 150000.05+");
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

}
