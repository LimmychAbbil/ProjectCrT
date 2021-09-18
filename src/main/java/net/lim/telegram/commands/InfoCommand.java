package net.lim.telegram.commands;

import net.lim.model.taskers.Tasker;
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
import java.util.List;

public class InfoCommand extends BotCommand {
    private final TelegramLongPollingCommandBot bot;
    private final Tasker observer;
    public InfoCommand(TelegramLongPollingCommandBot bot, Tasker observer, String commandIdentifier, String description) {
        super(commandIdentifier, description);
        this.bot = bot;
        this.observer = observer;
    }

    @Override
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
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
