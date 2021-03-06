package com.muravyev.cinema.repo;

import com.muravyev.cinema.entities.EntityStatus;
import com.muravyev.cinema.entities.hall.Hall;
import com.muravyev.cinema.entities.hall.Seat;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByRowAndNumberAndHallIdAndEntityStatus(int row, int number, long hallId, EntityStatus status);

    List<Seat> findAllByHallIdAndIdIn(long hallId, Collection<Long> id);

    List<Seat> findAllByHallIdAndEntityStatus(Long hallId, EntityStatus status, Sort sort);

    void deleteSeatsByIdInAndHallId(Collection<Long> id, Long hallId);

    @Modifying
    @Query("update Seat set unUsed = :unUsed where id in (:ids) and hall.id = :hallId")
    void setAllUnUsedStatus(@Param("hallId") long hallId,
                            @Param("ids") Collection<Long> ids,
                            @Param("unUsed") boolean unUsed);

    @Modifying
    @Query("update Seat set entityStatus = :status where id in (:ids) and hall.id = :hallId")
    void setAllNotActiveEntityStatus(@Param("hallId") long hallId,
                                     @Param("ids") Collection<Long> ids,
                                     @Param("status") EntityStatus status);

    @Query("select max(s.number) from Seat s where s.row = :row and s.hall = :hall and s.entityStatus = 'ACTIVE'")
    Optional<Integer> findLastSeatNumberInRow(@Param("row") int row, @Param("hall") Hall hall);
}
