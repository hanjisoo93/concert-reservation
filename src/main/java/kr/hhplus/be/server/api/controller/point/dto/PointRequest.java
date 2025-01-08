package kr.hhplus.be.server.api.controller.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PointRequest {

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "충전할 포인트 (양수만 허용)")
    @NotNull
    private int amount;
}
