package io.picthor.data.entity;

import com.realcnbs.horizon.framework.data.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Directory extends AbstractEntity {

    public enum State {
        ENABLED, DISABLED
    }

    public enum Type {
        ROOT, STANDARD
    }
    private Long parentId;
    private Long rootDirectoryId;
    private String fullPath;
    private String label;
    private String name;
    private String description;
    private State state;
    private Type type;
    private String excludes;
    private Directory parent;
    private List<Directory> children;
    private LocalDateTime lastSyncAt;
    private DirectoryStats stats;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(id, directory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
