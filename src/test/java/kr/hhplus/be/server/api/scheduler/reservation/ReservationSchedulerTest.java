package kr.hhplus.be.server.api.scheduler.reservation;

import kr.hhplus.be.server.interfaces.scheduler.reservation.ReservationScheduler;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ReservationScheduler 통합 테스트")
class ReservationSchedulerTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationScheduler reservationScheduler;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("만료된 예약 요청 실패 처리 스케줄러")
    void testProcessExpiredReservations() {
        // given
        for (int i = 1; i <= 50; i++) {
            // 예약된 좌석 생성
            ConcertSeat reservedSeat = ConcertSeat.builder()
                    .concertScheduleId(1L)
                    .seatNumber(i)
                    .price(10000)
                    .build();
            ConcertSeat savedSeat = concertSeatRepository.save(reservedSeat);

            // 만료된 예약 생성
            Reservation expiredReservation = Reservation.builder()
                    .userId((long) i)
                    .seatId(savedSeat.getId())
                    .status(ReservationStatus.PENDING)
                    .expiredAt(LocalDateTime.now().minusMinutes(5)) // 만료된 시간
                    .build();
            reservationRepository.save(expiredReservation);

            // 활성 토큰 생성
            Token activeToken = Token.builder()
                    .uuid("token-" + i)
                    .userId((long) i)
                    .status(TokenStatus.ACTIVE)
                    .expiredAt(LocalDateTime.now().plusMinutes(30))
                    .createdAt(LocalDateTime.now().minusMinutes(15))
                    .build();
            tokenRepository.save(activeToken);
        }

        // when
        reservationScheduler.processExpiredReservations();

        // then
        long failedReservationCount = reservationRepository.countByStatus(ReservationStatus.FAILED);
        assertThat(failedReservationCount).isEqualTo(50); // 만료된 예약 실패 처리

        long expiredTokenCount = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        assertThat(expiredTokenCount).isEqualTo(50); // 활성 토큰 만료 처리
    }
}