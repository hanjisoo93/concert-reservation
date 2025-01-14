package kr.hhplus.be.server.interfaces.controller.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class TokenResponse {

    @Schema(required = true, description = "토큰 ID")
    @NotNull
    private Long id;

    @Schema(required = true, description = "사용자 식별자")
    @NotNull
    private String uuid;

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "토큰 상태")
    @NotNull
    private TokenStatus status;

    @Schema(description = "토큰 만료 시간")
    @NotNull
    private LocalDateTime expiredAt;

    @Schema(description = "토큰 생성 시간")
    @NotNull
    private LocalDateTime createdAt;

    @Builder
    private TokenResponse(Long id, String uuid, Long userId, TokenStatus status, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public static TokenResponse of(Token token) {
        return TokenResponse.builder()
                .id(token.getId())
                .uuid(token.getUuid())
                .userId(token.getUserId())
                .status(token.getStatus())
                .expiredAt(token.getExpiredAt())
                .createdAt(token.getCreatedAt())
                .build();
    }
}
