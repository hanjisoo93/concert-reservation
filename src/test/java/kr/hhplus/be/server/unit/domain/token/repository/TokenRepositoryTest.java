package kr.hhplus.be.server.unit.domain.token.repository;

import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class TokenRepositoryTest {

    @Mock
    private TokenRepository tokenRepository;

    @DisplayName("사용자 ID와 정상 토큰 상태를 가진 토큰을 조회한다.")
    @Test
    void findAllByUserIdAndStatus() {
        // given
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5).truncatedTo(ChronoUnit.MICROS);
        Token tokenBuild = Token.builder()
                .uuid("test-uuid")
                .userId("jisoohan")
                .status(TokenStatus.ACTIVE)
                .expiredAt(expiredAt)
                .build();
        Mockito.when(tokenRepository.findAllByUserIdAndStatus("jisoohan", TokenStatus.ACTIVE))
                .thenReturn(tokenBuild);

        // when
        Token token = tokenRepository.findAllByUserIdAndStatus("jisoohan", tokenBuild.getStatus());

        // then
        Assertions.assertThat(token)
                .isNotNull()
                .extracting("uuid", "userId", "status", "expiredAt")
                .containsExactlyInAnyOrder("test-uuid", "jisoohan", TokenStatus.ACTIVE, expiredAt);

        Mockito.verify(tokenRepository).findAllByUserIdAndStatus("jisoohan", TokenStatus.ACTIVE);
    }

    @DisplayName("사용자 ID로 토큰 조회 시 토큰이 없다면 Exception 처리를 한다")
    @Test
    void findAllByUserIdAndStatus_whenTokenNotFound_throwsException() {
        // given
        String userId = "jisoohan";
        Mockito.when(tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE))
                .thenReturn(null); // 토큰이 없는 경우를 Mocking

        // when & then
        Assertions.assertThatThrownBy(() -> {
                    Token token = tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
                    if (token == null) {
                        throw new IllegalArgumentException("유효한 토큰을 찾을 수 없습니다.");
                    }
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효한 토큰을 찾을 수 없습니다.");

        // Verify
        Mockito.verify(tokenRepository).findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
    }
}