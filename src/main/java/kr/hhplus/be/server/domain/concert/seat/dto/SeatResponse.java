package kr.hhplus.be.server.domain.concert.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Integer id;
    private Integer concertScheduleId;
    private Integer seatNumber;
    private String status;
    private String ownerUuid;
    private String reservationTime;
}
