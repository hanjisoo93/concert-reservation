package kr.hhplus.be.server.unit.domain.point.entity;

import kr.hhplus.be.server.domain.point.entity.Point;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PointTest {

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoint_success() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .amount(100)
                .build();

        // when
        point.userPoint(50);

        // then
        Assertions.assertThat(point.getAmount()).isEqualTo(50);
    }

    @Test
    @DisplayName("포인트 부족으로 사용 실패")
    void usePoint_insufficientPoints_throwsException() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .amount(30)
                .build();

        // when & then
        Assertions.assertThatThrownBy(() -> point.userPoint(50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("0 이하의 포인트 사용 시 실패")
    void usePoint_negativeOrZeroAmount_throwsException() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .amount(100)
                .build();

        // when & then
        Assertions.assertThatThrownBy(() -> point.userPoint(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트는 1 이상이어야 합니다.");

        Assertions.assertThatThrownBy(() -> point.userPoint(-10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void addPoint_success() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .amount(100)
                .build();

        // when
        point.addPoint(50);

        // then
        Assertions.assertThat(point.getAmount()).isEqualTo(150);
    }

    @Test
    @DisplayName("0 이하의 포인트 충전 시 실패")
    void addPoint_negativeOrZeroAmount_throwsException() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .amount(100)
                .build();

        // when & then
        Assertions.assertThatThrownBy(() -> point.addPoint(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 1 이상이어야 합니다.");

        Assertions.assertThatThrownBy(() -> point.addPoint(-10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 1 이상이어야 합니다.");
    }
}