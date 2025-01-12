package kr.hhplus.be.server.api.scheduler.token;

import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("TokenScheduler 통합 테스트")
class TokenSchedulerTest {

    @Autowired
    private TokenScheduler tokenScheduler;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
    @DisplayName("WAIT 상태 토큰을 ACTIVE 상태로 변경")
    void testActivateTokens() {
        // given
        for (int i = 1; i <= 150; i++) {
            TokenStatus status = (i <= 100) ? TokenStatus.WAIT : TokenStatus.ACTIVE;
            tokenRepository.save(Token.builder()
                    .uuid("token-" + i)
                    .userId((long) i)
                    .status(status)
                    .expiredAt(LocalDateTime.now().plusMinutes(30))
                    .build());
        }

        // when
        tokenScheduler.activateTokens();

        // then
        long activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        assertThat(activeCount).isEqualTo(100);

        long waitCount = tokenRepository.countByStatus(TokenStatus.WAIT);
        assertThat(waitCount).isEqualTo(50);
    }

    @Test
    @Transactional
    @DisplayName("만료된 ACTIVE 상태 토큰을 EXPIRED 상태로 변경")
    void testExpireTokens() {
        // given
        for (int i = 1; i <= 100; i++) {
            boolean isExpired = i <= 30; // 상위 30개는 만료된 시간 설정
            tokenRepository.save(Token.builder()
                    .uuid("active-token-" + i)
                    .userId((long) i)
                    .status(TokenStatus.ACTIVE)
                    .expiredAt(isExpired
                            ? LocalDateTime.now().minusMinutes(5) // 만료된 토큰
                            : LocalDateTime.now().plusMinutes(30)) // 유효한 토큰
                    .build());
        }

        // when
        tokenScheduler.expireTokens(); // 스케줄러 실행

        // then
        long expiredCount = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        assertThat(expiredCount).isEqualTo(30);

        long activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        assertThat(activeCount).isEqualTo(70);
    }

    @Test
    @DisplayName("만료된 ACTIVE 상태 토큰 처리 및 WAIT 상태 토큰 활성화")
    void testConcurrentSchedulers() throws InterruptedException {
        // given
        for (int i = 1; i <= 250; i++) {
            TokenStatus status = (i <= 100) ? TokenStatus.ACTIVE : TokenStatus.WAIT;
            LocalDateTime expiredAt = (i <= 50)
                    ? LocalDateTime.now().minusMinutes(5)  // 만료된 토큰
                    : LocalDateTime.now().plusMinutes(30); // 유효한 토큰

            tokenRepository.save(Token.builder()
                    .uuid("token-" + i)
                    .userId((long) i)
                    .status(status)
                    .expiredAt(expiredAt)
                    .build());
        }

        long initialActiveCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        long initialExpiredCount = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        long initialWaitCount = tokenRepository.countByStatus(TokenStatus.WAIT);

        assertThat(initialActiveCount).isEqualTo(100);
        assertThat(initialExpiredCount).isEqualTo(0);
        assertThat(initialWaitCount).isEqualTo(150);

        // when
        tokenScheduler.processTokens();

        // then
        long finalActiveCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        long finalExpiredCount = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        long finalWaitCount = tokenRepository.countByStatus(TokenStatus.WAIT);

        assertThat(finalActiveCount).isEqualTo(100);
        assertThat(finalExpiredCount).isEqualTo(50);
        assertThat(finalWaitCount).isEqualTo(100);
    }
}