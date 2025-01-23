package kr.hhplus.be.server.domain.service.reservation.concurrency.optimistic;

import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("좌석 예약 요청 - 낙관적 락 동시성 테스트")
public class ReservationOptimisticLockTest {
    
    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("낙관적 락을 이용한 동시 좌석 예약 테스트")
    void createReservation_concurrentRequests_optimisticLock() throws InterruptedException {
        // given
        Long seatId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) (100 + i);
            executorService.execute(() -> {
                try {
                    reservationService.createReservationWithOptimisticLock(userId, seatId);
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
        assertThat(successCount.get()).isEqualTo(1); // 단 하나의 요청만 성공해야 함
        assertThat(failureCount.get()).isEqualTo(threadCount - 1); // 나머지는 실패해야 함
    }
}
