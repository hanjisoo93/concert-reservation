package kr.hhplus.be.server.domain.entity.point;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.naming.InsufficientResourcesException;

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
    public void userPoint(int amount){
        if(isAmountLessThan(amount)) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        if(amount <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 1 이상이어야 합니다.");
        }
        this.amount -= amount;
    }

    // 포인트 충전
    public void addPoint(int amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("충전할 포인트는 1 이상이어야 합니다.");
        }
        this.amount += amount;
    }
}
