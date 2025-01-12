package kr.hhplus.be.server.api.facade.payment;

import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeatStatus;
import kr.hhplus.be.server.domain.concert.seat.repository.ConcertSeatRepository;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.point.entity.Point;
import kr.hhplus.be.server.domain.point.entity.PointChangeType;
import kr.hhplus.be.server.domain.point.entity.PointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final ReservationRepository reservationRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final TokenRepository tokenRepository;

    @Transactional
    public void processPayment(Long reservationId) {
        // 1. 예약 요청 확인
        Reservation reservation = reservationRepository.findAllById(reservationId);
        if(reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }

        if(reservation.isExpired(reservation.getExpiredAt())) {
            throw new IllegalStateException("예약 요청 가능한 시간이 만료되었습니다.");
        }

        // 2. 좌석 조회
        ConcertSeat concertSeat = concertSeatRepository.findByIdForUpdate(reservation.getSeatId());
        if (concertSeat == null) {
            throw new IllegalArgumentException("좌석을 찾을 수 없습니다.");
        }

        // 3. 포인트 결제 처리
        processPoint(reservation.getUserId(), concertSeat, reservationId);

        // 4. 상태 변경
        reservation.updateStatus(ReservationStatus.SUCCESS);
        concertSeat.updateStatus(ConcertSeatStatus.CONFIRMED);

        // 4. 토큰 만료 처리
        Token token = tokenRepository.findFirstByUserIdAndStatusForUpdate(reservation.getUserId(), TokenStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("유효한 토큰이 없습니다."));
        token.expireToken();
    }

    public void processPoint(Long userId, ConcertSeat concertSeat, Long reservationId) {
        // 1. 포인트 조회
        Point point = pointRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 존재하지 않습니다."));

        // 2. 좌석 가격 검증
        if (point.isAmountLessThan(concertSeat.getPrice())) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다. 충전 후 다시 시도해주세요.");
        }

        // 3. 결제 처리
        Payment payment = Payment.createPayment(userId, reservationId, concertSeat.getPrice());
        paymentRepository.save(payment);

        // 4. 포인트 차감
        point.userPoint(concertSeat.getPrice());
        pointRepository.save(point);

        // 5. 포인트 히스토리 저장
        PointHistory pointHistory = PointHistory.createPointHistory(
                userId,
                concertSeat.getPrice(),
                point.getAmount(),
                PointChangeType.WITHDRAWAL
        );
        pointHistoryRepository.save(pointHistory);
    }
}
