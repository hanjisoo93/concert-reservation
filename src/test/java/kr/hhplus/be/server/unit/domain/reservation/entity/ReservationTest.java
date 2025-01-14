package kr.hhplus.be.server.unit.domain.reservation.entity;

import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class ReservationTest {

    @Test
    @DisplayName("예약 생성 성공")
    void createReservation_success() {
        // given
        Long userId = 1L;
        Long seatId = 100L;

        // when
        Reservation reservation = Reservation.createReservation(userId, seatId);

        // then
        Assertions.assertThat(reservation)
                .isNotNull()
                .extracting("userId", "seatId", "status")
                .containsExactly(userId, seatId, ReservationStatus.PENDING);

        Assertions.assertThat(reservation.getCreatedAt()).isNotNull();
        Assertions.assertThat(reservation.getExpiredAt()).isAfter(reservation.getCreatedAt());
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공")
    void updateStatus_success() {
        // given
        Reservation reservation = Reservation.createReservation(1L, 100L);

        // when
        reservation.updateStatus(ReservationStatus.SUCCESS);

        // then
        Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.SUCCESS);
    }

    @Test
    @DisplayName("유효하지 않은 예약 상태로 업데이트 시 예외 발생")
    void updateStatus_invalidStatus_throwsException() {
        // given
        Reservation reservation = Reservation.createReservation(1L, 100L);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.updateStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 예약 상태입니다.");
    }

    @Test
    @DisplayName("예약 만료 시간 업데이트 성공")
    void updateExpiredAt_success() {
        // given
        Reservation reservation = Reservation.createReservation(1L, 100L);
        LocalDateTime newExpiredAt = LocalDateTime.now().plusHours(1);

        // when
        reservation.updateExpiredAt(newExpiredAt);

        // then
        Assertions.assertThat(reservation.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    @DisplayName("유효하지 않은 만료 시간 업데이트 시 예외 발생")
    void updateExpiredAt_invalidExpiredAt_throwsException() {
        // given
        Reservation reservation = Reservation.createReservation(1L, 100L);
        LocalDateTime invalidExpiredAt = LocalDateTime.now().minusHours(1);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.updateExpiredAt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 만료 시간입니다.");

        Assertions.assertThatThrownBy(() -> reservation.updateExpiredAt(invalidExpiredAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 만료 시간입니다.");
    }
}