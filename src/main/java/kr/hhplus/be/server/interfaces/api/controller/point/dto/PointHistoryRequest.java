package kr.hhplus.be.server.interfaces.api.controller.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PointHistoryRequest {

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "충전/사용 할 포인트 (양수만 허용)")
    @Min(value = 0, message = "충전/사용 포인트는 0보다 커야합니다.")
    private int changeAmount;

    @Schema(required = true, description = "충전/사용 후 포인트 (양수만 허용)")
    @Min(value = 0, message = "충전/사용 후 포인트는 0보다 커야합니다.")
    private int pointAfterAmount;

    @Schema(required = true, description = "포인트 변경 타입")
    @NotBlank(message = "유효하지 않은 포인트 변경 타입입니다.")
    private PointChangeType changeType;
}
