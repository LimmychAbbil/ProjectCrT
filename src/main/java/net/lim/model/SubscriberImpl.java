package net.lim.model;

import net.lim.telegram.CryptoFollowerBot;

import java.util.Map;

public class SubscriberImpl implements Subscriber {
    private Map<String,Double> cryptoMap;
    private Task task;

    public SubscriberImpl(Task task) {
        this.task = task;
    }

    @Override
    public void update(Map<String,Double> cryptoMap) {
        this.cryptoMap = cryptoMap;
        if (isValueReached()) {
            display();
        }
    }

    private boolean isValueReached() {
        Double currentValue = cryptoMap.get(task.getCrCode());
        if (currentValue == null) {
            return false;
        }

        if (task.isGreat() && currentValue >= task.getDesiredValue()) {
            return true;
        }
        return !task.isGreat() && currentValue <= task.getDesiredValue();
    }

    public void display() {
        String message = "Current value is: " + cryptoMap.get(task.getCrCode());
        System.out.println("Task " + task + " completed. " + message);
        if (task.getTaskAuthorId() != null) {
            CryptoFollowerBot.sendMsg(task.getTaskAuthorId(), message);
        }
    }
}
