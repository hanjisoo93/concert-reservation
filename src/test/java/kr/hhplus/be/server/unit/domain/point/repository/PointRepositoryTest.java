package kr.hhplus.be.server.unit.domain.point.repository;

import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointRepositoryTest {

    @Mock
    private PointRepository pointRepository;

    @DisplayName("사용자 ID의 잔액을 조회한다.")
    @Test
    void findAllByUser_Id() {
        // given
        Point mockPoint = Point.builder()
                .userId(1L)
                .amount(30000)
                .build();

        Mockito.when(pointRepository.findAllByUserId(1L))
                .thenReturn(mockPoint);

        // when
        Point point = pointRepository.findAllByUserId(mockPoint.getUserId());

        // then
        Assertions.assertThat(point)
                .isNotNull()
                .extracting("id", "userId", "amount")
                .containsExactlyInAnyOrder(mockPoint.getId(), 1L, 30000);
    }
}