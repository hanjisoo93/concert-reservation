package kr.hhplus.be.server.application.facade.payment;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import kr.hhplus.be.server.infra.repository.payment.PaymentRepository;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        pointHistoryRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("결제 성공")
    void processPaymentSuccessfully() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Point mockPoint = Point.builder()
                .userId(1L)
                .amount(70000)
                .build();
        Point savedPoint = pointRepository.save(mockPoint);

        Reservation mockReservation = Reservation.builder()
                .userId(1L)
                .seatId(savedConcertSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Reservation savedReservation = reservationRepository.save(mockReservation);

        // when
        paymentFacade.payment(savedReservation.getId());

        // then
        // 1. 예약 상태 확인
        Reservation updatedReservation = reservationRepository.findReservationById(savedReservation.getId())
                .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.SUCCESS);

        // 2. 포인트 차감 확인
        Point updatedPoint = pointRepository.findById(savedPoint.getId())
                .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));
        assertThat(updatedPoint.getAmount()).isEqualTo(20000);

        // 3. 포인트 히스토리 확인
        List<PointHistory> pointHistories = pointHistoryRepository.findAll();
        assertThat(pointHistories).hasSize(1);
        assertThat(pointHistories.get(0).getChangeType()).isEqualTo(PointChangeType.WITHDRAWAL);
        assertThat(pointHistories.get(0).getChangeAmount()).isEqualTo(50000);
        assertThat(pointHistories.get(0).getPointAfterAmount()).isEqualTo(20000);
    }

    @Test
    @DisplayName("포인트 부족으로 결제 실패")
    void processPaymentWithInsufficientPoints() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Point mockPoint = Point.builder()
                .userId(1L)
                .amount(0)
                .build();
        pointRepository.save(mockPoint);

        Reservation mockReservation = Reservation.builder()
                .userId(1L)
                .seatId(savedConcertSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Reservation savedReservation = reservationRepository.save(mockReservation);

        // when & then
        assertThatThrownBy(() -> paymentFacade.payment(savedReservation.getId()))
                .isInstanceOf(PointException.class)
                .hasMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("만료된 예약으로 결제 실패")
    void processPaymentWithExpiredReservation() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Reservation mockReservation = Reservation.builder()
                .userId(1L)
                .seatId(savedConcertSeat.getId())
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now())
                .build();
        Reservation savedReservation = reservationRepository.save(mockReservation);

        // when & then
        assertThatThrownBy(() -> paymentFacade.payment(savedReservation.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약 요청 가능한 시간이 만료되었습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 예약으로 결제 실패")
    void processPaymentWithNonExistentReservation() {
        // when & then
        assertThatThrownBy(() -> paymentFacade.payment(999L)) // 존재하지 않는 예약 ID
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("예약 정보를 찾을 수 없습니다.");
    }
}