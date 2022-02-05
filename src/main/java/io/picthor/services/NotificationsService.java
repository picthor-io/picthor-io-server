package io.picthor.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class NotificationsService {

    private final LinkedList<Notification> notifications = new LinkedList<>();

    public void addSuccess(String title, String message) {
        notifications.add(new Notification(Notification.Type.SUCCESS, title, message));
    }

    public void addError(String title, String message) {
        notifications.add(new Notification(Notification.Type.ERROR, title, message));
    }

    public void addInfo(String title, String message) {
        notifications.add(new Notification(Notification.Type.INFO, title, message));
    }

    public List<Notification> getAll() {
        List<Notification> res = new ArrayList<>();
        while(!notifications.isEmpty()){
            res.add(notifications.pop());
        }
        return res;
    }
}
