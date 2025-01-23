package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.entity.point.PointHistory;
import kr.hhplus.be.server.infra.repository.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public List<PointHistory> getPointHistories(Long userId) {
        return pointHistoryRepository.findPointHistoriesByUserId(userId);
    }

    @Transactional
    public void createPointHistory(Long userId, int changeAmount, int pointAfterAmount, PointChangeType changeType) {
        try {
            // 1. 포인트 히스토리 검증 및 생성
            PointHistory pointHistory = PointHistory.createPointHistory(userId, changeAmount, pointAfterAmount, changeType);

            // 2. 포인트 히스토리 저장
            pointHistoryRepository.save(pointHistory);

            log.info("포인트 히스토리 완료 - userId={}, changeAmount={}, pointAfterAmount={}, changeType={}", userId, changeAmount, pointAfterAmount, changeType);
        } catch (Exception e) {
            log.error("포인트 히스토리 실패 - userId={}, changeAmount={}, changeType={}", userId, changeAmount, changeType, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
