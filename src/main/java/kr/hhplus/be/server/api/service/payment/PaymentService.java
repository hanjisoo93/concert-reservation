package kr.hhplus.be.server.api.service.payment;

import kr.hhplus.be.server.api.controller.payment.dto.PaymentResponse;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    @Transactional
    public PaymentResponse createPayment(Long userId, Long reservationId, int amount) {
        Payment payment = Payment.createPayment(userId, reservationId, amount);
        paymentRepository.save(payment);
        return PaymentResponse.of(payment);
    }
}
