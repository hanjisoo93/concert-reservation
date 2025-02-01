package kr.hhplus.be.server.domain.entity.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.reservation.ReservationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reservation_optimistic", uniqueConstraints = {@UniqueConstraint(name = "uq_seat_id", columnNames = {"seat_id"})})
public class ReservationOptimistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "seat_id", unique = true)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    @Version
    private Integer version; // 낙관적 락 전용을 위한 버전 필드 추가

    @Builder
    private ReservationOptimistic(Long id, Long userId, Long seatId, ReservationStatus status, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public static ReservationOptimistic createReservation(Long userId, Long seatId) {
        return ReservationOptimistic.builder()
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }
}
