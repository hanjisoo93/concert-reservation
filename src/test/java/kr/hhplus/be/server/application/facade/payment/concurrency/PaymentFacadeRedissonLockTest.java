package kr.hhplus.be.server.application.facade.payment.concurrency.distributed.redis;

import kr.hhplus.be.server.application.facade.payment.PaymentFacade;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.point.Point;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("결제 요청 - Redisson AOP 기반 분산 락 동시성 테스트")
public class PaymentFacadeRedissonLockTest {

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
    @DisplayName("동시에 동일한 예약 ID로 여러 결제 요청이 발생할 때, 중복 결제가 발생하지 않아야 한다.")
    void concurrentPaymentRequests_shouldPreventDuplicatePayment() throws InterruptedException {
        // given
        Long userId = 1L;
        int seatPrice = 50000; // 좌석 가격 (결제 금액)
        int initialPoint = 70000; // 초기 포인트 (사용 가능 금액)
        int numThreads = 5; // 동시에 실행할 결제 요청 개수

        // 콘서트 좌석 생성 및 저장
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(seatPrice)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        // 사용자 포인트 생성 및 저장
        Point mockPoint = Point.builder()
                .userId(userId)
                .amount(initialPoint)
                .build();
        pointRepository.save(mockPoint);

        Reservation mockReservation = Reservation.builder()
                .userId(userId)
                .seatId(savedConcertSeat.getId()) // 동일한 좌석 ID 사용
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Reservation savedReservation = reservationRepository.save(mockReservation);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CyclicBarrier barrier = new CyclicBarrier(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();

        // when
        for (int i = 0; i < numThreads; i++) {
            tasks.add(() -> {
                try {
                    barrier.await(); // 모든 스레드가 동시에 실행되도록 대기
                    paymentFacade.paymentProcess(savedReservation.getId()); // 동일한 예약 ID 사용
                    successCount.incrementAndGet();
                } catch (PointException e) { // 포인트 부족 등의 예외 처리
                    failureCount.incrementAndGet();
                } catch (ReservationException e) { // 중복 결제 시 예외 발생
                    failureCount.incrementAndGet();
                }
                return null;
            });
        }

        executorService.invokeAll(tasks);
        executorService.shutdown();

        Reservation resultReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        Point resultPoint = pointRepository.findPointByUserId(userId).orElseThrow();
        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(resultReservation.getStatus()).isEqualTo(ReservationStatus.SUCCESS);
        assertThat(resultPoint.getAmount()).isEqualTo(20000);
        assertThat(histories).hasSize(1);
    }
}
