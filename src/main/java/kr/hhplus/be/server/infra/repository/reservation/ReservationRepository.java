package kr.hhplus.be.server.infra.repository.reservation;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findById(Long reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findAllByExpiredAtBeforeAndStatus(@Param("now") LocalDateTime now, @Param("status") ReservationStatus status);

    int countByStatus(ReservationStatus reservationStatus);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.seatId = :seatId AND r.status IN (:statuses)")
    boolean existsSeatReservation(@Param("seatId") Long seatId, @Param("statuses") List<ReservationStatus> statuses);
}
