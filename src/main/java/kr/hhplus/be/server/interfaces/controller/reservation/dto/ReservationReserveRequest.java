package kr.hhplus.be.server.interfaces.controller.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationReserveRequest {
    @Schema(required = true, description = "예약 요청한 사용자 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "예약 요청한 좌석 ID")
    @NotNull
    private Long seatId;

    @Builder
    private ReservationReserveRequest(Long userId, Long seatId) {
        this.userId = userId;
        this.seatId = seatId;
    }
}
