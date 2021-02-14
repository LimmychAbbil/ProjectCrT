package net.lim.model.taskers;

import net.lim.model.Subscriber;

import java.util.Map;

public interface Tasker extends Runnable {
    void registerSubscriber(Subscriber o);
    void removeSubscriber(Subscriber o);
    void notifyObservers();

    void saveUpdates(Map<String, Double> map);
}
