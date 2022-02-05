package io.picthor.services;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends AbstractEntity {

    public enum Type{
        SUCCESS, ERROR, INFO
    }

    private Type type;

    private String title;

    private String message;

    public Notification(Type type, String title, String message) {
        this.createdAt = LocalDateTime.now();
        this.type = type;
        this.title = title;
        this.message = message;
    }
}
