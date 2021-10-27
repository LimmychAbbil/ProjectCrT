package net.lim.telegram.commands;

import net.lim.Application;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class ListCommand extends BotCommand {

    public ListCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        Application.sendTelegramMsg(user.getId(), "Here would be a list of tasks for subscriber " + user.getId());
    }
}
