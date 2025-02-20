package kr.hhplus.be.server.interfaces.api.controller.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationConfirmRequest {

    @Schema(required = true, description = "콘서트 좌석 ID")
    @NotNull
    private Long seatId;

    private ReservationStatus status;
    private LocalDateTime expiredAt;
}
