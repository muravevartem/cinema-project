package com.muravyev.cinema.repo;

import com.muravyev.cinema.stat.statobjects.AgeLimitFilmCountStatObject;
import com.muravyev.cinema.stat.statobjects.CountryFilmCountStatObject;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StatisticsRepository {
    List<CountryFilmCountStatObject> getStatisticsCountryFilm(Date startDate, Date endDate);

    Optional<Double> getOccupancyFilm(long filmId, Date startDate, Date endDate);

    Optional<Double> getAverageTicketsInPurchase(long filmId, Date startDate, Date endDate);

    Optional<Long> getCountTickets(long filmId, Date startDate, Date endDate);

    List<AgeLimitFilmCountStatObject> getStatisticsLimitsFilm(Date startDate, Date endDate);
}
