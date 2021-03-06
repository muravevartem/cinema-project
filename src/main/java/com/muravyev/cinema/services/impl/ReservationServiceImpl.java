package com.muravyev.cinema.services.impl;

import com.muravyev.cinema.dto.ReservationDto;
import com.muravyev.cinema.dto.SeatDto;
import com.muravyev.cinema.entities.EntityStatus;
import com.muravyev.cinema.entities.hall.Hall;
import com.muravyev.cinema.entities.hall.Seat;
import com.muravyev.cinema.entities.payment.Reservation;
import com.muravyev.cinema.entities.screening.FilmScreening;
import com.muravyev.cinema.entities.users.User;
import com.muravyev.cinema.events.*;
import com.muravyev.cinema.repo.FilmScreeningRepository;
import com.muravyev.cinema.repo.ReservationRepository;
import com.muravyev.cinema.repo.SeatRepository;
import com.muravyev.cinema.repo.TicketRepository;
import com.muravyev.cinema.services.ReservationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ReservationServiceImpl implements ReservationService, Observer {

    @Value("${app.reserve.age}")
    private long reserveExpiration;

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final FilmScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

    private final Map<Class<? extends Event<?>>, Consumer<Event<?>>> eventActions = new HashMap<>() {{

        put(CancelScreeningEvent.class, event -> cancelReservations(((CancelScreeningEvent) event).getValue()));
        put(DisableSeatEvent.class, (event -> cancelReservations(((DisableSeatEvent) event).getValue())));

    }};

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  TicketRepository ticketRepository,
                                  FilmScreeningRepository screeningRepository,
                                  SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
    }

    @Autowired
    @Override
    public void setNotificationManager(NotificationManager notificationManager) {
        notificationManager.subscribe(this, eventActions.keySet());
    }

    @Override
    @Transactional
    public List<Reservation> createReservations(List<ReservationDto> reservationDtos, User user) {
        List<Reservation> reservations = reservationDtos.stream()
                .map(reservationDto -> toReservation(reservationDto, user))
                .collect(Collectors.toList());
        return reservationRepository.saveAll(reservations);
    }

    @Override
    public List<Reservation> getReservations(long screeningId, User user) {
        return reservationRepository
                .findAllByFilmScreeningIdAndUserAndEntityStatusAndExpiryDateAfterOrderByCreatedDesc(screeningId,
                        user,
                        EntityStatus.ACTIVE,
                        new Date());
    }

    @Override
    public List<FilmScreening> getFilmScreeningWhereMyBookings(long filmId, User user) {
        return reservationRepository.findAllByUserAndFilmScreeningFilmIdAndEntityStatus(
                user,
                filmId,
                EntityStatus.ACTIVE
        );
    }

    private void cancelReservations(FilmScreening screening) {
        List<Reservation> reservations =
                reservationRepository.findAllByFilmScreeningAndEntityStatusAndExpiryDateAfter(screening,
                        EntityStatus.ACTIVE,
                        new Date());
        reservations.stream()
                .peek(x -> x.setEntityStatus(EntityStatus.NOT_ACTIVE))
                .forEach(reservationRepository::save);
    }

    private void cancelReservations(Seat seat) {
        List<Reservation> reservations =
                reservationRepository.findAllBySeatAndEntityStatusAndExpiryDateAfter(seat,
                        EntityStatus.ACTIVE,
                        new Date());
        reservations.stream()
                .peek(x -> x.setEntityStatus(EntityStatus.NOT_ACTIVE))
                .forEach(reservationRepository::save);
    }


    private Reservation toReservation(ReservationDto reservationDto, User user) {
        Reservation reservation = new Reservation();
        SeatDto seatDto = reservationDto.getSeat();
        FilmScreening filmScreening = screeningRepository.findByIdAndEntityStatus(reservationDto.getScreeningId(),
                        EntityStatus.ACTIVE)
                .orElseThrow(EntityNotFoundException::new);
        reservation.setFilmScreening(filmScreening);
        Hall hall = filmScreening.getHall();
        Seat seat = seatRepository.findByRowAndNumberAndHallIdAndEntityStatus(seatDto.getRow(),
                        seatDto.getNumber(),
                        hall.getId(),
                        EntityStatus.ACTIVE)
                .orElseThrow(EntityNotFoundException::new);
        if (reservationRepository.existsBySeatAndFilmScreeningAndEntityStatusAndExpiryDateAfter(seat,
                filmScreening,
                EntityStatus.ACTIVE,
                new Date()))
            throw new IllegalArgumentException("This seat is busy");
        if (ticketRepository.existsBySeatAndFilmScreeningAndEntityStatus(seat, filmScreening, EntityStatus.ACTIVE))
            throw new IllegalArgumentException("This seat is busy");
        reservation.setSeat(seat);
        Date autoExpiryDate = createExpirationDate();
        reservation.setExpiryDate(autoExpiryDate.before(filmScreening.getDate())
                ? autoExpiryDate
                : filmScreening.getDate());
        reservation.setUser(user);
        return reservation;
    }


    private Date createExpirationDate() {
        ZonedDateTime dateTime = ZonedDateTime.now().plusMinutes(reserveExpiration);
        return Date.from(dateTime.toInstant());
    }

    @Override
    public void notify(Event<?> event, Class<? extends Event<?>> eventType) {
        eventActions.get(eventType).accept(event);
    }
}
