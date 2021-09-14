package net.lim.telegram;

import lombok.extern.slf4j.Slf4j;
import net.lim.Application;
import net.lim.model.SubscriberImpl;
import net.lim.model.Task;
import net.lim.model.TaskBuilder;
import net.lim.model.taskers.Tasker;
import net.lim.model.taskers.UaTasker;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CryptoFollowerBot extends TelegramLongPollingCommandBot {

    private Map<Long, TaskBuilder> taskBuilderMap = new HashMap<>();

    private Tasker observer;

    public CryptoFollowerBot(Tasker observer) {
        this.observer = observer;
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message message = update.getMessage();
        if (message == null && update.hasCallbackQuery()) {
            processInlineButtonPressed(update);
            return; //FIXME
        }
        if (TaskBuilder.isMessageAPattern(message)) {
            Task taskFromMessage = TaskBuilder.fromString(message);
            if (taskFromMessage != null) {
                sendMsg(update.getMessage().getChatId().toString(), "OK. Following for " + taskFromMessage.getCrCode() + " price.");
                observer.registerSubscriber(new SubscriberImpl(taskFromMessage));
            } else {
                sendMsg(update.getMessage().getChatId().toString(), "Error, request message should be CRCOD <price>+/-");
            }
        } else if (taskBuilderMap.get(update.getMessage().getChatId()) == null && observer.getCryptoKeys().contains(message.getText().toUpperCase())){
            TaskBuilder taskBuilder = new TaskBuilder();
            taskBuilder.withTaskAuthor(update.getMessage().getChatId());
            taskBuilder.withCrCode(message.getText().toUpperCase());
            taskBuilderMap.put(update.getMessage().getChatId(), taskBuilder);
            sendMsg(update.getMessage().getChatId().toString(), "OK. Continue creating new task. Print desired value in UAH");
        } else {
            TaskBuilder taskBuilder = taskBuilderMap.get(update.getMessage().getChatId());
            if (taskBuilder != null && taskBuilder.isReady()) {
                Task task = taskBuilder.build();
                if (task != null) {
                    sendMsg(update.getMessage().getChatId().toString(), "OK. Following for " + task.getCrCode() + " price.");
                    observer.registerSubscriber(new SubscriberImpl(task));
                    taskBuilderMap.remove(update.getMessage().getChatId());
                } else {
                    sendMsg(update.getMessage().getChatId().toString(), "Error, can't create a new task");
                }
            } else {
                if ("+".equals(update.getMessage().getText()) || "-".equals(update.getMessage().getText())) {
                    taskBuilder.withPlusOrMinus("+".equals(update.getMessage().getText()));
                    sendMsg(update.getMessage().getChatId().toString(), "Task ready. Print anything to subscribe"); //TODO a message to review + confirm/cancel
                } else {
                    try {
                        Double desiredValue = Double.parseDouble(update.getMessage().getText());
                        taskBuilder.withDesiredValue(desiredValue);

                        SendMessage messageReply = new SendMessage();
                        messageReply.setChatId(message.getChat().getId().toString());
                        messageReply.setText("Print +/- to set the corner value side reached to inform"); //TODO add + and - inline buttons
                        execute(messageReply);

                    } catch (NumberFormatException e) {
                        sendMsg(update.getMessage().getChatId().toString(), "Error, can't create a new task: desired value is not a number");
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void processInlineButtonPressed(Update update) {
        Long author = update.getCallbackQuery().getFrom().getId();
        String buttonPressed = update.getCallbackQuery().getData();
        TaskBuilder taskBuilder = new TaskBuilder();
        taskBuilder.withTaskAuthor(author);
        taskBuilder.withCrCode(buttonPressed.toUpperCase());
        taskBuilderMap.put(author, taskBuilder);
        Application.sendTelegramMsg(author, "Print the corner UAH value of the " + buttonPressed + " coin (as a digit, i.e. 1.35)");
    }

    public synchronized void sendMsg(String chatId, String s) {
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

    @Override
    public String getBotUsername() {
        return "CryptoFollowerBot";
    }

    @Override
    public void onRegister() {
        register(new BotCommand("info", "Show bot usage") {
            @Override //262538555
            public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
                SendMessage message = new SendMessage();
                message.setChatId(chat.getId().toString());
                message.setText("Select the crypto currency to follow price");

                // Create ReplyKeyboardMarkup object
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                // Create the keyboard (list of keyboard rows)
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();


                // Create a keyboard row
                List<InlineKeyboardButton> row = new ArrayList<>();

                for (String key : observer.getCryptoKeys()) {
                    InlineKeyboardButton button = new InlineKeyboardButton(key);
                    button.setCallbackData(key);
                    button.setSwitchInlineQueryCurrentChat(key);
                    row.add(button);
                    if (row.size() == 5) {
                        keyboard.add(row);
                        row = new ArrayList<>();
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
                Application.sendTelegramMsg(chat.getId(),
                        "You can subscribe for a crypto value on btc-trade.com.ua. Request code is CRCOD <price>+/-" +
                                "\nIn example: BTC 150000.05+");
            }
        });

        register(new BotCommand("about", "Bot version, contacts") {
            @Override
            public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
                Application.sendTelegramMsg(chat.getId(),
                        "CryptoFollewerBot version 0.01f (under development).\n The default into-time is 5 minutes.\n" +
                                "Any collaboration/contribution appreciated: https://github.com/LimmychAbbil/ProjectCrT");
            }
        });
    }


}
