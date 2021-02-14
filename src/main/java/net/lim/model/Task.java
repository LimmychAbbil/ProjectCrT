package net.lim.model;


import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task {

    private final String crCode;
    private final double desiredValue;
    private final boolean great;

    @Getter
    @Setter
    private Long taskAuthorId;

    public Task(String crCode, double desiredValue, boolean great) {
        this.crCode = crCode;
        this.desiredValue = desiredValue;
        this.great = great;
    }

    public static Task fromString(Message message) {
        String request = message.getText();
        String pattern = "[A-Z,a-z]{3,5} [0-9]+([\\\\.|,][0-9]+)?[+|-]";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(request);

        if (m.matches()) {
            Task newTask = new Task(request.split(" ")[0],
                    Double.parseDouble(request.substring(request.indexOf(" ") + 1, request.length() - 1)
                            .replace(",", ".")),
                    "+".equals(request.substring(request.length() - 1)));
            newTask.setTaskAuthorId(message.getChatId());
            return newTask;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Task {" + crCode + " " + (taskAuthorId == null ? "" : "(author=" + getTaskAuthorId() + ") ") + desiredValue + (great ? "+" : "-") + "}";
    }

    public String getCrCode() {
        return crCode;
    }

    public double getDesiredValue() {
        return desiredValue;
    }

    public boolean isGreat() {
        return great;
    }
}
