package kr.hhplus.be.server.api.controller.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.point.entity.PointChangeType;
import kr.hhplus.be.server.domain.point.entity.PointHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PointHistoryResponse {

    @Schema(required = true, description = "잔액 히스토리 ID")
    @NotNull
    private Long id;

    @Schema(required = true, description = "잔액 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "변경된 금액")
    @NotNull
    private int changeAmount;

    @Schema(required = true, description = "변경 후 잔액")
    @NotNull
    private int pointAfterAmount;

    @Schema(required = true, description = "변경 유형")
    @NotNull
    private PointChangeType changeType;

    private LocalDateTime createdAt;

    @Builder
    private PointHistoryResponse(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType, LocalDateTime createdAt) {
        this.userId = userId;
        this.changeAmount = changeAmount;
        this.pointAfterAmount = pointAfterAmount;
        this.changeType = changeType;
        this.createdAt = createdAt;
    }

    public static PointHistoryResponse of(PointHistory pointHistory) {
        return PointHistoryResponse.builder()
                .userId(pointHistory.getUserId())
                .changeAmount(pointHistory.getChangeAmount())
                .pointAfterAmount(pointHistory.getPointAfterAmount())
                .changeType(pointHistory.getChangeType())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }

    // 리스트 변환
    public static List<PointHistoryResponse> of(List<PointHistory> pointHistories) {
        return pointHistories.stream()
                .map(PointHistoryResponse::of)
                .collect(Collectors.toList());
    }
}
