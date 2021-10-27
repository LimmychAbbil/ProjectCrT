package net.lim.model;

import java.util.Map;

public interface Subscriber {
    void update(Map<String, Double> cryptoMap);

    void addTask(Task task);
    void removeTasks(String crCode);

}
