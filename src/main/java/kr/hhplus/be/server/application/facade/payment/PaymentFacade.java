package kr.hhplus.be.server.application.facade.payment;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.domain.service.payment.PaymentService;
import kr.hhplus.be.server.domain.service.point.PointHistoryService;
import kr.hhplus.be.server.domain.service.point.PointService;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final ReservationService reservationService;
    private final ConcertSeatService concertSeatService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final PaymentService paymentService;

    @Transactional
    public void payment(Long reservationId) {
        // 1. 예약 확인
        Reservation reservation = reservationService.validReservation(reservationId);

        // 2. 좌석 확인
        ConcertSeat concertSeat = concertSeatService.getConcertSeat(reservation.getSeatId());

        // 3. 포인트 처리
        pointService.charge(reservation.getUserId(), concertSeat.getPrice());

        // 4. 결제 등록
        paymentService.createPayment(reservation.getUserId(), reservationId, concertSeat.getPrice());

        // 5. 예약 상태 변경
        reservationService.updateReservationStatus(reservationId, ReservationStatus.SUCCESS);
    }

    @Transactional
    public void paymentWithRedissonLock(Long reservationId) {
        // 1. 예약 확인
        Reservation reservation = reservationService.validReservation(reservationId);

        // 2. 좌석 확인
        ConcertSeat concertSeat = concertSeatService.getConcertSeat(reservation.getSeatId());

        // 3. 포인트 처리
        pointService.spendPointWithRedissonLock(reservation.getUserId(), concertSeat.getPrice());

        // 4. 결제 등록
        paymentService.createPayment(reservation.getUserId(), reservationId, concertSeat.getPrice());

        // 5. 예약 상태 변경
        reservationService.updateReservationStatus(reservationId, ReservationStatus.SUCCESS);
    }
}
