package kr.hhplus.be.server.api.controller.concert.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.api.service.concert.schedule.ConcertScheduleService;
import kr.hhplus.be.server.api.controller.concert.schedule.dto.ConcertScheduleResponse;
import kr.hhplus.be.server.common.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 콘서트 스케줄 목록을 반환합니다. 결과는 배열 형식으로 제공되며, 각 항목은 콘서트 스케줄을 나타냅니다.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ConcertScheduleResponse.class)))),
            @ApiResponse(responseCode = "404", description = "해당 콘서트의 콘서트 스케줄을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{concertId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Page<ConcertScheduleResponse>> getConcertSchedules(@PathVariable(name = "concertId") Long concertId,
                                                                             Pageable pageable) {
        Page<ConcertScheduleResponse> concertSchedules = concertScheduleService.getConcertSchedules(concertId, pageable);

        if(concertSchedules == null || concertSchedules.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Page.empty(pageable)); // 빈 페이지 반환
        }
        return ResponseEntity.ok(concertSchedules);
    }
}
