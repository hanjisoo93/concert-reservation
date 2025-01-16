package kr.hhplus.be.server.unit.domain.payment.entity;

import kr.hhplus.be.server.domain.entity.payment.Payment;
import kr.hhplus.be.server.domain.exception.payment.PaymentException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentTest {

    @Test
    @DisplayName("결제 내역 생성 성공")
    void createPayment_success() {
        // given
        Long userId = 1L;
        Long reservationId = 100L;
        int amount = 500;

        // when
        Payment payment = Payment.createPayment(userId, reservationId, amount);

        // then
        Assertions.assertThat(payment)
                .isNotNull()
                .extracting("userId", "reservationId", "amount")
                .containsExactly(userId, reservationId, amount);

        Assertions.assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 금액이 0 이하일 경우 예외 발생")
    void createPayment_invalidAmount_throwsException() {
        // given
        Long userId = 1L;
        Long reservationId = 100L;

        // when & then
        Assertions.assertThatThrownBy(() -> Payment.createPayment(userId, reservationId, 0))
                .isInstanceOf(PaymentException.class)
                .hasMessage("결제 금액은 0보다 커야 합니다.");

        Assertions.assertThatThrownBy(() -> Payment.createPayment(userId, reservationId, -100))
                .isInstanceOf(PaymentException.class)
                .hasMessage("결제 금액은 0보다 커야 합니다.");
    }
}
