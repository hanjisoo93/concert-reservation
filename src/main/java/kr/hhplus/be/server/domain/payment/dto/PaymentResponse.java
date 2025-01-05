package kr.hhplus.be.server.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer id;           // 결제 고유 ID
    private String uuid;          // 사용자 UUID
    private Integer concertSeatId; // 결제 대상 좌석 ID
    private Double amount;        // 결제 금액
    private String status;        // 결제 상태: SUCCESS, FAILED
    private String paymentTime;   // 결제 완료 시간
}
