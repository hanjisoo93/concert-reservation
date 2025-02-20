package kr.hhplus.be.server.interfaces.api.controller.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.payment.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponse {

    @Schema(required = true, description = "결제 ID")
    @NotNull
    private Long id;

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(description = "예약 ID")
    @NotNull
    private Long reservationId;

    @Schema(required = true, description = "결제 금액")
    @NotNull
    private int amount;

    private LocalDateTime createdAt;

    @Builder
    private PaymentResponse(Long id, Long userId, Long reservationId, int amount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static PaymentResponse of(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .reservationId(payment.getReservationId())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
