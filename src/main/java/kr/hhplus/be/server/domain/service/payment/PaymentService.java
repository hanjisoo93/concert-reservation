package kr.hhplus.be.server.domain.service.payment;

import kr.hhplus.be.server.domain.entity.payment.Payment;
import kr.hhplus.be.server.infra.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    @Transactional
    public Payment createPayment(Long userId, Long reservationId, int amount) {
        Payment payment = Payment.createPayment(userId, reservationId, amount);
        paymentRepository.save(payment);
        return payment;
    }
}
