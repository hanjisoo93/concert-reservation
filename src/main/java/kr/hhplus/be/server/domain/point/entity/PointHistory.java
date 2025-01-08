package kr.hhplus.be.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int changeAmount;

    private int pointAfterAmount;

    @Enumerated(EnumType.STRING)
    private PointChangeType changeType;

    private LocalDateTime createdAt;

    @Builder
    private PointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType, LocalDateTime createdAt) {
        this.userId = userId;
        this.changeAmount = changeAmount;
        this.pointAfterAmount = pointAfterAmount;
        this.changeType = changeType;
        this.createdAt = createdAt;
    }

    public static PointHistory createPointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType) {
        if(PointChangeType.DEPOSIT.equals(changeType)) {
            if (changeAmount <= 0) {
                throw new IllegalArgumentException("충전할 포인트는 1 이상이어야 합니다.");
            }
        }

        if(PointChangeType.WITHDRAWAL.equals(changeType)) {
            if(changeAmount <= 0) {
                throw new IllegalArgumentException("사용할 포인트는 1 이상이어야 합니다.");
            }

            if(pointAfterAmount < 0) {
                throw new IllegalArgumentException("사용한 포인트 잔액이 부족합니다.");
            }
        }

        return PointHistory.builder()
                .userId(userId)
                .changeAmount(changeAmount)
                .pointAfterAmount(pointAfterAmount)
                .changeType(changeType)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
