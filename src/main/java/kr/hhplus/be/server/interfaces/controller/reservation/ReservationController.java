package kr.hhplus.be.server.interfaces.controller.reservation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.application.aop.ValidateToken;
import kr.hhplus.be.server.interfaces.controller.reservation.dto.ReservationConfirmRequest;
import kr.hhplus.be.server.interfaces.controller.reservation.dto.ReservationReserveRequest;
import kr.hhplus.be.server.common.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concert/reservation")
public class ReservationController {

    @Operation(summary = "콘서트 좌석 예약 요청", description = "콘서트 과석 예약 요청을 수행하며, 대기열 검증을 포함합니다.", tags={ "Reservation API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌석이 성공적으로 예약 요청 되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청한 좌석 ID를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "요청한 좌석이 이미 예약되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/reserve", consumes = "application/json", produces = "application/json")
    @ValidateToken
    public ResponseEntity<String> reserveReservation(@RequestBody ReservationReserveRequest reservationRequest){
        return ResponseEntity.ok("좌석이 성공적으로 예약 요청 되었습니다.");
    }

    @Operation(summary = "콘서트 좌석 예약 결과 처리", description = "콘서트 좌석 예약 결과 처리합니다.", tags={ "Reservation API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘서트 좌석 예약 결과가 성공적으로 처리 되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청한 좌석 ID를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "요청한 좌석이 이미 예약 상태로 충돌했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/confirm", consumes = "application/json", produces = "application/json")
    @ValidateToken
    public ResponseEntity<String> confirmReservation(@RequestBody ReservationConfirmRequest reservationRequest){
        return ResponseEntity.ok("콘서트 좌석 예약 결과가 성공적으로 처리 되었습니다.");
    }
}
