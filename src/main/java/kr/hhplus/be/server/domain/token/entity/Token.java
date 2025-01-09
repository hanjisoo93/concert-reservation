package kr.hhplus.be.server.domain.token.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    private LocalDateTime expiredAt;

    @Builder
    private Token(String uuid, Long userId, TokenStatus status, LocalDateTime expiredAt) {
        this.uuid = uuid;
        this.userId = userId;
        this.status = status;
        this.expiredAt = expiredAt;
    }

    public static Token createToken(Long userId) {
        return Token.builder()
                .uuid(generateUuid())
                .userId(userId)
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().plusMinutes(30)) // 기본 30분 유지
                .build();
    }

    public void expireToken() {
        if (this.status == TokenStatus.EXPIRED) {
            throw new IllegalStateException("이미 만료된 토큰입니다.");
        }
        this.status = TokenStatus.EXPIRED;
    }

    private static String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // 토큰 유효성 확인
    public boolean isExpired() {
        return this.expiredAt.isBefore(LocalDateTime.now());
    }
}
