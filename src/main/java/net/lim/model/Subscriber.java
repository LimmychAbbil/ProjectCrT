package net.lim.model;

import net.lim.model.task.Task;

import java.util.List;
import java.util.Map;

public interface Subscriber {
    void update(Map<String, Double> cryptoMap);
    void checkTask(Task task, Map<String, Double> cryptoMap);

    void addTask(Task task);
    void removeTasks(String crCode);
    void removeTask(String crCode, Double desiredValue, boolean isGreat);
    List<Task> tasksList();

    Double getLatestUpdateByCrCode(String crCode);
}
