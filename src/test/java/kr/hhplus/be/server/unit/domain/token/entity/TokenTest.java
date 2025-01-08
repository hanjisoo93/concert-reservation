package kr.hhplus.be.server.unit.domain.token.entity;

import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    @DisplayName("토큰 생성 성공")
    void createToken_success() {
        // given
        String userId = "jisoohan";

        // when
        Token token = Token.createToken(userId);

        // then
        assertThat(token)
                .isNotNull()
                .extracting("userId", "status")
                .containsExactly(userId, TokenStatus.ACTIVE);

        assertThat(token.getExpiredAt()).isAfter(LocalDateTime.now());
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("토큰 만료 처리 성공")
    void expireToken_success() {
        // given
        Token token = Token.createToken("jisoohan");

        // when
        token.expireToken();

        // then
        assertThat(token.getStatus()).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("이미 만료된 토큰을 다시 만료 처리 시 예외 발생")
    void expireToken_alreadyExpired_throwsException() {
        // given
        Token token = Token.createToken("jisoohan");
        token.expireToken();

        // when & then
        assertThatThrownBy(token::expireToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 만료된 토큰입니다.");
    }

    @Test
    @DisplayName("만료된 토큰 확인 성공")
    void isExpired_success() {
        // given
        Token token = Token.builder()
                .uuid("test-uuid")
                .userId("jisoohan")
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().minusMinutes(1)) // 과거 시간
                .build();

        // when
        boolean expired = token.isExpired();

        // then
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰 확인 성공")
    void isExpired_notExpired_success() {
        // given
        Token token = Token.builder()
                .uuid("test-uuid")
                .userId("jisoohan")
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30)) // 미래 시간
                .build();

        // when
        boolean expired = token.isExpired();

        // then
        assertThat(expired).isFalse();
    }
}