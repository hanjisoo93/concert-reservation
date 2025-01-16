package kr.hhplus.be.server.domain.service.reservation;

import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ReservationService 통합 테스트")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이미 결제 완료된 예약은 만료 처리되지 않는다")
    void expirePendingReservation_shouldSkipCompletedReservations() {
        // given
        Reservation pendingReservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .expiredAt(LocalDateTime.now().minusMinutes(1)) // 만료됨
                .build();

        Reservation completedReservation = Reservation.builder()
                .status(ReservationStatus.SUCCESS) // 이미 결제 완료
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        reservationRepository.saveAll(List.of(pendingReservation, completedReservation));

        // when
        reservationService.expirePendingReservation();

        // then
        // 만료 처리된 예약 확인
        int failedReservations = reservationRepository.countByStatus(ReservationStatus.FAILED);
        assertThat(failedReservations).isEqualTo(1);

        // 이미 결제 완료된 예약 확인
        int successReservations = reservationRepository.countByStatus(ReservationStatus.SUCCESS);
        assertThat(successReservations).isEqualTo(1);
    }
}