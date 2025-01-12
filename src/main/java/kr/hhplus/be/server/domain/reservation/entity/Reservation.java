package kr.hhplus.be.server.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    @Builder
    private Reservation(Long id, Long userId, Long seatId, ReservationStatus status, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.seatId = seatId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public static Reservation createReservation(Long userId, Long seatId) {
        return Reservation.builder()
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    public void updateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("유효하지 않은 예약 상태입니다.");
        }
        this.status = status;
    }

    public void updateExpiredAt(LocalDateTime expiredAt) {
        if (expiredAt == null || expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("유효하지 않은 만료 시간입니다.");
        }
        this.expiredAt = expiredAt;
    }

    public boolean isExpired(LocalDateTime expiredAt) {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}
