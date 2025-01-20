package kr.hhplus.be.server.domain.service.queue;

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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("QueueService 통합 테스트")
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void tearDown() {
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("활성화 가능한 토큰이 100개 이하로 유지되도록 활성화 처리")
    void activateTokens_shouldNotExceed100ActiveTokens() {
        // given
        // 50개의 ACTIVE 상태 토큰 생성
        List<Token> activeTokens = createTestTokens(TokenStatus.ACTIVE, 70);
        tokenRepository.saveAll(activeTokens);

        // 150개의 WAIT 상태 토큰 생성
        List<Token> waitingTokens = createTestTokens(TokenStatus.WAIT, 150);
        tokenRepository.saveAll(waitingTokens);

        // when
        queueService.activateTokens();

        // then
        int result = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        assertThat(result).isEqualTo(100);

        int remainingWaitingTokens = tokenRepository.countByStatus(TokenStatus.WAIT);
        assertThat(remainingWaitingTokens).isEqualTo(120); // 나머지 WAIT 상태 토큰 확인
    }

    @Test
    @DisplayName("만료된 토큰을 EXPIRED 상태로 변경")
    void expireTokens_shouldUpdateExpiredTokensToExpiredStatus() {
        // given
        // 만료된 ACTIVE 상태 토큰 생성
        List<Token> expiredActiveTokens = createTestTokensWithExpiry(TokenStatus.ACTIVE, 10, LocalDateTime.now().minusMinutes(1));
        tokenRepository.saveAll(expiredActiveTokens);

        // 만료되지 않은 ACTIVE 상태 토큰 생성
        List<Token> validActiveTokens = createTestTokensWithExpiry(TokenStatus.ACTIVE, 10, LocalDateTime.now().plusMinutes(10));
        tokenRepository.saveAll(validActiveTokens);

        // 만료된 WAIT 상태 토큰 생성
        List<Token> expiredWaitingTokens = createTestTokensWithExpiry(TokenStatus.WAIT, 5, LocalDateTime.now().minusMinutes(1));
        tokenRepository.saveAll(expiredWaitingTokens);

        // when
        queueService.expireTokens();

        // then
        int expiredTokens = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        assertThat(expiredTokens).isEqualTo(15); // 만료된 ACTIVE + WAIT 토큰 수

        int validTokens = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        assertThat(validTokens).isEqualTo(10); // 여전히 유효한 ACTIVE 토큰
    }

    @Test
    @DisplayName("동시에 여러 개의 토큰 활성화 요청이 발생해도 최대 100개까지만 활성화된다")
    void activateTokens_concurrentRequests_shouldNotExceedLimit() throws InterruptedException {
        // given
        IntStream.range(0, 150).forEach(i -> {
            tokenRepository.save(Token.createToken((long) i));
        });

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        // when
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                try {
                    queueService.activateTokens();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long activeTokenCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        assertThat(activeTokenCount).isEqualTo(100);
    }

    @Test
    @DisplayName("동시에 여러 개의 토큰 만료 요청이 발생해도 동일한 토큰이 중복 만료되지 않는다")
    void expireTokens_concurrentRequests_shouldHandleProperly() throws InterruptedException {
        // given
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(1);
        IntStream.range(0, 50).forEach(i -> {
            Token token = Token.createToken((long) i);
            token.expireToken();
            tokenRepository.save(token);
        });

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        // when: 동시에 3개의 만료 요청을 실행
        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    queueService.expireTokens();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long activeTokenCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        System.out.println("만료 처리 후 활성화된 토큰 개수: " + activeTokenCount);

        assertThat(activeTokenCount).isEqualTo(0); // 모든 활성화된 토큰이 만료되었는지 검증
    }

    // Helper 메서드: 특정 상태의 토큰 생성
    private List<Token> createTestTokens(TokenStatus status, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> Token.builder()
                        .status(status)
                        .expiredAt(LocalDateTime.now().plusMinutes(30))
                        .createdAt(LocalDateTime.now().minusMinutes(i))
                        .build())
                .collect(Collectors.toList());
    }

    // Helper 메서드: 만료 시간 설정 토큰 생성
    private List<Token> createTestTokensWithExpiry(TokenStatus status, int count, LocalDateTime expiredAt) {
        return IntStream.range(0, count)
                .mapToObj(i -> Token.builder()
                        .status(status)
                        .expiredAt(expiredAt)
                        .createdAt(LocalDateTime.now().minusMinutes(i))
                        .build())
                .collect(Collectors.toList());
    }

}