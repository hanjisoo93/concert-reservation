package kr.hhplus.be.server.domain.entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.payment.PaymentException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long reservationId;

    private int amount;

    private LocalDateTime createdAt;

    @Builder
    private Payment(Long userId, Long reservationId, int amount, LocalDateTime createdAt) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static Payment createPayment(Long userId, Long reservationId, int amount){
        if(amount <= 0) {
            throw new PaymentException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        return Payment.builder()
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
