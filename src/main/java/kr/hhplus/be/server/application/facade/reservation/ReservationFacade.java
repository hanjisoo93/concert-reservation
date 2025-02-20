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
    private final TokenService tokenService;

    @Transactional
    public void reserve(Long userId, Long seatId) {
        concertSeatService.getConcertSeat(seatId);
        reservationService.validateSeatReservation(seatId);
        reservationService.createReservation(userId, seatId);
        tokenService.expireTokens();
    }
}
