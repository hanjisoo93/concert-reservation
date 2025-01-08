package kr.hhplus.be.server.domain.concert.seat.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.schedule.entity.ConcertSchedule;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertScheduleId;

    private int seatNumber;

    private int price;

    @Enumerated(EnumType.STRING)
    private ConcertSeatStatus status;

    @Builder
    private ConcertSeat(Long id, Long concertScheduleId, int seatNumber, int price, ConcertSeatStatus status) {
        this.id = id;
        this.concertScheduleId = concertScheduleId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }
}
