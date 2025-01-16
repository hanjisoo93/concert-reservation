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
    public Payment createPayment(Long userId, Long reservationId, int amount) {
        try {
            Payment payment = Payment.createPayment(userId, reservationId, amount);
            paymentRepository.save(payment);
            log.info("결제 완료 - paymentId={}, userId={}, reservationId={}, amount={}",
                    payment.getId(), userId, reservationId, amount);
            return payment;
        } catch (Exception e) {
            log.error("결제 생성 실패 - userId={}, reservationId={}, amount={}", userId, reservationId, amount, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
