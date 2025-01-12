package kr.hhplus.be.server.api.controller.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.token.entity.Token;
import lombok.Getter;

@Getter
public class ReservationReserveRequest {
    private Token token;

    @Schema(required = true, description = "예약 요청한 좌석 ID")
    @NotNull
    private Long seatId;
}
