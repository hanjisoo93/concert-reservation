package kr.hhplus.be.server.api.facade.payment;

import kr.hhplus.be.server.application.facade.payment.PaymentFacade;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import kr.hhplus.be.server.domain.entity.payment.Payment;
import kr.hhplus.be.server.infra.repository.payment.PaymentRepository;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("PaymentFacade 통합 테스트")
class PaymentFacadeTest {
    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        tokenRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        pointHistoryRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("결제 성공 통합 테스트")
    void processPayment_success() {
        // given
        Long userId = 1L;

        // 좌석 생성
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .price(10000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seatId(savedSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // 토큰 생성
        Token token = Token.createToken(userId);
        token.updateStatus(TokenStatus.ACTIVE);
        tokenRepository.save(token);

        // 포인트 생성
        Point point = Point.builder()
                .userId(userId)
                .amount(20000)
                .build();
        pointRepository.save(point);

        // when
        paymentFacade.processPayment(savedReservation.getId());

        // then
        // 예약 상태 확인
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.SUCCESS);

        // 좌석 상태 확인
        ConcertSeat updatedSeat = concertSeatRepository.findById(savedSeat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(ConcertSeatStatus.CONFIRMED);

        // 토큰 상태 확인
        Token updatedToken = tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.EXPIRED);
        assertThat(updatedToken.getStatus()).isEqualTo(TokenStatus.EXPIRED);

        // 포인트 상태 확인
        Point updatedPoint = pointRepository.findAllByUserId(userId);
        assertThat(updatedPoint.getAmount()).isEqualTo(10000);

        // 결제 정보 확인
        Payment payment = paymentRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("결제 정보가 저장되지 않았습니다."));
        assertThat(payment.getAmount()).isEqualTo(10000);
        assertThat(payment.getUserId()).isEqualTo(userId);

        // 포인트 히스토리 확인
        PointHistory pointHistory = pointHistoryRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("포인트 히스토리가 저장되지 않았습니다."));
        assertThat(pointHistory.getChangeType()).isEqualTo(PointChangeType.WITHDRAWAL);
        assertThat(pointHistory.getChangeAmount()).isEqualTo(10000);
        assertThat(pointHistory.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("결제 실패 - 포인트 부족")
    void processPayment_failDueToInsufficientPoints() {
        // given
        Long userId = 1L;

        // 좌석 생성
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .price(10000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seatId(savedSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
        Reservation saveReservation = reservationRepository.save(reservation);

        // 토큰 생성
        Token token = Token.createToken(userId);
        token.updateStatus(TokenStatus.ACTIVE);
        tokenRepository.save(token);

        // 포인트 생성 (부족한 포인트 설정)
        Point point = Point.builder()
                .userId(userId)
                .amount(5000)
                .build();
        pointRepository.save(point);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                        paymentFacade.processPayment(saveReservation.getId()),
                "포인트 잔액이 부족합니다. 충전 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("결제 실패 - 만료된 예약")
    void processPayment_failDueToExpiredReservation() {
        // given
        Long userId = 1L;

        // 좌석 생성
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .price(10000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        // 만료된 예약 생성
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seatId(savedSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().minusMinutes(5))
                .build();
        Reservation saveReservation = reservationRepository.save(reservation);

        // 토큰 생성
        Token token = Token.createToken(userId);
        token.updateStatus(TokenStatus.ACTIVE);
        tokenRepository.save(token);

        // when & then
        assertThrows(IllegalStateException.class, () ->
                        paymentFacade.processPayment(saveReservation.getId()),
                "예약 요청 가능한 시간이 만료되었습니다.");
    }
}