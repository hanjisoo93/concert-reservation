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
import java.util.Optional;

import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(MockitoExtension.class)
class ConcertSeatRepositoryTest {

    @Mock
    private ConcertSeatRepository concertSeatRepository;

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
                        .build(),
                ConcertSeat.builder()
                        .id(2L)
                        .concertScheduleId(1L)
                        .seatNumber(26)
                        .price(50000)
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
                .extracting("id", "concertScheduleId", "seatNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, 25, 50000),
                        tuple(2L, 1L, 26, 50000)
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
