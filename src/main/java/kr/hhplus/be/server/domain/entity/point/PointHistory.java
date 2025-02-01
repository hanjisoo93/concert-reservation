package kr.hhplus.be.server.domain.entity.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.point.PointHistoryException;
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
                throw new PointHistoryException(ErrorCode.INVALID_POINT_CHARGE);
            }
        }

        if(PointChangeType.WITHDRAWAL.equals(changeType)) {
            if(changeAmount <= 0) {
                throw new PointHistoryException(ErrorCode.INVALID_POINT_USAGE);
            }

            if(pointAfterAmount < 0) {
                throw new PointHistoryException(ErrorCode.INSUFFICIENT_POINT);
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
