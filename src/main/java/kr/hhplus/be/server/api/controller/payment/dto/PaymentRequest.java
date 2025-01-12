package kr.hhplus.be.server.api.controller.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentRequest {

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(description = "예약 ID")
    @NotNull
    private Long reservationId;

    @Schema(required = true, description = "결제 금액")
    @NotNull
    private int amount;
}
