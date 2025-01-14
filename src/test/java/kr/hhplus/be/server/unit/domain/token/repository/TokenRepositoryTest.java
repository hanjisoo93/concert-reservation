package kr.hhplus.be.server.unit.domain.token.repository;

import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

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
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(expiredAt)
                .build();
        Mockito.when(tokenRepository.findAllByUserIdAndStatus(1L, TokenStatus.ACTIVE))
                .thenReturn(tokenBuild);

        // when
        Token token = tokenRepository.findAllByUserIdAndStatus(1L, tokenBuild.getStatus());

        // then
        assertThat(token)
                .isNotNull()
                .extracting("uuid", "userId", "status", "expiredAt")
                .containsExactlyInAnyOrder("test-uuid", 1L, TokenStatus.ACTIVE, expiredAt);

        Mockito.verify(tokenRepository).findAllByUserIdAndStatus(1L, TokenStatus.ACTIVE);
    }

    @DisplayName("사용자 ID로 토큰 조회 시 토큰이 없다면 Exception 처리를 한다")
    @Test
    void findAllByUserIdAndStatus_whenTokenNotFound_throwsException() {
        // given
        Long userId = 1L;
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

    @Test
    @DisplayName("상태별 토큰 개수 조회")
    void countByStatus() {
        // given
        Mockito.when(tokenRepository.countByStatus(TokenStatus.ACTIVE)).thenReturn(5);
        Mockito.when(tokenRepository.countByStatus(TokenStatus.WAIT)).thenReturn(10);

        // when
        int activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
        int waitCount = tokenRepository.countByStatus(TokenStatus.WAIT);

        // then
        assertThat(activeCount).isEqualTo(5);
        assertThat(waitCount).isEqualTo(10);

        Mockito.verify(tokenRepository, times(1)).countByStatus(TokenStatus.ACTIVE);
        Mockito.verify(tokenRepository, times(1)).countByStatus(TokenStatus.WAIT);
    }

    @Test
    @DisplayName("WAIT 상태의 오래된 토큰 상위 100개 조회")
    void findTop100ByStatusOrderByCreatedAtAsc() {
        // given
        List<Token> mockTokens = IntStream.range(0, 100)
                .mapToObj(i -> Token.builder()
                        .uuid("uuid-" + i)
                        .userId(1L + i)
                        .status(TokenStatus.WAIT)
                        .expiredAt(null)
                        .build())
                .collect(Collectors.toList());

        Mockito.when(tokenRepository.findTop100ByStatusOrderByCreatedAtAsc(TokenStatus.WAIT))
                .thenReturn(mockTokens);

        // when
        List<Token> tokens = tokenRepository.findTop100ByStatusOrderByCreatedAtAsc(TokenStatus.WAIT);

        // then
        assertThat(tokens).hasSize(100);
        assertThat(tokens.get(0).getUuid()).isEqualTo("uuid-0");
        assertThat(tokens.get(99).getUuid()).isEqualTo("uuid-99");

        Mockito.verify(tokenRepository, times(1))
                .findTop100ByStatusOrderByCreatedAtAsc(TokenStatus.WAIT);
    }

    @Test
    @DisplayName("만료된 ACTIVE 상태의 토큰 조회")
    void findAllByExpiredAtBeforeAndStatus() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Token> mockTokens = List.of(
                Token.builder()
                        .uuid("expired-token-1")
                        .userId(1L)
                        .status(TokenStatus.ACTIVE)
                        .expiredAt(now.minusMinutes(10))
                        .build(),
                Token.builder()
                        .uuid("expired-token-2")
                        .userId(2L)
                        .status(TokenStatus.ACTIVE)
                        .expiredAt(now.minusMinutes(5))
                        .build()
        );

        Mockito.when(tokenRepository.findAllByExpiredAtBeforeAndStatus(now, TokenStatus.ACTIVE))
                .thenReturn(mockTokens);

        // when
        List<Token> expiredTokens = tokenRepository.findAllByExpiredAtBeforeAndStatus(now, TokenStatus.ACTIVE);

        // then
        assertThat(expiredTokens).hasSize(2);
        assertThat(expiredTokens.get(0).getUuid()).isEqualTo("expired-token-1");
        assertThat(expiredTokens.get(1).getUuid()).isEqualTo("expired-token-2");

        Mockito.verify(tokenRepository, times(1))
                .findAllByExpiredAtBeforeAndStatus(now, TokenStatus.ACTIVE);
    }

    @Test
    @DisplayName("WAIT 상태에서 만료 시간이 설정되고 만료 여부 확인")
    void waitTokenExpiration() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Token waitToken = Token.builder()
                .uuid("wait-uuid")
                .userId(1L)
                .status(TokenStatus.WAIT)
                .expiredAt(now.plusMinutes(10)) // 10분 후 만료
                .build();

        Mockito.when(tokenRepository.findAllByExpiredAtBeforeAndStatus(now.plusMinutes(11), TokenStatus.WAIT))
                .thenReturn(List.of(waitToken));

        // when
        List<Token> expiredTokens = tokenRepository.findAllByExpiredAtBeforeAndStatus(now.plusMinutes(11), TokenStatus.WAIT);

        // then
        assertThat(expiredTokens).hasSize(1);
        assertThat(expiredTokens.get(0).getUuid()).isEqualTo("wait-uuid");

        Mockito.verify(tokenRepository, times(1))
                .findAllByExpiredAtBeforeAndStatus(now.plusMinutes(11), TokenStatus.WAIT);
    }

    @Test
    @DisplayName("WAIT 상태에서 ACTIVE로 전환 시 만료 시간 연장")
    void activateWaitTokenAndExtendExpiration() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Token waitToken = Token.builder()
                .uuid("wait-uuid")
                .userId(1L)
                .status(TokenStatus.WAIT)
                .expiredAt(now.plusMinutes(10)) // 10분 후 만료
                .build();

        Mockito.when(tokenRepository.findAllByUserIdAndStatus(1L, TokenStatus.WAIT)).thenReturn(waitToken);

        // when
        Token token = tokenRepository.findAllByUserIdAndStatus(1L, TokenStatus.WAIT);
        token.updateStatus(TokenStatus.ACTIVE); // ACTIVE로 전환

        // then
        assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(token.getExpiredAt()).isAfter(now.plusMinutes(29)); // ACTIVE로 전환 후 30분 연장 확인
        assertThat(token.getExpiredAt()).isBefore(now.plusMinutes(31));
    }
}