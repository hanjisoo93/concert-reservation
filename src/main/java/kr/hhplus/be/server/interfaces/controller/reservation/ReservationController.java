package kr.hhplus.be.server.interfaces.controller.reservation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.facade.reservation.ReservationFacade;
import kr.hhplus.be.server.domain.service.reservation.ReservationService;
import kr.hhplus.be.server.interfaces.controller.reservation.dto.ReservationReserveRequest;
import kr.hhplus.be.server.common.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/concert/reservation")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @Operation(summary = "콘서트 좌석 예약 요청", description = "콘서트 과석 예약 요청을 수행하며, 대기열 검증을 포함합니다.", tags={ "Reservation API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌석이 성공적으로 예약 요청 되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청한 좌석 ID를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping(value = "/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> reserveSeat(@RequestHeader("Authorization") String token,
                                              @RequestBody @Valid ReservationReserveRequest reservationRequest) {
        reservationFacade.reserve(reservationRequest.getUserId(), reservationRequest.getSeatId(), token);
        return ResponseEntity.ok("좌석이 성공적으로 예약 요청 되었습니다.");
    }
}
