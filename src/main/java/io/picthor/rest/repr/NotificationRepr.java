package io.picthor.rest.repr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.realcnbs.horizon.framework.rest.repr.AbstractEntityRepr;
import io.picthor.services.Notification;

@JsonIgnoreProperties({"id", "updatedAt"})
public class NotificationRepr extends AbstractEntityRepr {

    private final Notification notification;

    public NotificationRepr(Notification notification) {
        super(notification);
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }

    @JsonProperty
    public Notification.Type getType() {
        return notification.getType();
    }

    @JsonProperty
    public String getTitle() {
        return notification.getTitle();
    }

    @JsonProperty
    public String getMessage() {
        return notification.getMessage();
    }
}
