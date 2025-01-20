package kr.hhplus.be.server.interfaces.controller.concert.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kr.hhplus.be.server.domain.service.concert.schedule.ConcertScheduleService;
import kr.hhplus.be.server.interfaces.controller.concert.schedule.dto.ConcertScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/concert/schedule")
public class ConcertScheduleController {

    private final ConcertScheduleService concertScheduleService;

    @Operation(summary = "콘서트 스케줄 목록 조회", description = "콘서트 스케줄 목록을 페이징으로 조회 합니다. 현재 시간 이전 날짜는 조회되지 않습니다.", tags={ "Concert Schedule API" })
    @ApiResponse(responseCode = "200", description = "성공적으로 콘서트 스케줄 목록을 반환합니다. 결과는 배열 형식으로 제공되며, 각 항목은 콘서트 스케줄을 나타냅니다.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ConcertScheduleResponse.class))))
    @GetMapping(value = "/{concertId}", produces = "application/json")
    public Page<ConcertScheduleResponse> getConcertSchedules(@PathVariable(name = "concertId") Long concertId,
                                                             Pageable pageable) {
        return concertScheduleService.getConcertSchedules(concertId, pageable);
    }
}
