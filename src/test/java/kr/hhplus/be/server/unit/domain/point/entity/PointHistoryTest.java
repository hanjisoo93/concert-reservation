package kr.hhplus.be.server.unit.domain.point.entity;

import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PointHistoryTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("사용자 ID로 포인트 히스토리를 조회한다.")
    void findAllByUserId_success() {
        // given
        List<PointHistory> mockHistories = List.of(
                PointHistory.builder()
                        .userId(1L)
                        .changeAmount(500)
                        .pointAfterAmount(1500)
                        .changeType(PointChangeType.DEPOSIT)
                        .createdAt(LocalDateTime.now())
                        .build(),
                PointHistory.builder()
                        .userId(1L)
                        .changeAmount(-200)
                        .pointAfterAmount(1300)
                        .changeType(PointChangeType.WITHDRAWAL)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Mockito.when(pointHistoryRepository.findAllByUserId(1L)).thenReturn(mockHistories);

        // when
        List<PointHistory> histories = pointHistoryRepository.findAllByUserId(1L);

        // then
        Assertions.assertThat(histories)
                .isNotNull()
                .hasSize(2)
                .extracting("userId", "changeAmount", "pointAfterAmount", "changeType")
                .containsExactly(
                        Tuple.tuple(1L, 500, 1500, PointChangeType.DEPOSIT),
                        Tuple.tuple(1L, -200, 1300, PointChangeType.WITHDRAWAL)
                );

        Mockito.verify(pointHistoryRepository, Mockito.times(1)).findAllByUserId(1L);
    }

    @Test
    @DisplayName("포인트 충전 내역 생성 성공")
    void createPointHistory_deposit_success() {
        // given
        Long userId = 1L;
        int changeAmount = 500; // 충전 금액
        int currentAmount = 1000;
        int pointAfterAmount = currentAmount + changeAmount;

        // when
        PointHistory history = PointHistory.createPointHistory(
                userId,
                changeAmount,
                pointAfterAmount,
                PointChangeType.DEPOSIT
        );

        // then
        Assertions.assertThat(history)
                .isNotNull()
                .extracting("userId", "changeAmount", "pointAfterAmount", "changeType")
                .containsExactly(userId, changeAmount, pointAfterAmount, PointChangeType.DEPOSIT);

        Assertions.assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("포인트 사용 내역 생성 성공")
    void createPointHistory_withdrawal_success() {
        // given
        Long userId = 1L;
        int changeAmount = 300; // 사용 금액
        int currentAmount = 1000;
        int pointAfterAmount = currentAmount - changeAmount;

        // when
        PointHistory history = PointHistory.createPointHistory(
                userId,
                changeAmount,
                pointAfterAmount,
                PointChangeType.WITHDRAWAL
        );

        // then
        Assertions.assertThat(history)
                .isNotNull()
                .extracting("userId", "changeAmount", "pointAfterAmount", "changeType")
                .containsExactly(userId, changeAmount, pointAfterAmount, PointChangeType.WITHDRAWAL);

        Assertions.assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("충전 금액이 0 이하일 경우 예외 발생")
    void createPointHistory_invalidDepositAmount_throwsException() {
        // given
        Long userId = 1L;
        int changeAmount = 0; // 잘못된 충전 금액
        int currentAmount = 1000;

        // when & then
        Assertions.assertThatThrownBy(() -> PointHistory.createPointHistory(
                        userId,
                        changeAmount,
                        currentAmount + changeAmount,
                        PointChangeType.DEPOSIT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("사용 금액이 0 이하일 경우 예외 발생")
    void createPointHistory_invalidwithdrawalAmount_throwsException() {
        // given
        Long userId = 1L;
        int changeAmount = -500; // 잘못된 사용 금액 (음수)
        int currentAmount = 1000;

        // when & then
        Assertions.assertThatThrownBy(() -> PointHistory.createPointHistory(
                        userId,
                        changeAmount,
                        currentAmount - changeAmount,
                        PointChangeType.WITHDRAWAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("포인트 사용 후 잔액이 0 미만일 경우 예외 발생")
    void createPointHistory_insufficientAmount_throwsException() {
        // given
        Long userId = 1L;
        int changeAmount = 1200; // 사용 금액
        int currentAmount = 1000;
        int pointAfterAmount = currentAmount - changeAmount;

        // when & then
        Assertions.assertThatThrownBy(() -> PointHistory.createPointHistory(
                        userId,
                        changeAmount,
                        pointAfterAmount,
                        PointChangeType.WITHDRAWAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용한 포인트 잔액이 부족합니다.");
    }

}
