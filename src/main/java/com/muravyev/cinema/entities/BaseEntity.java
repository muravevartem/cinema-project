package com.muravyev.cinema.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Data
@MappedSuperclass
public abstract class BaseEntity {
    @JsonIgnore
    @CreatedDate
    @Column(name = "insert_date")
    private Date created;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "update_date")
    private Date updated;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EntityStatus entityStatus = EntityStatus.ACTIVE;

    public BaseEntity() {
    }

    @PreUpdate
    private void update() {
        updated = new Date();
    }

    @PrePersist
    private void persist() {
        created = new Date();
    }

    public boolean isActive() {
        return entityStatus == EntityStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(created, that.created) && Objects.equals(updated, that.updated)
                && entityStatus == that.entityStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(created, updated, entityStatus);
    }
}
