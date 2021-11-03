package com.muravyev.cinema.entities.hall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muravyev.cinema.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "places")
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row", nullable = false)
    private String row;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "unused", nullable = false)
    private boolean disabled = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "hall_id")
    private Hall hall;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return row == place.row && number == place.number && disabled == place.disabled && Objects.equals(id, place.id) && Objects.equals(hall, place.hall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, row, number, disabled, hall);
    }
}
