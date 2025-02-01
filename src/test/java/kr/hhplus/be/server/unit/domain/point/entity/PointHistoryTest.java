package kr.hhplus.be.server.unit.domain.point.entity;

import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.exception.point.PointHistoryException;
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

        Mockito.when(pointHistoryRepository.findPointHistoriesByUserId(1L)).thenReturn(mockHistories);

        // when
        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(1L);

        // then
        Assertions.assertThat(histories)
                .isNotNull()
                .hasSize(2)
                .extracting("userId", "changeAmount", "pointAfterAmount", "changeType")
                .containsExactly(
                        Tuple.tuple(1L, 500, 1500, PointChangeType.DEPOSIT),
                        Tuple.tuple(1L, -200, 1300, PointChangeType.WITHDRAWAL)
                );

        Mockito.verify(pointHistoryRepository, Mockito.times(1)).findPointHistoriesByUserId(1L);
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
}
