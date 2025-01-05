package kr.hhplus.be.server.domain.balance.controller;

import kr.hhplus.be.server.domain.balance.dto.BalanceHistoryResponse;
import kr.hhplus.be.server.domain.balance.dto.BalanceHistoryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/balance/history")
public class BalanceHistoryController {
    private final List<BalanceHistoryResponse> balanceHistoryList = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public BalanceHistoryController() {
        balanceHistoryList.add(new BalanceHistoryResponse(1, "mock-uuid-12345", 50.0, 150.0, "DEPOSIT", "2024-12-31T10:00:00"));
        balanceHistoryList.add(new BalanceHistoryResponse(2, "mock-uuid-67890", -30.0, 220.0, "WITHDRAWAL", "2024-12-31T11:00:00"));
    }

    /**
     * 잔액 변경 기록 저장
     */
    @PostMapping
    public ResponseEntity<BalanceHistoryResponse> saveBalanceHistory(@RequestBody BalanceHistoryRequest request) {
        Double previousBalance = balanceHistoryList.stream()
                .filter(record -> record.getUuid().equals(request.getUuid()))
                .mapToDouble(BalanceHistoryResponse::getBalanceAfterChange)
                .max()
                .orElse(0.0);

        Double newBalance = previousBalance + request.getChangeAmount();

        BalanceHistoryResponse newRecord = new BalanceHistoryResponse(
                idCounter.getAndIncrement(),
                request.getUuid(),
                request.getChangeAmount(),
                newBalance,
                request.getChangeType(),
                LocalDateTime.now().toString()
        );

        balanceHistoryList.add(newRecord);

        return ResponseEntity.ok(newRecord);
    }
}
