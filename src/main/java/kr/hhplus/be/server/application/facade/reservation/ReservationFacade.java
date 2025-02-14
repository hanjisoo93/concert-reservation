package kr.hhplus.be.server.application.facade.reservation;

import kr.hhplus.be.server.domain.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationFacade {

    private final ConcertSeatService concertSeatService;
    private final ReservationService reservationService;

    @Transactional
    public void reserve(Long userId, Long seatId) {
        // 1. 좌석 확인
        concertSeatService.getConcertSeat(seatId);

        // 2. 좌석 예약 가능 여부 확인
        reservationService.validateSeatReservation(seatId);

        // 3. 예약 하기
        reservationService.createReservation(userId, seatId);

        // 4. 토큰 만료 시간 연장
        // @TODO 활성화 토큰 만료 시간 연장
    }
}
