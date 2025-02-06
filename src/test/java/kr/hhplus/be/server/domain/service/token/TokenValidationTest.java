package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.domain.service.token.TokenService;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRedisRepository;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("TokenValidationTest 통합 테스트")
public class TokenValidationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRedisRepository tokenRedisRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String activeKey = TokenType.ACTIVE.getKeyPrefix();

    @BeforeEach
    void tearDown() {
        redisTemplate.delete(activeKey);
    }

    @Test
    @DisplayName("유효한 토큰 검증 - 토큰이 존재하면 true")
    void isValidToken_validToken() {
        // given
        Long userId = 1L;
        String value = String.valueOf(userId);
        double score = System.currentTimeMillis() + (TokenType.ACTIVE.getTtlSeconds() * 1000L);
        tokenRedisRepository.saveToken(activeKey, value, score);

        // when
        boolean valid = tokenService.isValidToken(userId);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰 검증 - 토큰이 없으면 false")
    void isValidToken_tokenNotFound() {
        // given
        Long userId = 2L;

        // when
        boolean valid = tokenService.isValidToken(userId);

        // then
        assertThat(valid).isFalse();
    }
}
