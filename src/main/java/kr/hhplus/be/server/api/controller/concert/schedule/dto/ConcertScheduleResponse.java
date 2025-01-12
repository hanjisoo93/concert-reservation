package kr.hhplus.be.server.api.controller.concert.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.common.page.BasePageResponse;
import kr.hhplus.be.server.domain.concert.schedule.entity.ConcertSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ConcertScheduleResponse extends BasePageResponse {
    @Schema(required = true, description = "콘서트 스케줄 ID")
    private Long id;

    @Schema(required = true, description = "콘서트 ID")
    private Long concertId;

    @Schema(required = true, description = "콘서트 날짜")
    private LocalDate concertDate;

    @Builder
    private ConcertScheduleResponse(Long id, Long concertId, LocalDate concertDate) {
        this.id = id;
        this.concertId = concertId;
        this.concertDate = concertDate;
    }

    public static ConcertScheduleResponse of(ConcertSchedule concertSchedule) {
        return ConcertScheduleResponse.builder()
                .id(concertSchedule.getId())
                .concertId(concertSchedule.getConcertId())
                .concertDate(concertSchedule.getConcertDate())
                .build();
    }

    // 리스트 변환
    public static List<ConcertScheduleResponse> of(List<ConcertSchedule> concertSchedules) {
        return concertSchedules.stream()
                .map(ConcertScheduleResponse::of)
                .collect(Collectors.toList());
    }
}
