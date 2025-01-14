package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.interfaces.controller.point.dto.PointHistoryResponse;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
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
    public List<PointHistory> getPointHistories(Long userId) {
        return pointHistoryRepository.findAllByUserId(userId);
    }

    @Transactional
    public PointHistory processPointHistory(Long userId, int changeAmount, PointChangeType changeType) {
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

    public PointHistory createPointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType) {

        // 1. 포인트 히스토리 검증 및 생성
        PointHistory pointHistory = PointHistory.createPointHistory(userId, changeAmount, pointAfterAmount, changeType);

        // 2. 포인트 히스토리 저장
        pointHistoryRepository.save(pointHistory);

        return pointHistory;
    }
}
