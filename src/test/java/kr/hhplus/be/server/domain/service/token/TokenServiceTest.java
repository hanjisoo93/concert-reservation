package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.infra.repository.token.TokenRedisRepository;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("TokenService 통합 테스트")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    TokenRedisRepository tokenRedisRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String waitingKey = TokenType.WAITING.getKeyPrefix();
    private final String activeKey = TokenType.ACTIVE.getKeyPrefix();

    @BeforeEach
    void tearDown() {
        tokenRepository.deleteAllInBatch();

        Set<String> waitingKeys = redisTemplate.keys(waitingKey + "*");
        if(!waitingKeys.isEmpty()) {
            redisTemplate.delete(waitingKeys);
        }

        Set<String> activeKeys = redisTemplate.keys(activeKey + "*");
        if(!activeKeys.isEmpty()) {
            redisTemplate.delete(activeKeys);
        }
    }

    @Test
    @DisplayName("새로운 사용자에게 WAIT 상태의 토큰 발급")
    void issueWaitToken_newUser_success() {
        // given
        Long userId = 1L;

        // when
        Token token = tokenService.issueWaitToken(userId);

        // then
        assertThat(token).isNotNull();
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getStatus()).isEqualTo(TokenStatus.WAIT);
        assertThat(token.getUuid()).isNotNull();
        assertThat(token.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("기존 ACTIVE 상태 토큰이 있는 경우 반환")
    void issueWaitToken_existingActiveToken() {
        // given
        Long userId = 1L;
        Token activeToken = Token.builder()
                .uuid("active-uuid")
                .userId(userId)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepository.save(activeToken);

        // when
        Token tokenResponse = tokenService.issueWaitToken(userId);

        // then
        assertThat(tokenResponse.getUuid()).isEqualTo("active-uuid");
        assertThat(tokenResponse.getStatus()).isEqualTo(TokenStatus.ACTIVE);
    }

    @Test
    @DisplayName("기존 WAIT 상태 토큰이 있는 경우 반환")
    void issueWaitToken_existingWaitToken() {
        // given
        Long userId = 1L;
        Token waitToken = Token.builder()
                .uuid("wait-uuid")
                .userId(userId)
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
        tokenRepository.save(waitToken);

        // when
        Token tokenResponse = tokenService.issueWaitToken(userId);

        // then
        assertThat(tokenResponse.getUuid()).isEqualTo("wait-uuid");
        assertThat(tokenResponse.getStatus()).isEqualTo(TokenStatus.WAIT);
    }

    @Test
    @DisplayName("기존 토큰이 모두 만료된 경우 새로운 WAIT 상태 토큰 발급")
    void issueWaitToken_expiredTokens() {
        // given
        Long userId = 1L;
        Token expiredToken = Token.builder()
                .uuid("expired-uuid")
                .userId(userId)
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().minusMinutes(5))
                .build();
        tokenRepository.save(expiredToken);

        // when
        Token tokenResponse = tokenService.issueWaitToken(userId);

        // then
        assertThat(tokenResponse.getUuid()).isNotEqualTo("expired-uuid"); // 새로운 토큰
        assertThat(tokenResponse.getStatus()).isEqualTo(TokenStatus.WAIT);
    }

    @Test
    @DisplayName("새로운 토큰 발급 → 활성 토큰 반환 → 만료된 토큰 처리")
    void tokenLifecycleIntegrationTest() {
        // Step 1: 새로운 사용자에게 WAIT 상태의 토큰 발급
        Long userId = 1L;
        Token tokenResponse1 = tokenService.issueWaitToken(userId);
        assertThat(tokenResponse1.getStatus()).isEqualTo(TokenStatus.WAIT);
        assertThat(tokenResponse1.getExpiredAt()).isAfter(LocalDateTime.now());

        // Step 2: 기존 WAIT 상태의 토큰 반환
        Token tokenResponse2 = tokenService.issueWaitToken(userId);
        assertThat(tokenResponse2.getUuid()).isEqualTo(tokenResponse1.getUuid());

        // Step 3: 기존 토큰 만료 후 새로운 WAIT 상태 토큰 발급
        Token expiredToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                userId,
                List.of(TokenStatus.WAIT, TokenStatus.ACTIVE),
                LocalDateTime.now()
        ).orElseThrow(() -> new IllegalStateException("유효하지 않은 토큰입니다."));
        expiredToken.forceExpire(5); // 강제로 만료 처리
        tokenRepository.save(expiredToken);

        Token tokenResponse3 = tokenService.issueWaitToken(userId);
        assertThat(tokenResponse3.getUuid()).isNotEqualTo(tokenResponse1.getUuid());
        assertThat(tokenResponse3.getStatus()).isEqualTo(TokenStatus.WAIT);
    }

    @Test
    @DisplayName("다수 사용자 동시 WAIT 토큰 발급")
    void issueWaitToken_concurrentRequests() throws InterruptedException {
        // given
        int numberOfUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);

        List<Long> userIds = IntStream.rangeClosed(1, numberOfUsers).mapToObj(Long::valueOf).toList();

        // when
        for (Long userId : userIds) {
            executor.submit(() -> {
                try {
                    tokenService.issueWaitToken(userId);
                } catch (Exception e) {
                    System.err.println("Error for user " + userId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        List<Token> tokens = tokenRepository.findAll();
        assertThat(tokens).hasSize(numberOfUsers);
        tokens.forEach(token -> {
            assertThat(token.getStatus()).isEqualTo(TokenStatus.WAIT);
            assertThat(token.getExpiredAt()).isAfter(LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("Redis - 다수 사용자 동시 WAIT 토큰 발급")
    void issueWaitTokenWithRedis_concurrentRequests() throws InterruptedException {
        // given
        int numberOfUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);

        List<String> resultList = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errorCount = new AtomicInteger(0);

        List<Long> userIds = IntStream.rangeClosed(1, numberOfUsers).mapToObj(Long::valueOf).toList();

        // when
        for (Long userId : userIds) {
            executor.submit(() -> {
                try {
                    String result = tokenService.issueWaitTokenWithRedis(userId);
                    resultList.add(result);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error for user " + userId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        assertTrue(resultList.contains("NEW_WAIT_TOKEN"), "새로운 토큰이 반드시 포함되어야 함");
        assertEquals(0, errorCount.get(), "예외 발생 없이 모든 요청이 성공해야 함");
        assertEquals(numberOfUsers, resultList.size(), "모든 요청이 응답을 받아야 함");
    }

    @Test
    @DisplayName("Redis - 활성 토큰이 100개 이하로 유지되도록 활성화 처리")
    void activateTokens_shouldNotExceed100ActiveTokens() {
        // given
        IntStream.range(0, 70).forEach(i -> {
            long expirationTime = System.currentTimeMillis() + TokenType.ACTIVE.getTtlSeconds() * 1000L;
            tokenRedisRepository.saveToken(activeKey, "user_active_" + i, expirationTime);
        });

        IntStream.range(0, 150).forEach(i -> {
            tokenRedisRepository.saveToken(waitingKey, "user_waiting_" + i, System.currentTimeMillis() + i);
        });

        // when
        tokenService.activateTokensWithRedis();

        // then
        Long activeCount = tokenRedisRepository.getTokenCount(activeKey);
        Long waitingCount = tokenRedisRepository.getTokenCount(waitingKey);

        assertThat(activeCount).isEqualTo(100);
        assertThat(waitingCount).isEqualTo(120);
    }

    @Test
    @DisplayName("Reids - 대기열이 비어 있을 때 활성화 처리하면 아무 변화가 없어야 함")
    void activateTokens_whenWaitingEmpty() {
        // given
        IntStream.range(0, 50).forEach(i -> {
            double score = System.currentTimeMillis() + i;
            redisTemplate.opsForZSet().add(activeKey, "activeUser_" + i, score);
        });

        // when
        tokenService.activateTokensWithRedis();

        // then
        Long activeCount = redisTemplate.opsForZSet().zCard(activeKey);
        Long waitingCount = redisTemplate.opsForZSet().zCard(waitingKey);
        assertThat(activeCount).isEqualTo(50);
        assertThat(waitingCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis - 대기열 토큰의 순서가 보장되어 가장 오래된 토큰부터 활성화된다")
    void activateTokens_orderPreservation() {
        // given
        redisTemplate.opsForZSet().add(waitingKey, "userA", 1000);
        redisTemplate.opsForZSet().add(waitingKey, "userB", 2000);
        redisTemplate.opsForZSet().add(waitingKey, "userC", 3000);
        redisTemplate.opsForZSet().add(waitingKey, "userD", 4000);

        // when
        tokenService.activateTokensWithRedis();

        // then
        Long waitingCount = redisTemplate.opsForZSet().zCard(waitingKey);
        Set<String> activeMembers = redisTemplate.opsForZSet().range(activeKey, 0, -1);

        assertThat(waitingCount).isEqualTo(0);
        assertThat(activeMembers).containsExactlyInAnyOrder("userA", "userB", "userC", "userD");
    }

    @Test
    @DisplayName("Redis - 만료된 토큰만 제거되어야 함")
    void expireTokens_shouldRemoveExpiredTokensOnly() {
        // given
        long now = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            tokenRedisRepository.saveToken(waitingKey, "waitingUser_expired_" + i, now - 1000);
        }
        for (int i = 0; i < 5; i++) {
            tokenRedisRepository.saveToken(waitingKey, "waitingUser_valid_" + i, now + 10000);
        }

        for (int i = 0; i < 8; i++) {
            tokenRedisRepository.saveToken(activeKey, "activeUser_expired_" + i, now - 1000);
        }
        for (int i = 0; i < 7; i++) {
            tokenRedisRepository.saveToken(activeKey, "activeUser_valid_" + i, now + 10000);
        }

        // when
        tokenService.expireTokensWithRedis();

        // then
        Long remainingWaiting = redisTemplate.opsForZSet().zCard(waitingKey);
        Long remainingActive = redisTemplate.opsForZSet().zCard(activeKey);
        assertThat(remainingWaiting).isEqualTo(5);
        assertThat(remainingActive).isEqualTo(7);
    }

    @Test
    @DisplayName("Redis - 만료 대상 토큰이 없으면 아무 변화가 없어야 함")
    void expireTokens_whenNoExpiredTokens() {
        // given
        long now = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            tokenRedisRepository.saveToken(waitingKey, "waitingUser_valid_" + i, now + 10000);
        }
        for (int i = 0; i < 5; i++) {
            tokenRedisRepository.saveToken(activeKey, "activeUser_valid_" + i, now + 10000);
        }

        // when
        tokenService.expireTokensWithRedis();

        // then
        Long remainingWaiting = redisTemplate.opsForZSet().zCard(waitingKey);
        Long remainingActive = redisTemplate.opsForZSet().zCard(activeKey);
        assertThat(remainingWaiting).isEqualTo(5);
        assertThat(remainingActive).isEqualTo(5);
    }
}