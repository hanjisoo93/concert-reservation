package kr.hhplus.be.server.application.facade.payment;

import kr.hhplus.be.server.application.handler.payment.PaymentHandler;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.event.ReservationCompletedEvent;
import kr.hhplus.be.server.domain.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final ReservationService reservationService;
    private final ConcertSeatService concertSeatService;
    private final TokenService tokenService;
    private final PaymentHandler paymentHandler;

    private final ApplicationEventPublisher eventPublisher;

    @DistributedLock(key = "#userId", waitTime = 10, leaseTime = 15)
    public void paymentProcess(Long reservationId) {
        Reservation reservation = reservationService.validateReservation(reservationId);
        ConcertSeat concertSeat = concertSeatService.getConcertSeat(reservation.getSeatId());
        paymentHandler.payment(reservationId, reservation, concertSeat);
        reservationService.updateReservationStatus(reservationId, ReservationStatus.SUCCESS);
        tokenService.expireTokens();

        eventPublisher.publishEvent(new ReservationCompletedEvent(reservation));
    }
}
