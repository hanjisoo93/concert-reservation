package kr.hhplus.be.server.infra.repository.reservation;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.entity.reservation.ReservationOptimistic;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationOptimisticRepository extends JpaRepository<ReservationOptimistic, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT r FROM ReservationOptimistic r WHERE r.seatId = :seatId AND r.status IN (:statuses)")
    Optional<ReservationOptimistic> findReservationBySeatIdAndStatues(@Param("seatId") Long seatId, @Param("statuses") List<ReservationStatus> statuses);
}
