package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public List<PointHistory> getPointHistories(Long userId) {
        return pointHistoryRepository.findAllByUserId(userId);
    }

    @Transactional
    public void createPointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType) {
        // 1. 포인트 히스토리 검증 및 생성
        PointHistory pointHistory = PointHistory.createPointHistory(userId, changeAmount, pointAfterAmount, changeType);

        // 2. 포인트 히스토리 저장
        pointHistoryRepository.save(pointHistory);
    }
}
