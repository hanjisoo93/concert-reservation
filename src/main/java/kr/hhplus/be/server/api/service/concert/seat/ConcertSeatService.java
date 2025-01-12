package kr.hhplus.be.server.api.service.concert.seat;

import kr.hhplus.be.server.api.controller.concert.seat.dto.ConcertSeatResponse;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.repository.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ConcertSeatService {

    private final ConcertSeatRepository concertSeatRepository;

    @Transactional(readOnly = true)
    public List<ConcertSeatResponse> getConcertSeats(Long concertScheduleId){
        List<ConcertSeat> concertSeats = concertSeatRepository.findAllByConcertScheduleId(concertScheduleId);
        return ConcertSeatResponse.of(concertSeats);
    }

    @Transactional(readOnly = true)
    public ConcertSeatResponse getConcertSeat(Long concertSeatId){
        ConcertSeat concertSeat = concertSeatRepository.findAllById(concertSeatId);
        return ConcertSeatResponse.of(concertSeat);
    }
}
