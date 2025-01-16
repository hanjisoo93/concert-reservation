package kr.hhplus.be.server.domain.service.concert.seat;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.exception.concert.seat.ConcertSeatNotFoundException;
import kr.hhplus.be.server.interfaces.controller.concert.seat.dto.ConcertSeatResponse;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        try {
            return concertSeatRepository.findConcertSeatById(concertSeatId)
                .orElseThrow(ConcertSeatNotFoundException::new);
        } catch (ConcertSeatNotFoundException e) {
            log.warn("좌석 조회 실패: concertSeatId={}", concertSeatId, e);
            throw e;
        } catch (Exception e) {
            log.error("예기치 못한 오류 발생: concertSeatId={}", concertSeatId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
