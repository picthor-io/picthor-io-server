package io.picthor.data.entity;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Setting extends AbstractEntity {

    public enum Type {
        STRING, JSON_ARRAY
    }

    private String name;

    private String value;

    private Type type;

}
