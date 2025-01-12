package kr.hhplus.be.server.api.service.concert.schedule;

import kr.hhplus.be.server.api.controller.concert.schedule.dto.ConcertScheduleResponse;
import kr.hhplus.be.server.domain.concert.schedule.entity.ConcertSchedule;
import kr.hhplus.be.server.domain.concert.schedule.repository.ConcertScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ConcertScheduleService {

    private final ConcertScheduleRepository concertScheduleRepository;

    @Transactional(readOnly = true)
    public Page<ConcertScheduleResponse> getConcertSchedules(Long concertId, Pageable pageable) {
        LocalDate currentDate = LocalDate.now();

        Page<ConcertSchedule> concertSchedules = concertScheduleRepository
                .findAllByConcertIdAndConcertDateAfter(concertId, currentDate, pageable);
        return concertSchedules.map(ConcertScheduleResponse::of);
    }
}
