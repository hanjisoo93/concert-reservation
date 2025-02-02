package kr.hhplus.be.server.application.facade.payment;

import kr.hhplus.be.server.application.handler.payment.PaymentHandler;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final ReservationService reservationService;
    private final ConcertSeatService concertSeatService;
    private final PaymentHandler paymentHandler;

    @DistributedLock(key = "#userId", waitTime = 10, leaseTime = 15)
    public void payment(Long reservationId) {
        // 1. 예약 확인
        Reservation reservation = reservationService.validateReservation(reservationId);

        // 2. 좌석 확인
        ConcertSeat concertSeat = concertSeatService.getConcertSeat(reservation.getSeatId());

        // 3. 결제 처리
        paymentHandler.payment(reservationId, reservation, concertSeat);
    }
}
