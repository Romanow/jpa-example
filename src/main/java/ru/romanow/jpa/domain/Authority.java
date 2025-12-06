package ru.romanow.jpa.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.*;
import java.util.Objects;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "authority")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority grant = (Authority) o;
        return Objects.equals(id, grant.id) && Objects.equals(name, grant.name) && Objects.equals(priority, grant.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, priority);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("priority", priority)
                .toString();
    }
}
