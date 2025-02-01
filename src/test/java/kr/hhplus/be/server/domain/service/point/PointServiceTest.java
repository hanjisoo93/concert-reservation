package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("PointService 통합 테스트")
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @BeforeEach
    void tearDown() {
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시에 여러 개의 포인트 차감 요청이 들어와도 정상적으로 차감된다")
    void processPoint_concurrentRequests_shouldDeductCorrectly() throws InterruptedException {
        // given
        Long testUserId = 1L;
        int initialPoint = 10000;
        int deductionAmount = 3000;
        int numThreads = 5;

        Point mockPoint = Point.builder()
                .userId(testUserId)
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
                    pointService.charge(testUserId, deductionAmount);
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
        Point resultPoint = pointRepository.findPointByUserId(testUserId).orElseThrow();

        assertThat(successCount.get() + failureCount.get()).isEqualTo(numThreads); // 모든 요청이 처리되었는지 확인
        assertThat(resultPoint.getAmount()).isEqualTo(initialPoint - (successCount.get() * deductionAmount)); // 남은 포인트 검증
        assertThat(failureCount.get()).isGreaterThan(0); // 일부 요청이 실패해야 정상
    }

    @Test
    @DisplayName("포인트 잔액이 부족할 경우 일부 요청이 실패해야 한다")
    void processPoint_concurrentRequests_shouldFailWhenBalanceIsInsufficient() throws InterruptedException {
        // given
        Long testUserId = 2L;
        int initialPoint = 10000;
        int deductionAmount = 5000;
        int numThreads = 5;

        Point mockPoint = Point.builder()
                .userId(testUserId)
                .amount(initialPoint)
                .build();
        pointRepository.save(mockPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    pointService.charge(testUserId, deductionAmount);
                } catch (PointException e) {
                    failureCount.incrementAndGet(); // 예외 발생 횟수 기록
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(failureCount.get()).isGreaterThan(0);
    }
}