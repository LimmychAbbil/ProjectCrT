package net.lim.telegram;

import lombok.extern.slf4j.Slf4j;
import net.lim.Application;
import net.lim.model.SubscriberImpl;
import net.lim.model.Task;
import net.lim.model.TaskBuilder;
import net.lim.model.taskers.Tasker;
import net.lim.telegram.commands.AboutCommand;
import net.lim.telegram.commands.InfoCommand;
import net.lim.telegram.commands.StartCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

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
        String messageText = (message == null) ? null : message.getText();
        if (message == null && update.hasCallbackQuery()) {
            processInlineButtonPressed(update);
        } else if (messageText != null) {
            processTextMessage(message);
        }
    }

    private void processInlineButtonPressed(Update update) {
        Long author = update.getCallbackQuery().getFrom().getId();
        String buttonPressed = update.getCallbackQuery().getData();
        if (taskBuilderMap.get(author) == null) {
            TaskBuilder taskBuilder = new TaskBuilder();
            taskBuilder.withTaskAuthor(author);
            taskBuilder.withCrCode(buttonPressed.toUpperCase());
            taskBuilderMap.put(author, taskBuilder);
            Application.sendTelegramMsg(author, "Print the corner UAH value of the " + buttonPressed + " coin (as a digit, i.e. 1.35)");
        } else {
            TaskBuilder taskBuilder = taskBuilderMap.get(author);
            taskBuilder.withPlusOrMinus("+".equals(buttonPressed));
            sendMsg(author.toString(), "Task ready. Print anything to subscribe");
        }
    }

    private void processTextMessage(Message message) { //TODO refactor
        String messageText = message.getText();
        if (TaskBuilder.isMessageAPattern(messageText)) {
            Task taskFromMessage = TaskBuilder.fromString(message);
            if (taskFromMessage != null) {
                sendMsg(message.getChatId().toString(), "OK. Following for " + taskFromMessage.getCrCode() + " price.");
                observer.registerSubscriber(new SubscriberImpl(taskFromMessage));
            } else {
                sendMsg(message.getChatId().toString(), "Error, request message should be CRCOD <price>+/-");
            }
        } else if (taskBuilderMap.get(message.getChatId()) == null && observer.getCryptoKeys().contains(messageText.toUpperCase())) {
            TaskBuilder taskBuilder = new TaskBuilder();
            taskBuilder.withTaskAuthor(message.getChatId());
            taskBuilder.withCrCode(messageText.toUpperCase());
            taskBuilderMap.put(message.getChatId(), taskBuilder);
            sendMsg(message.getChatId().toString(), "OK. Continue creating new task. Print desired value in UAH");
        } else {
            TaskBuilder taskBuilder = taskBuilderMap.get(message.getChatId());
            if (taskBuilder != null && taskBuilder.isReady()) {
                Task task = taskBuilder.build();
                if (task != null) {
                    sendMsg(message.getChatId().toString(), "OK. Following for " + task.getCrCode() + " price.");
                    observer.registerSubscriber(new SubscriberImpl(task));
                    taskBuilderMap.remove(message.getChatId());
                } else {
                    sendMsg(message.getChatId().toString(), "Error, can't create a new task");
                }
            } else {
                try {
                    Double desiredValue = Double.parseDouble(messageText);
                    taskBuilder.withDesiredValue(desiredValue);

                    SendMessage messageReply = new SendMessage();
                    messageReply.setChatId(message.getChat().getId().toString());
                    messageReply.setText("Press +/- to set the corner value side reached to inform");
                    List<InlineKeyboardButton> buttonRow = new ArrayList<>();
                    InlineKeyboardButton plusButton = new InlineKeyboardButton("+");
                    plusButton.setCallbackData("+");
                    InlineKeyboardButton minusButton = new InlineKeyboardButton("-");
                    minusButton.setCallbackData("-");
                    buttonRow.add(plusButton);
                    buttonRow.add(minusButton);
                    messageReply.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(buttonRow)));
                    execute(messageReply);

                } catch (NumberFormatException e) {
                    sendMsg(message.getChatId().toString(), "Error, can't create a new task: desired value is not a number");
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: {}", e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "CryptoFollowerBot";
    }

    @Override
    public void onRegister() {
        register(new InfoCommand(this, observer, "info", "Show bot usage"));
        register(new StartCommand("start", ""));
        register(new AboutCommand("about", "Bot version, contacts"));
    }
}
