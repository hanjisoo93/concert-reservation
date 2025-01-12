package kr.hhplus.be.server.api.controller.concert.seat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.api.controller.concert.seat.dto.ConcertSeatResponse;
import kr.hhplus.be.server.api.service.concert.seat.ConcertSeatService;
import kr.hhplus.be.server.common.error.ErrorResponse;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/concert/seats")
public class ConcertSeatController {

    private final ConcertSeatService concertSeatService;

    @Operation(summary = "콘서트 좌석 목록 조회", description = "콘서트 스케줄 ID를 기반으로 좌석 목록을 조회합니다.", tags={ "Concert Seat API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 좌석 목록을 반환합니다. 결과는 배열 형식으로 제공되며, 각 항목은 좌석 정보를 나타냅니다.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ConcertSeatResponse.class)))),
            @ApiResponse(responseCode = "404", description = "주어진 콘서트 스케줄 ID에 해당하는 좌석 정보가 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping
    public ResponseEntity<Object> getConcertSeats(@RequestParam("concertScheduleId") Long concertScheduleId) {
        List<ConcertSeatResponse> concertSeats = concertSeatService.getConcertSeats(concertScheduleId);
        if (concertSeats == null || concertSeats.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("해당 날짜에 콘서트 좌석이 없습니다."));
        }
        return ResponseEntity.ok(concertSeats);
    }

    @Operation(summary = "콘서트 좌석 상세 조회", description = "콘서트 좌석 ID를 기반으로 좌석 상세 정보를 조회합니다.", tags={ "Concert Seat API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 좌석 정보를 조회했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConcertSeatResponse.class))),
            @ApiResponse(responseCode = "404", description = "No seat found with the given ID.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{concertSeatId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> getConcertSeat(@PathVariable Long concertSeatId) {
        ConcertSeatResponse concertSeatResponse = concertSeatService.getConcertSeat(concertSeatId);
        if(concertSeatResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("해당 좌석을 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(concertSeatResponse);
    }
}
