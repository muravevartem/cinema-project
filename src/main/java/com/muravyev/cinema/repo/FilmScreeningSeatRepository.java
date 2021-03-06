package com.muravyev.cinema.repo;

import com.muravyev.cinema.entities.screening.FilmScreening;
import com.muravyev.cinema.entities.screening.FilmScreeningSeat;

import java.util.List;

public interface FilmScreeningSeatRepository extends ReadOnlyRepository<FilmScreeningSeat, Long> {
    List<FilmScreeningSeat> findAllByScreeningId(long screeningId);

    List<FilmScreeningSeat> findAllByScreening(FilmScreening screening);

//    @Query(nativeQuery = true,
//            value = "select case when avg(ocf.occupancy)<>0 then avg(ocf.occupancy) else 0 end \n" +
//                    "from occupancy_film ocf\n" +
//                    "        join films f on f.id = ocf.film_id\n" +
//                    "        join halls h on h.id = ocf.hall_id\n" +
//                    "        join seats s on h.id = s.hall_id\n" +
//                    "        left outer join tickets t on ocf.id = t.film_screening_id " +
//                    "           and s.id = t.seat_id and (t.status = 'ACTIVE') " +
//                    "where f.id = :film and (ocf.date between :startDate and :endDate)")
//    Optional<Double> getOccupancyScreeningId(@Param("film") long filmId,
//                                             @Param("startDate") Date start,
//                                             @Param("endDate") Date end);
}
