package kr.hhplus.be.server.unit.domain.concert.schedule.repository;

import kr.hhplus.be.server.domain.entity.concert.schedule.ConcertSchedule;
import kr.hhplus.be.server.infra.repository.concert.schedule.ConcertScheduleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ConcertScheduleRepositoryTest {

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @DisplayName("concertId와 concertDate 조건에 따른 데이터 필터링 및 페이징을 검증한다.")
    @Test
    void findAllByConcertIdAndConcertDateAfter_withPaging() {
        // given
        LocalDate currentDate = LocalDate.of(2025, 1, 6);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("concertDate").ascending());

        List<ConcertSchedule> allSchedules = List.of(
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 1)).build(),
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 2)).build(),
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 6)).build(),
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 7)).build(),
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 8)).build(),
                ConcertSchedule.builder().concertId(1L).concertDate(LocalDate.of(2025, 1, 9)).build()
        );

        // Mocking: 페이징 처리 포함
        Mockito.when(concertScheduleRepository.findAllByConcertIdAndConcertDateAfter(1L, currentDate, pageable))
                .thenAnswer(invocation -> {
                    List<ConcertSchedule> filtered = allSchedules.stream()
                            .filter(schedule -> schedule.getConcertId() == 1L && schedule.getConcertDate().isAfter(currentDate))
                            .sorted(Comparator.comparing(ConcertSchedule::getConcertDate))
                            .toList();

                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), filtered.size());
                    return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
                });

        // when
        Page<ConcertSchedule> result = concertScheduleRepository.findAllByConcertIdAndConcertDateAfter(1L, currentDate, pageable);

        // then
        Assertions.assertThat(result.getContent())
                .hasSize(2) // 현재 페이지의 데이터 크기
                .extracting("concertDate")
                .containsExactly(
                        LocalDate.of(2025, 1, 7),
                        LocalDate.of(2025, 1, 8)
                );

        Assertions.assertThat(result.getTotalElements()).isEqualTo(3); // 필터링된 전체 데이터 개수
        Assertions.assertThat(result.getTotalPages()).isEqualTo(2);    // 총 페이지 수
        Assertions.assertThat(result.isLast()).isFalse();                       // 마지막 페이지 여부
    }
}