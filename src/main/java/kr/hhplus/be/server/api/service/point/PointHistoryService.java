package kr.hhplus.be.server.api.service.point;

import kr.hhplus.be.server.api.controller.point.dto.PointHistoryResponse;
import kr.hhplus.be.server.domain.point.entity.Point;
import kr.hhplus.be.server.domain.point.entity.PointChangeType;
import kr.hhplus.be.server.domain.point.entity.PointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public List<PointHistoryResponse> getPointHistories(Long userId) {
        List<PointHistory> pointHistories = pointHistoryRepository.findAllByUserId(userId);
        return PointHistoryResponse.of(pointHistories);
    }

    @Transactional
    public PointHistoryResponse processPointHistory(Long userId, int changeAmount, PointChangeType changeType) {
        // 1. 현재 포인트 조회
        Point currentPoint = pointRepository.findAllByUserId(userId);

        // 2. 포인트 변경 계산
        int pointAfterAmount = calculatePoint(currentPoint, changeAmount, changeType);

        // 2. 포인트 히스토리 생성
        return createPointHistory(userId, changeAmount, pointAfterAmount, changeType);
    }

    private int calculatePoint(Point point, int amount, PointChangeType changeType) {
        int pointAfterAmount;
        if (PointChangeType.DEPOSIT.equals(changeType)) {
            pointAfterAmount = point.getAmount() + amount;
        } else if (PointChangeType.WITHDRAWAL.equals(changeType)) {
            if (point.getAmount() < amount) {
                throw new IllegalArgumentException("사용한 포인트 잔액이 부족합니다.");
            }
            pointAfterAmount = point.getAmount() - amount;
        } else {
            throw new IllegalArgumentException("유효하지 않은 포인트 변경 타입입니다.");
        }
        return pointAfterAmount;
    }

    public PointHistoryResponse createPointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType) {

        // 1. 포인트 히스토리 검증 및 생성
        PointHistory pointHistory = PointHistory.createPointHistory(userId, changeAmount, pointAfterAmount, changeType);

        // 2. 포인트 히스토리 저장
        pointHistoryRepository.save(pointHistory);

        return PointHistoryResponse.builder()
                .userId(pointHistory.getUserId())
                .changeAmount(pointHistory.getChangeAmount())
                .pointAfterAmount(pointHistory.getPointAfterAmount())
                .changeType(pointHistory.getChangeType())
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}
