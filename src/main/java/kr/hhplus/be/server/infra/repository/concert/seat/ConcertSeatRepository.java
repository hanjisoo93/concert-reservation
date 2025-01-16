package kr.hhplus.be.server.infra.repository.concert.seat;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertSeatRepository extends JpaRepository<ConcertSeat,Long> {

    Optional<ConcertSeat> findConcertSeatById(Long concertSeatId);

    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);
}
