package kr.hhplus.be.server.domain.balance.dto;

import lombok.Data;

@Data
public class BalanceRequest {
    private String uuid;
    private Double amount;
}
