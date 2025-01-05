package kr.hhplus.be.server.domain.balance.dto;

import lombok.Data;

@Data
public class BalanceHistoryRequest {
    private String uuid;          // 사용자 UUID
    private Double changeAmount;  // 변경 금액
    private String changeType;    // 변경 유형: DEPOSIT, WITHDRAWAL
}
