package kr.hhplus.be.server.api.controller.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
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
    private String userId;

    @Schema(required = true, description = "토큰 상태")
    @NotNull
    private TokenStatus status;

    @Schema(description = "토큰 만료 시간")
    @NotNull
    private LocalDateTime expiredAt;

    @Builder
    private TokenResponse(Long id, String uuid, String userId, TokenStatus status, LocalDateTime expiredAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.status = status;
        this.expiredAt = expiredAt;
    }

    public static TokenResponse of(Token token) {
        return TokenResponse.builder()
                .id(token.getId())
                .uuid(token.getUuid())
                .userId(token.getUserId())
                .status(token.getStatus())
                .expiredAt(token.getExpiredAt())
                .build();
    }
}
