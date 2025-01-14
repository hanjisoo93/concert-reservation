package kr.hhplus.be.server.unit.domain.concert.seat.repository;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeatStatus;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(MockitoExtension.class)
class ConcertSeatRepositoryTest {

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @DisplayName("콘서트 좌석 ID로 좌석 정보를 조회한다")
    @Test
    void findAllById() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .id((1L))
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .status(ConcertSeatStatus.AVAILABLE)
                .build();
        Mockito.when(concertSeatRepository.findAllById(1L))
                .thenReturn(mockConcertSeat);

        // when
        ConcertSeat concertSeat = concertSeatRepository.findAllById(mockConcertSeat.getId());

        // then
        Assertions.assertThat(concertSeat)
                .isNotNull()
                .extracting("id", "concertScheduleId", "seatNumber", "price", "status")
                .containsExactly(mockConcertSeat.getId(), mockConcertSeat.getConcertScheduleId(), 25, 50000, ConcertSeatStatus.AVAILABLE);
    }

    @DisplayName("콘서트 ID 로 좌석의 목록을 조회한다.")
    @Test
    void findAllByConcertScheduleId() {
        // given
        List<ConcertSeat> mockSeats = List.of(
                ConcertSeat.builder()
                        .id(1L)
                        .concertScheduleId(1L)
                        .seatNumber(25)
                        .price(50000)
                        .status(ConcertSeatStatus.AVAILABLE)
                        .build(),
                ConcertSeat.builder()
                        .id(2L)
                        .concertScheduleId(1L)
                        .seatNumber(26)
                        .price(50000)
                        .status(ConcertSeatStatus.AVAILABLE)
                        .build()
        );

        Mockito.when(concertSeatRepository.findAllByConcertScheduleId(1L))
                .thenReturn(mockSeats);

        // when
        List<ConcertSeat> concertSeats = concertSeatRepository.findAllByConcertScheduleId(1L);

        // then
        Assertions.assertThat(concertSeats)
                .isNotNull()
                .hasSize(2)
                .extracting("id", "concertScheduleId", "seatNumber", "price", "status")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, 25, 50000, ConcertSeatStatus.AVAILABLE),
                        tuple(2L, 1L, 26, 50000, ConcertSeatStatus.AVAILABLE)
                );
    }

    @DisplayName("콘서트 스케줄 ID 에 해당하는 좌석이 없는 경우 Exception 처리를 한다.")
    @Test
    void findAllByConcertScheduleId_whenSeatsNotFound_throwsException() {
        // given
        Long concertScheduleId = 1L;
        Mockito.when(concertSeatRepository.findAllByConcertScheduleId(concertScheduleId))
                .thenReturn(Collections.emptyList());

        // when & then
        Assertions.assertThatThrownBy(() -> {
                    List<ConcertSeat> concertSeats = concertSeatRepository.findAllByConcertScheduleId(concertScheduleId);
                    if (concertSeats.isEmpty()) {
                        throw new IllegalArgumentException("해당 날짜에 콘서트 좌석이 없습니다.");
                    }
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 날짜에 콘서트 좌석이 없습니다.");

        Mockito.verify(concertSeatRepository).findAllByConcertScheduleId(concertScheduleId);
    }
}
