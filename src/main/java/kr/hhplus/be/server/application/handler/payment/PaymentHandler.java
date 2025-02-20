package kr.hhplus.be.server.application.handler.payment;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.service.payment.PaymentService;
import kr.hhplus.be.server.domain.service.point.PointService;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentHandler {

    private final PointService pointService;
    private final PaymentService paymentService;

    @Transactional
    public void payment(Long reservationId, Reservation reservation, ConcertSeat concertSeat) {
        pointService.spendPoint(reservation.getUserId(), concertSeat.getPrice());
        paymentService.createPayment(reservation.getUserId(), reservationId, concertSeat.getPrice());
    }
}
