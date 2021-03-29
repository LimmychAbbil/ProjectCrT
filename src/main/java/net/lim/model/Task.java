package net.lim.model;


import lombok.Getter;
import lombok.Setter;

public class Task {

    private final String crCode;
    private final double desiredValue;
    private final boolean great;

    public final static String FULL_TASK_PATTERN = "[A-Z,a-z]{3,5} [0-9]+([\\\\.|,][0-9]+)?[+|-]";

    @Getter
    @Setter
    private Long taskAuthorId;

    public Task(String crCode, double desiredValue, boolean great) {
        this.crCode = crCode;
        this.desiredValue = desiredValue;
        this.great = great;
    }

    Task(String crCode, double desiredValue, boolean great, Long taskAuthorId) {
        this(crCode, desiredValue, great);
        setTaskAuthorId(taskAuthorId);
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
