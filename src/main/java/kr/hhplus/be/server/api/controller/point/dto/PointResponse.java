package kr.hhplus.be.server.api.controller.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.point.entity.Point;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PointResponse {

    @Schema(required = true, description = "잔액 ID")
    @NotNull
    private Long id;

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(description = "잔액")
    private int amount;

    @Builder
    private PointResponse(Long id, Long userId, int amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
    }

    public static PointResponse of(Point point) {
        return PointResponse.builder()
                .id(point.getId())
                .userId(point.getUserId())
                .amount(point.getAmount())
                .build();
    }
}
