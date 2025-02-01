package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ReservationService 통합 테스트")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이미 결제 완료된 예약은 만료 처리되지 않는다")
    void expirePendingReservation_shouldSkipCompletedReservations() {
        // given
        Reservation pendingReservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().minusMinutes(1)) // 만료됨
                .build();

        Reservation completedReservation = Reservation.builder()
                .status(ReservationStatus.SUCCESS) // 이미 결제 완료
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        reservationRepository.saveAll(List.of(pendingReservation, completedReservation));

        // when
        reservationService.expirePendingReservation();

        // then
        // 만료 처리된 예약 확인
        int failedReservations = reservationRepository.countByStatus(ReservationStatus.FAILED);
        assertThat(failedReservations).isEqualTo(1);

        // 이미 결제 완료된 예약 확인
        int successReservations = reservationRepository.countByStatus(ReservationStatus.SUCCESS);
        assertThat(successReservations).isEqualTo(1);
    }

    @Test
    @DisplayName("동시에 같은 좌석을 예약하려고 할 때, 중복 예약이 발생하지 않아야 한다")
    void createReservation_concurrentRequests_shouldPreventDuplicateReservations() throws InterruptedException {
        // given
        Long seatId = 2L;
        Long userId1 = 100L;
        Long userId2 = 200L;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 테이블 row 삽입에 대한 deadlock 방지용
        reservationService.createReservation(999L, 1L);

        // when
        executorService.execute(() -> {
            try {
                reservationService.createReservation(userId1, seatId);
                successCount.incrementAndGet();
            } catch (ReservationException e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.execute(() -> {
            try {
                reservationService.createReservation(userId2, seatId);
                successCount.incrementAndGet();
            } catch (ReservationException e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1); // 한 명만 성공해야 함
        assertThat(failureCount.get()).isEqualTo(1); // 나머지는 실패해야 함
    }
}