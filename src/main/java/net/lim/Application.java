package net.lim;

import net.lim.model.taskers.Tasker;
import net.lim.model.taskers.UaTasker;
import net.lim.telegram.CryptoFollowerBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Application {

    private static Tasker observer;
    private static CryptoFollowerBot instance;

    public static void main(String[] args) {
        observer = new UaTasker();
        instance = new CryptoFollowerBot(observer);
        new Thread(observer).start();
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new CryptoFollowerBot(observer));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void sendTelegramMsg(Long authorId, String message) {
        instance.sendMsg(authorId.toString(), message);
    }
}
