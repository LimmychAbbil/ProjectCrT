package net.lim.model.task;

import lombok.extern.slf4j.Slf4j;
import net.lim.Application;
import net.lim.model.Subscriber;
import net.lim.model.SubscriberImpl;
import net.lim.model.taskers.UaTasker;
import net.lim.telegram.CryptoFollowerBot;

@Slf4j
public class TaskHandler {
    public static final String TASK_DELETE_KEY_FORMAT = "TASK:DELETE %s %s";
    public static final String TASK_CHECK_KEY_FORMAT = "TASK:CHECK %s";
    public static final String TASK_EDIT_KEY_FORMAT = "TASK:EDIT %s %s %s";

    public static void handleTaskButtonPressed(Long author, String buttonPressed) {
        Subscriber subscriber = SubscriberImpl.getSubscriber(author);
        String cryptoCode = getCryptoCode(buttonPressed);
        if (isDeleteTask(buttonPressed)) {

            subscriber.removeTasks(cryptoCode);
            log.info("User " + author + " deleted his task for " + cryptoCode);
            Application.sendTelegramMsg(author, "All tasks for " + cryptoCode + " was deleted. You have "
                    + subscriber.tasksList().size() + " active tasks");
        } else if (isCheckTask(buttonPressed)) {
            Double latestUpdateValue = subscriber.getLatestUpdateByCrCode(cryptoCode);
            if (latestUpdateValue != null) {
                Application.sendTelegramMsg(author, "Latest check value for " + cryptoCode + " is " + latestUpdateValue);
            } else {
                Application.sendTelegramMsg(author, "No information available for code " + cryptoCode);
            }
        } else if (isEditTask(buttonPressed)) {
            double taskToEditDesiredValue = Double.parseDouble(buttonPressed.split(" ")[2]);
            boolean taskToEditIsGreat = "+".equals(buttonPressed.split(" ")[3]);
            subscriber.removeTask(cryptoCode, taskToEditDesiredValue, taskToEditIsGreat);

            log.info("User " + author + "is editing his task for " + cryptoCode + ":" + taskToEditDesiredValue + (taskToEditIsGreat ? "+" : "-"));

            CryptoFollowerBot.startNewTask(author, cryptoCode);
        } else {
            Application.sendTelegramMsg(author, "This button is not implemented yet, action ignored"); //TODO
        }
    }

    private static boolean isEditTask(String buttonPressed) {
        return buttonPressed.startsWith("TASK:EDIT");
    }

    private static boolean isCheckTask(String buttonPressed) {
        return buttonPressed.startsWith("TASK:CHECK");
    }

    private static boolean isDeleteTask(String buttonPressed) {
        return buttonPressed.startsWith("TASK:DELETE");
    }

    private static String getCryptoCode(String buttonPressed) {
        return buttonPressed.split(" ")[1];
    }
}
