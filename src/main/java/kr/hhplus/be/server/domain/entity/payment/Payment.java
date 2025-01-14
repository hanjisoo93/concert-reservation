package kr.hhplus.be.server.domain.entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        return Payment.builder()
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
