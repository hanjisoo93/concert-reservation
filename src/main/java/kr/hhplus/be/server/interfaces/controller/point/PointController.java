package kr.hhplus.be.server.interfaces.controller.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kr.hhplus.be.server.common.error.ErrorResponse;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.domain.service.point.PointHistoryService;
import kr.hhplus.be.server.domain.service.point.PointService;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointHistoryResponse;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointRequest;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    @Operation(summary = "잔액 조회", description = "특정 사용자의 잔액을 조회합니다.", tags={ "Point API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 잔액을 조회했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PointResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getPoint(@PathVariable Long userId) {
        Point point = pointService.getPoint(userId);
        return ResponseEntity.ok(PointResponse.of(point));
    }

    @Operation(summary = "포인트 변경 기록 조회", description = "포인트 변경 기록을 조회회한다.", tags={ "Point API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트 변경 기록이 성공적으로 저장되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PointHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @GetMapping(value = "/history/{userId}")
    public ResponseEntity<Object> getPointHistory(@PathVariable Long userId) {
        List<PointHistory> pointHistories = pointHistoryService.getPointHistories(userId);
        return ResponseEntity.ok(PointHistoryResponse.of(pointHistories));
    }

    @Operation(summary = "포인트 충전 요청", description = "특정 사용자의 포인트트을 충전합니다. 충전 시 포인트트 내역은 `PointHistory`에 기록됩니다.", tags={ "Point API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 포인트를 충전했습니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping(value = "/add")
    public ResponseEntity<String> addPoint(@RequestBody PointRequest pointRequest) {
        pointService.addPoint(pointRequest.getUserId(), pointRequest.getAmount());
        return ResponseEntity.ok("포인트 충전이 완료되었습니다.");
    }

    @Operation(summary = "포인트 사용 요청", description = "특정 사용자의 포인트을 사용합니다. 사용 시 포인트 내역은 `PointHistory`에 기록됩니다.", tags={ "Point API" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 포인트를 사용했습니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "포인트가 부족하여 요청을 처리할 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping(value = "/use")
    public ResponseEntity<String> usePoint(@RequestBody PointRequest pointRequest) {
        pointService.usePoint(pointRequest.getUserId(), pointRequest.getAmount());
        return ResponseEntity.ok("포인트 사용이 완료되었습니다.");
    }
}
