package kr.hhplus.be.server.domain.payment.controller;

import kr.hhplus.be.server.domain.payment.dto.PaymentRequest;
import kr.hhplus.be.server.domain.payment.dto.PaymentResponse;
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
@RequestMapping("/payments")
public class PaymentController {
    private final List<PaymentResponse> paymentRecords = new ArrayList<>();
    private final AtomicInteger paymentIdCounter = new AtomicInteger(1);

    public PaymentController() {
        paymentRecords.add(new PaymentResponse(1, "mock-uuid-12345", 1, 100.0, "SUCCESS", "2024-12-31T10:00:00"));
        paymentRecords.add(new PaymentResponse(2, "mock-uuid-67890", 2, 150.0, "FAILED", "2024-12-31T11:00:00"));
    }


    /**
     * 결제 처리
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        String status = (request.getAmount() > 0) ? "SUCCESS" : "FAILED";

        PaymentResponse payment = new PaymentResponse(
                paymentIdCounter.getAndIncrement(),
                request.getUuid(),
                request.getConcertSeatId(),
                request.getAmount(),
                status,
                LocalDateTime.now().toString()
        );

        paymentRecords.add(payment);

        return ResponseEntity.ok(payment);
    }
}
