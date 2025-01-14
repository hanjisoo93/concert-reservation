package kr.hhplus.be.server.infra.repository.concert.schedule;

import kr.hhplus.be.server.domain.entity.concert.schedule.ConcertSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    Page<ConcertSchedule> findAllByConcertIdAndConcertDateAfter(Long concertId, LocalDate currentDate, Pageable pageable);
}
