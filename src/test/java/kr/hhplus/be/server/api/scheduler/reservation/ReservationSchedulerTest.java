package kr.hhplus.be.server.api.scheduler.reservation;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.api.scheduler.token.TokenScheduler;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeatStatus;
import kr.hhplus.be.server.domain.concert.seat.repository.ConcertSeatRepository;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.token.entity.Token;
import kr.hhplus.be.server.domain.token.entity.TokenStatus;
import kr.hhplus.be.server.domain.token.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
                    .status(ConcertSeatStatus.RESERVED)
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

        long availableSeatCount = concertSeatRepository.countByStatus(ConcertSeatStatus.AVAILABLE);
        assertThat(availableSeatCount).isEqualTo(50); // 좌석 상태 복원

        long expiredTokenCount = tokenRepository.countByStatus(TokenStatus.EXPIRED);
        assertThat(expiredTokenCount).isEqualTo(50); // 활성 토큰 만료 처리
    }
}