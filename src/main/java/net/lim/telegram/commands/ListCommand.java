package net.lim.telegram.commands;

import net.lim.Application;
import net.lim.model.Subscriber;
import net.lim.model.SubscriberImpl;
import net.lim.model.task.TaskHandler;
import net.lim.model.taskers.Tasker;
import net.lim.telegram.CryptoFollowerBot;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListCommand extends BotCommand {

    //FIXME do not pass entry bot class to the commands
    private final CryptoFollowerBot bot;

    public ListCommand(CryptoFollowerBot cryptoFollowerBot, String commandIdentifier, String description) {
        super(commandIdentifier, description);
        this.bot = cryptoFollowerBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        Application.sendTelegramMsg(user.getId(), "List of tasks for subscriber " + user.getUserName());
        Subscriber subscriber = SubscriberImpl.getSubscriber(user.getId());
        subscriber.tasksList().forEach(task -> {
            SendMessage messageReply = new SendMessage();
            messageReply.setChatId(chat.getId().toString());
            messageReply.setText("The subscribe for " + task.getCrCode() + " coin to reach " + task.getDesiredValue() + (task.isGreat() ? "+" : "-"));
            List<InlineKeyboardButton> buttonRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton("\uD83D\uDC40");
            checkButton.setCallbackData(String.format(TaskHandler.TASK_CHECK_KEY_FORMAT, task.getCrCode()));
            InlineKeyboardButton editButton = new InlineKeyboardButton("\uD83D\uDD8A");
            editButton.setCallbackData(String.format(TaskHandler.TASK_EDIT_KEY_FORMAT, task.getCrCode(), task.getDesiredValue(), task.isGreat() ? "+" : "-"));
            InlineKeyboardButton deleteButton = new InlineKeyboardButton("\u274C");
            deleteButton.setCallbackData(String.format(TaskHandler.TASK_DELETE_KEY_FORMAT, task.getCrCode(), task.getDesiredValue()));
            buttonRow.add(checkButton);
            buttonRow.add(editButton);
            buttonRow.add(deleteButton);
            messageReply.setReplyMarkup(new InlineKeyboardMarkup(Collections.singletonList(buttonRow)));
            try {
                bot.execute(messageReply);
            } catch (TelegramApiException e) {
                e.printStackTrace(); //TODO handle exception in the bot execute() method
            }
        });
    }
}
