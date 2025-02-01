package kr.hhplus.be.server.domain.service.reservation.concurrency.distributed.redis;

import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("좌석 예약 요청 - Redisson AOP 기반 분산 락 동시성 테스트")
public class ReservationRedissonLockTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Redisson AOP 기반 분산 락을 이용한 동시 좌석 예약 테스트")
    void createReservation_concurrentRequests_redisson_lock() throws InterruptedException {
        // given
        Long seatId = 1L;
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) (5 + i);
            executorService.execute(() -> {
                try {
                    reservationService.createReservationWithRedissonLock(userId, seatId);
                    successCount.incrementAndGet();
                } catch (ReservationException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }
}
