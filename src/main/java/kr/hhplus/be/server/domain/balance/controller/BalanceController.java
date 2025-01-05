package kr.hhplus.be.server.domain.balance.controller;

import kr.hhplus.be.server.domain.balance.dto.BalanceRequest;
import kr.hhplus.be.server.domain.balance.dto.BalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/balance")
public class BalanceController {
    private final Map<String, Double> userBalances = new HashMap<>();

    public BalanceController() {
        userBalances.put("mock-uuid-12345", 100.0); // 사용자 A
        userBalances.put("mock-uuid-67890", 250.0); // 사용자 B
    }

    /**
     * 잔액 조회
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String uuid) {
        // Mock balance lookup
        Double balance = userBalances.getOrDefault(uuid, 0.0);
        BalanceResponse response = new BalanceResponse(uuid, balance);
        return ResponseEntity.ok(response);
    }

    /**
     * 잔액 충전 요청
     */
    @PostMapping("/charge")
    public ResponseEntity<BalanceResponse> chargeBalance(@RequestBody BalanceRequest request) {
        // Update balance
        userBalances.put(request.getUuid(), userBalances.getOrDefault(request.getUuid(), 0.0) + request.getAmount());

        // Prepare response
        BalanceResponse response = new BalanceResponse(request.getUuid(), userBalances.get(request.getUuid()));
        return ResponseEntity.ok(response);
    }
}
