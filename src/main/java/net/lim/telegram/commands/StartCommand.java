package net.lim.telegram.commands;

import net.lim.Application;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class StartCommand extends BotCommand {

    public StartCommand(String command, String description) {
        super(command, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        Application.sendTelegramMsg(chat.getId(),
                "You can subscribe for a crypto value on btc-trade.com.ua. Request code is CRCOD <price>+/-" +
                        "\nIn example: BTC 150000.05+");
    }
}
