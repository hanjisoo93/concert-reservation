package kr.hhplus.be.server.domain.service.concert.seat;

import kr.hhplus.be.server.domain.entity.concert.seat.ConcertSeat;
import kr.hhplus.be.server.domain.exception.concert.seat.ConcertSeatNotFoundException;
import kr.hhplus.be.server.infra.repository.concert.seat.ConcertSeatRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ConcertSeatServiceTest {

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @InjectMocks
    private ConcertSeatService concertSeatService;

    @DisplayName("콘서트 좌석 ID로 좌석 정보를 조회한다")
    @Test
    void findAllById() {
        // given
        ConcertSeat mockConcertSeat = ConcertSeat.builder()
                .id((1L))
                .concertScheduleId(1L)
                .seatNumber(25)
                .price(50000)
                .build();
        Mockito.when(concertSeatRepository.findConcertSeatById(1L))
                .thenReturn(Optional.of(mockConcertSeat));

        // when
        ConcertSeat concertSeat = concertSeatService.getConcertSeat(mockConcertSeat.getId());

        // then
        Assertions.assertThat(concertSeat)
                .isNotNull()
                .extracting("id", "concertScheduleId", "seatNumber", "price")
                .containsExactly(
                        mockConcertSeat.getId(),
                        mockConcertSeat.getConcertScheduleId(),
                        25,
                        50000
                );
    }

    @DisplayName("존재하지 않는 좌석 ID로 조회 시 예외를 발생시킨다")
    @Test
    void findConcertSeatById_NotFound() {
        // given
        Mockito.when(concertSeatRepository.findConcertSeatById(999L))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> concertSeatService.getConcertSeat(999L))
                .isInstanceOf(ConcertSeatNotFoundException.class)
                .hasMessageContaining("존재 하는 좌석이 없습니다.");
    }
}