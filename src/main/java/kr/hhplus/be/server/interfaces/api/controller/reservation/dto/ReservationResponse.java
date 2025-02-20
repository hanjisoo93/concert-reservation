package kr.hhplus.be.server.interfaces.api.controller.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationResponse {

    @Schema(required = true, description = "예약 ID")
    @NotNull
    private Long id;

    @Schema(required = true, description = "사용자 ID")
    @NotNull
    private Long userId;

    @Schema(required = true, description = "좌석 ID")
    @NotNull
    private Long seatId;

    @Schema(required = true, description = "결제 상태")
    @NotNull
    private ReservationStatus status;

    @Schema(required = true, description = "예약 가능 만료 시간")
    @NotNull
    private LocalDateTime expiredAt;

    @Schema(required = true, description = "예약 등록 일시")
    @NotNull
    private LocalDateTime createdAt;

    @Builder
    private ReservationResponse(Long userId, Long seatId, ReservationStatus status, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public static ReservationResponse of(Reservation reservation) {
        return ReservationResponse.builder()
                .userId(reservation.getUserId())
                .seatId(reservation.getSeatId())
                .status(reservation.getStatus())
                .expiredAt(reservation.getExpiredAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
