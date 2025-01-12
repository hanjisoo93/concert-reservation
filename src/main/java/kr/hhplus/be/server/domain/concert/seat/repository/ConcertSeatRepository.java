package kr.hhplus.be.server.domain.concert.seat.repository;

import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertSeatRepository extends JpaRepository<ConcertSeat,Long> {

    ConcertSeat findAllById(Long concertSeatId);

    List<ConcertSeat> findAllByConcertScheduleId(Long concertScheduleId);
}
