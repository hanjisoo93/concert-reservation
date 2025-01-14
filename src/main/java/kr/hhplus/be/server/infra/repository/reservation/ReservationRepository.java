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

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation findAllById(Long reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.expiredAt < :now AND r.status = :status")
    List<Reservation> findAllByExpiredAtBeforeAndStatusWithLock(@Param("now") LocalDateTime now, @Param("status") ReservationStatus status);

    long countByStatus(ReservationStatus reservationStatus);
}
