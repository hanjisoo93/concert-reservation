package kr.hhplus.be.server.domain.service.payment;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.payment.Payment;
import kr.hhplus.be.server.infra.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void createPayment(Long userId, Long reservationId, int amount) {
        try {
            Payment payment = Payment.createPayment(userId, reservationId, amount);
            paymentRepository.save(payment);
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
