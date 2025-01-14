package kr.hhplus.be.server.application.facade.reservation;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.domain.entity.reservation.Reservation;
import kr.hhplus.be.server.domain.entity.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.exception.concert.seat.ConcertSeatNotFoundException;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import kr.hhplus.be.server.infra.repository.reservation.ReservationRepository;
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
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ReservationFacade 통합 테스트")
class ReservationFacadeTest {

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        concertSeatRepository.deleteAllInBatch();
        tokenRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("좌석 예약 성공")
    void reserveSeatSuccessfully() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Token mockToken = Token.builder()
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Token savedToken = tokenRepository.save(mockToken);

        Long userId = 1L;
        Long seatId = savedConcertSeat.getId();

        // when
        reservationFacade.reserve(userId, seatId, savedToken.getUuid());

        // then
        Reservation reservation = reservationRepository.findAll().get(0);
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(userId);
        assertThat(reservation.getSeatId()).isEqualTo(seatId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        Token updatedToken = tokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
        assertThat(updatedToken.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("이미 예약된 좌석 예외 발생")
    void reserveAlreadyReservedSeat() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        ConcertSeat savedConcertSeat = concertSeatRepository.save(mockConcertSeat);

        Token token = Token.builder()
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Token savedToken = tokenRepository.save(token);

        Reservation reservation = Reservation.createReservation(1L, savedConcertSeat.getId());
        reservationRepository.save(reservation);

        Long userId = 1L;
        Long seatId = savedConcertSeat.getId();

        // when & then
        assertThatThrownBy(() -> reservationFacade.reserve(userId, seatId, savedToken.getUuid()))
                .isInstanceOf(ReservationException.class)
                .hasMessageContaining("좌석 [" + seatId + "]은 이미 예약된 좌석입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 좌석 예외 발생")
    void reserveInvalidSeat() {
        // given
        Token token = Token.builder()
                .userId(1L)
                .status(TokenStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .createdAt(LocalDateTime.now())
                .build();
        Token savedToken = tokenRepository.save(token);

        Long userId = 1L;
        Long invalidSeatId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationFacade.reserve(userId, invalidSeatId, savedToken.getUuid()))
                .isInstanceOf(ConcertSeatNotFoundException.class)
                .hasMessageContaining("존재 하는 좌석이 없습니다.");
    }
}