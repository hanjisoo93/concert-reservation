package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.interfaces.controller.token.dto.TokenResponse;
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

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("TokenService 통합 테스트")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void tearDown() {
        tokenRepository.deleteAllInBatch();
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

        List<Long> userIds = IntStream.rangeClosed(1, numberOfUsers).mapToObj(Long::valueOf).collect(Collectors.toList());

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
}