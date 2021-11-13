package net.lim.telegram.commands;

import net.lim.Application;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class AboutCommand extends BotCommand {
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        Application.sendTelegramMsg(chat.getId(),
                "CryptoFollowerBot version 0.02d (under development).\n The default retry-time is 5 minutes.\n" +
                        "Any collaboration/contribution appreciated: https://github.com/LimmychAbbil/ProjectCrT");
    }

    public AboutCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }
}
