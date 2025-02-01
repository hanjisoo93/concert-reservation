package kr.hhplus.be.server.domain.service.point.concurrency.pessimistic;

import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.service.point.PointService;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("포인트 충전/사용 요청 - 비관적 락 동시성 테스트")
public class PointPessimisticLockTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @BeforeEach
    void tearDown(){
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시에 동일한 사용자가 포인트를 사용 테스트")
    void spendPoint_concurrentRequests_pessimisticLock() throws InterruptedException {
        // given
        Long userId = 1L;
        int initialPoint = 10000;
        int spendAmount = 5000;
        int numThreads = 5;

        Point mockPoint = Point.builder()
                .userId(userId)
                .amount(initialPoint)
                .build();
        pointRepository.save(mockPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    pointService.spendPoint(userId, spendAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Point resultPoint = pointRepository.findPointByUserId(userId).orElseThrow();

        assertThat(successCount.get() + failureCount.get()).isEqualTo(numThreads); // 모든 요청이 처리되었는지 확인
        assertThat(resultPoint.getAmount()).isEqualTo(initialPoint - (successCount.get() * spendAmount)); // 남은 포인트 검증
        assertThat(failureCount.get()).isGreaterThan(0); // 일부 요청이 실패해야 정상
    }

    @Test
    @DisplayName("동시에 동일한 사용자가 포인트를 충전 및 사용 테스트")
    void creditAndSpendPoint_concurrentRequests_pessimisticLock() throws InterruptedException{
        //given
        Long userId = 1L;
        int initialPoint = 10000;
        int chargeAmount = 5000;
        int spendAmount = 7000;
        int numThreads = 5;

        Point mockPoint = Point.builder()
                .userId(userId)
                .amount(initialPoint)
                .build();
        pointRepository.save(mockPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads * 2);
        CyclicBarrier barrier = new CyclicBarrier(numThreads * 2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();

        // when
        for (int i = 0; i < numThreads; i++) {
            tasks.add(() -> {
                try {
                    barrier.await();
                    pointService.creditPoint(userId, chargeAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
                return null;
            });

            tasks.add(() -> {
                try {
                    barrier.await();
                    pointService.spendPoint(userId, spendAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
                return null;
            });
        }

        executorService.invokeAll(tasks);
        executorService.shutdown();

        // then
        Point result = pointRepository.findPointByUserId(userId).orElseThrow();
        assertThat(result.getAmount()).isGreaterThanOrEqualTo(0);
    }
}
