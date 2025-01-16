package kr.hhplus.be.server.domain.entity.token;

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

    private LocalDateTime createdAt;

    @Builder
    private Token(String uuid, Long userId, TokenStatus status, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.uuid = uuid;
        this.userId = userId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public static Token createToken(Long userId) {
        return Token.builder()
                .uuid(generateUuid())
                .userId(userId)
                .status(TokenStatus.WAIT)
                .expiredAt(LocalDateTime.now().plusMinutes(30)) // 기본 30분 유지
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void expireToken() {
        if (this.status == TokenStatus.EXPIRED) {
            throw new IllegalStateException("이미 만료된 토큰입니다.");
        }
        this.status = TokenStatus.EXPIRED;
    }

    public void updateStatus(TokenStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        }
        this.status = newStatus;

        // ACTIVE로 전환 시 만료 시간 연장
        if (newStatus == TokenStatus.ACTIVE) {
            this.expiredAt = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void updateExpiredAt(LocalDateTime newExpiredAt) {
        if (newExpiredAt == null || newExpiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("유효하지 않은 만료 시간입니다.");
        }
        this.expiredAt = newExpiredAt;
    }

    private static String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // 토큰 유효성 확인
    public boolean isExpired() {
        return this.expiredAt.isBefore(LocalDateTime.now());
    }

    // (테스트) 강제 만료 처리
    public void forceExpire(int minusMinutes) {
        this.expiredAt = LocalDateTime.now().minusMinutes(minusMinutes);
    }
}
