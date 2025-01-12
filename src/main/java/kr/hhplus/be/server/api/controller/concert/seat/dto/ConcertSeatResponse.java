package kr.hhplus.be.server.api.controller.concert.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeat;
import kr.hhplus.be.server.domain.concert.seat.entity.ConcertSeatStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ConcertSeatResponse {

    @Schema(required = true, description = "콘서트 좌석 ID")
    private Long id;

    @Schema(required = true, description = "콘서트 스케줄 정보 ID")
    private Long concertScheduleId;

    @Schema(required = true, description = "좌석 번호")
    @NotNull
    private int seatNumber;

    @Schema(required = true, description = "좌석 가격")
    private int price;

    @Schema(required = true, description = "좌석 상태")
    @NotNull
    private ConcertSeatStatus status;

    @Builder
    private ConcertSeatResponse(Long concertScheduleId, int seatNumber, int price, ConcertSeatStatus status) {
        this.concertScheduleId = concertScheduleId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }

    public static ConcertSeatResponse of(ConcertSeat concertSeat) {
        return ConcertSeatResponse.builder()
                .concertScheduleId(concertSeat.getConcertScheduleId())
                .status(concertSeat.getStatus())
                .price(concertSeat.getPrice())
                .seatNumber(concertSeat.getSeatNumber())
                .build();
    }

    // 리스트 변환
    public static List<ConcertSeatResponse> of(List<ConcertSeat> concertSeats) {
        return concertSeats.stream()
                .map(ConcertSeatResponse::of)
                .collect(Collectors.toList());
    }
}
