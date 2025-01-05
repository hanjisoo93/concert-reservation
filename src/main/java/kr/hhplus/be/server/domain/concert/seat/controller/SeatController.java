package kr.hhplus.be.server.domain.concert.seat.controller;

import kr.hhplus.be.server.domain.concert.seat.dto.SeatResponse;
import kr.hhplus.be.server.domain.concert.seat.dto.SeatReservationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concert/seats")
public class SeatController {

    private final List<SeatResponse> seats = new ArrayList<>();

    public SeatController() {
        seats.add(new SeatResponse(1, 1, 1, "AVAILABLE", null, null));
        seats.add(new SeatResponse(2, 1, 2, "AVAILABLE", null, null));
        seats.add(new SeatResponse(3, 1, 3, "RESERVED", "mock-uuid-12345", LocalDateTime.now().toString()));
        seats.add(new SeatResponse(4, 2, 1, "CONFIRMED", "mock-uuid-67890", LocalDateTime.now().toString()));
    }

    /**
     * 예약 가능 좌석 리스트 조회
     */
    @GetMapping
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(
            @RequestParam Integer scheduleId,
            @RequestParam(required = false, defaultValue = "AVAILABLE") String status
    ) {
        // 조건에 맞는 좌석 필터링
        List<SeatResponse> filteredSeats = seats.stream()
                .filter(seat -> seat.getConcertScheduleId().equals(scheduleId) && seat.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredSeats);
    }

    /**
     * 좌석 상세 조회
     */
    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponse> getSeatDetail(@PathVariable Integer seatId) {
        // 특정 ID에 맞는 좌석 조회
        return seats.stream()
                .filter(seat -> seat.getId().equals(seatId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 좌석 예약 요청
     */
    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<SeatResponse> reserveSeat(
            @PathVariable Integer seatId,
            @RequestBody SeatReservationRequest seatReservationRequest
    ) {
        for (SeatResponse seat : seats) {
            if (seat.getId().equals(seatId) && seat.getStatus().equalsIgnoreCase("AVAILABLE")) {
                // 좌석 예약 처리
                seat.setStatus("RESERVED");
                seat.setOwnerUuid(seatReservationRequest.getOwnerUuid());
                seat.setReservationTime(LocalDateTime.now().toString());
                return ResponseEntity.ok(seat);
            }
        }
        return ResponseEntity.badRequest().body(null);
    }

    /**
     * 좌석 예약 확정
     */
    @PostMapping("/{seatId}/confirm")
    public ResponseEntity<SeatResponse> confirmSeat(
            @PathVariable Integer seatId,
            @RequestBody SeatReservationRequest seatReservationRequest
    ) {
        for (SeatResponse seat : seats) {
            if (seat.getId().equals(seatId) && seat.getStatus().equalsIgnoreCase("RESERVED")
                    && seat.getOwnerUuid().equals(seatReservationRequest.getOwnerUuid())) {
                // 좌석 예약 확정 처리
                seat.setStatus("CONFIRMED");
                return ResponseEntity.ok(seat);
            }
        }
        return ResponseEntity.badRequest().body(null);
    }
}
