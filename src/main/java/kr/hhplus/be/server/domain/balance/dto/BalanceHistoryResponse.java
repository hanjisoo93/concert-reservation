package kr.hhplus.be.server.domain.balance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistoryResponse {
    private Integer id;           // 기록 고유 ID
    private String uuid;          // 사용자 UUID
    private Double changeAmount;  // 변경 금액
    private Double balanceAfterChange; // 변경 후 잔액
    private String changeType;    // 변경 유형: DEPOSIT, WITHDRAWAL
    private String createdAt;     // 변경 기록 생성 시간
}
