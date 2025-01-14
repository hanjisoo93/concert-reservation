package kr.hhplus.be.server.infra.repository.concert.seat;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertSeatRepository extends JpaRepository<ConcertSeat,Long> {

    ConcertSeat findAllById(Long concertSeatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cs FROM ConcertSeat cs WHERE cs.id = :seatId")
    ConcertSeat findByIdForUpdate(@Param("seatId") Long seatId);

    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);
}
