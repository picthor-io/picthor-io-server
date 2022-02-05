package io.picthor.rest.controller;

import io.picthor.rest.repr.NotificationRepr;
import io.picthor.services.Notification;
import io.picthor.services.NotificationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationsController {

    private final NotificationsService notificationsService;

    public NotificationsController(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @RequestMapping("/")
    public List<NotificationRepr> getAll() {
        List<Notification> notifications = notificationsService.getAll();
        List<NotificationRepr> reprs = new ArrayList<>();
        for (Notification notification : notifications) {
            reprs.add(new NotificationRepr(notification));
        }
        return reprs;
    }

}
