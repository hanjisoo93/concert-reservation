package kr.hhplus.be.server.domain.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String uuid;          // 사용자 UUID
    private Integer concertSeatId; // 결제 대상 좌석 ID
    private Double amount;        // 결제 금액
}
