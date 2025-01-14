package kr.hhplus.be.server.domain.entity.concert.seat;

import jakarta.persistence.*;
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

    public void updateStatus(ConcertSeatStatus status) {
        if(status == null) {
            throw new IllegalArgumentException("유효하지 않은 좌석 상태입니다.");
        }
        this.status = status;
    }
}
