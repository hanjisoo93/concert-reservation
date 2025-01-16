package kr.hhplus.be.server.domain.service.concert.seat;

import kr.hhplus.be.server.domain.exception.concert.seat.ConcertSeatNotFoundException;
import kr.hhplus.be.server.interfaces.controller.concert.seat.dto.ConcertSeatResponse;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertSeatService {

    private final ConcertSeatRepository concertSeatRepository;

    @Transactional(readOnly = true)
    public List<ConcertSeat> getConcertSeats(Long concertScheduleId){
        return concertSeatRepository.findAllByConcertScheduleId(concertScheduleId);
    }

    @Transactional(readOnly = true)
    public ConcertSeat getConcertSeat(Long concertSeatId){
        return concertSeatRepository.findConcertSeatById(concertSeatId)
                .orElseThrow(() -> new ConcertSeatNotFoundException("존재 하는 좌석이 없습니다."));
    }
}
