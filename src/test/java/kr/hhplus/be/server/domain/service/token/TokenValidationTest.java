package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.domain.service.token.TokenService;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("TokenValidationTest 통합 테스트")
public class TokenValidationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void tearDown() {
        tokenRepository.deleteAllInBatch();
    }
    @Test
    @DisplayName("유효한 UUID 토큰 검증")
    void isValidTokenByUuid_validToken() {
        // given
        Token validToken = Token.builder()
                .uuid("e1b2c3d4-5678-90ab-cdef-1234567890ab")
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
        tokenRepository.save(validToken);

        // when
        boolean result = tokenService.isValidTokenByUuid("e1b2c3d4-5678-90ab-cdef-1234567890ab");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("만료된 UUID 토큰 검증 실패")
    void isValidTokenByUuid_expiredToken() {
        // given
        Token expiredToken = Token.builder()
                .uuid("e1b2c3d4-5678-90ab-cdef-1234567890ab")
                .userId(1L)
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().minusMinutes(10))
                .build();
        tokenRepository.save(expiredToken);

        // when
        boolean result = tokenService.isValidTokenByUuid("e1b2c3d4-5678-90ab-cdef-1234567890ab");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("UUID 토큰이 존재하지 않을 경우 검증 실패")
    void isValidTokenByUuid_tokenNotFound() {
        // when
        boolean result = tokenService.isValidTokenByUuid("non-existent-uuid");

        // then
        assertThat(result).isFalse();
    }
}
