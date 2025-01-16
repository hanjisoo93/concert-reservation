package kr.hhplus.be.server.domain.entity.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.exception.point.PointException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int amount;

    @Builder
    private Point(Long userId, int amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public boolean isAmountLessThan(int amount) {
        return this.amount < amount;
    }

    // 포인트 사용
    public void usePoint(int amount){
        if(isAmountLessThan(amount)) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT);
        }

        if(amount <= 0) {
            throw new PointException(ErrorCode.INVALID_POINT_USAGE);
        }
        this.amount -= amount;
    }

    // 포인트 충전
    public void addPoint(int amount) {
        if(amount <= 0) {
            throw new PointException(ErrorCode.INVALID_POINT_CHARGE);
        }
        this.amount += amount;
    }
}
