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

    /**
     *
     * @param desiredValue
     * @return null if not crCode specified yet. Or return a TaskBuilder with crCode and DesiredValue
     */
    public TaskBuilder withDesiredValue(double desiredValue) {
        if (this.crCode == null) return null;
        this.desiredValue = desiredValue;
        return this;
    }

    /**
     *
     * @param greater
     * @return null if task is not ready to subscribe. Or return a ready task
     */
    public Task withPlusOrMinus(boolean greater) {
        this.greater = greater;
        if (notReady()) return null;
        return new Task(crCode, desiredValue, greater, taskAuthorId);
    }

    private boolean notReady() {
        return crCode == null || desiredValue == null || greater == null;
    }

    @Deprecated
    public Task build() {
        if (notReady()) return null;
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
