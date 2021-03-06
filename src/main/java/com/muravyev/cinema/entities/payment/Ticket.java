package com.muravyev.cinema.entities.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muravyev.cinema.entities.IdentityBaseEntity;
import com.muravyev.cinema.entities.hall.Seat;
import com.muravyev.cinema.entities.screening.FilmScreening;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "tickets")
@EntityListeners(AuditingEntityListener.class)
public class Ticket extends IdentityBaseEntity {
    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private BigDecimal price;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "film_screening_id", nullable = false)
    private FilmScreening filmScreening;

    @JsonIgnore
    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    private TicketRefund ticketRefund;

    @Transient
    private boolean isExpired;

    @PostLoad
    private void checkExpired() {
        isExpired = filmScreening.getDate().before(new Date()) || !isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        if (!super.equals(o)) return false;
        Ticket ticket = (Ticket) o;
        return isExpired == ticket.isExpired
                && Objects.equals(seat, ticket.seat)
                && Objects.equals(price, ticket.price)
                && Objects.equals(purchase, ticket.purchase)
                && Objects.equals(filmScreening, ticket.filmScreening)
                && Objects.equals(ticketRefund, ticket.ticketRefund);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), seat, price, purchase, filmScreening, ticketRefund, isExpired);
    }
}
