package kr.hhplus.be.server.api.facade.reservation;

import kr.hhplus.be.server.application.facade.reservation.ReservationFacade;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeatStatus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ReservationFacade 통합 테스트")
class ReservationFacadeTest {
    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void tearDown () {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유효한 좌석과 토큰으로 예약 성공")
    void createReservation_success() {
        // given
        Long userId = 1L;

        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(50)
                .price(10000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        Token token = Token.createToken(userId);
        token.updateStatus(TokenStatus.ACTIVE);
        tokenRepository.save(token);

        // when
        Reservation reservation = reservationFacade.createReservation(savedSeat.getId(), userId);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(userId);
        assertThat(reservation.getSeatId()).isEqualTo(savedSeat.getId());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        ConcertSeat updatedSeat = concertSeatRepository.findById(savedSeat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(ConcertSeatStatus.RESERVED);

        Token updatedToken = tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
        assertThat(updatedToken.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("이미 예약된 좌석으로 예약 실패")
    void createReservation_alreadyReserved() {
        // given
        Long userId = 1L;

        // 예약 중 좌석 데이터 생성
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .price(10000)
                .status(ConcertSeatStatus.RESERVED)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        // 활성 토큰 데이터 생성
        Token token = Token.createToken(userId);
        token.updateStatus(TokenStatus.ACTIVE);
        tokenRepository.save(token);

        // when & then
        assertThatThrownBy(() -> reservationFacade.createReservation(savedSeat.getId(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 예약 중인 좌석입니다.");
    }

    @Test
    @DisplayName("유효한 토큰이 없는 경우 예약 실패")
    void createReservation_noValidToken() {
        // given
        Long userId = 1L;

        // 좌석 데이터 생성
        ConcertSeat seat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(1)
                .price(10000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        ConcertSeat savedSeat = concertSeatRepository.save(seat);

        // when & then
        assertThatThrownBy(() -> reservationFacade.createReservation(savedSeat.getId(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효한 토큰이 없습니다.");
    }
}