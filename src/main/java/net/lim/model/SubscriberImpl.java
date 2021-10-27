package net.lim.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lim.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SubscriberImpl implements Subscriber {
    private Map<String,Double> cryptoMap;
    private List<Task> taskList;

    @Getter
    @Setter
    private Long subscriberId;

    private static Map<Long, Subscriber> subscribers = new HashMap<>();

    public static Subscriber getSubscriber(Long subscriberId) {
        Subscriber subscriber = null;
        if (subscribers.get(subscriberId) == null) {
            subscriber = new SubscriberImpl(subscriberId);
            subscribers.put(subscriberId, subscriber);
        }
        return subscriber;
    }

    private SubscriberImpl(Task task) {
        this(task.getTaskAuthorId());
        taskList.add(task);
    }

    private SubscriberImpl(Long subscriberId) {
        this.subscriberId = subscriberId;
        this.taskList = new ArrayList<>();
    }

    @Override
    public void update(Map<String,Double> cryptoMap) {
        this.cryptoMap = cryptoMap;
        for (Task task : taskList) {
            if (isValueReached(task)) {
                display(task);
            }
        }
    }

    @Override
    public void addTask(Task task) {
        log.info("Adding task " + task);
        taskList.add(task);
    }

    @Override
    public void removeTasks(String crCode) {
        taskList.removeAll(taskList.stream().filter(t -> crCode.equalsIgnoreCase(t.getCrCode())).collect(Collectors.toList()));
    }

    private boolean isValueReached(Task task) {
        Double currentValue = cryptoMap.get(task.getCrCode());
        if (currentValue == null) {
            return false;
        }

        if (task.isGreat() && currentValue >= task.getDesiredValue()) {
            return true;
        }
        return !task.isGreat() && currentValue <= task.getDesiredValue();
    }

    private void display(Task task) {
        String message = "Current value is: " + cryptoMap.get(task.getCrCode());
        System.out.println("Task " + taskList + " completed. " + message);
        if (task.getTaskAuthorId() != null) {
            Application.sendTelegramMsg(task.getTaskAuthorId(), message);
        }
    }
}
