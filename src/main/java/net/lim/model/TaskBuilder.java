package net.lim.model;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskBuilder {
    private String crCode;
    private Double desiredValue;
    private Boolean greater;
    private Long taskAuthorId;

    public TaskBuilder withTaskAuthor(Long taskAuthorId) {
        this.taskAuthorId = taskAuthorId;
        return this;
    }

    public TaskBuilder withCrCode(String crCode) {
        this.crCode = crCode;
        return this;
    }

    public TaskBuilder withDesiredValue(double desiredValue) {
        this.desiredValue = desiredValue;
        return this;
    }

    public TaskBuilder withPlusOrMinus(boolean greater) {
        this.greater = greater;
        return this;
    }

    public boolean isReady() {
        return crCode != null && desiredValue != null && greater != null;
    }

    public Task build() {
        if (!isReady()) return null;
        return new Task(crCode, desiredValue, greater, taskAuthorId);
    }

    public static Task fromString(Message message) {
        String request = message.getText();
        if (isMessageAPattern(request)) {
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

    public static boolean isMessageAPattern(String message) {
        String pattern = Task.FULL_TASK_PATTERN;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(message);
        return m.matches();
    }
}
