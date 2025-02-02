package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryService pointHistoryService;

    @Transactional(readOnly = true)
    public Point getPoint(Long userId) {
        try {
            return pointRepository.findPointByUserId(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));
        } catch (PointException e) {
            log.info("포인트 조회 실패 - userId={}", userId);
            throw e;
        } catch (Exception e) {
            log.error("포인트 조회 중 시스템 오류 발생 - userId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 포인트 충전 처리
     */
    @Transactional
    public void creditPoint(Long userId, int amount) {
        // 1. 포인트 충전 처리
        Point currentPoint = addPoint(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.DEPOSIT);
    }

    /**
     * 포인트 사용 처리
     */
    @Transactional
    public void spendPoint(Long userId, int amount) {
        // 1. 포인트 사용 처리
        Point currentPoint = usePoint(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.WITHDRAWAL);
    }

    /**
     * 포인트 충전
     */
    @Transactional
    public Point addPoint(Long userId, int amount) {
        try {
            // 1. 포인트 조회
            Point currentPoint = pointRepository.findPointByUserId(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));
            currentPoint.addPoint(amount);
            log.info("포인트 츙전 - userId={}, addedAmount={}, totalAmount={}", userId, amount, currentPoint.getAmount());
            return currentPoint;
        } catch (PointException e) {
            log.warn("포인트 충전 실패 - userId={}, amount={}", userId, amount, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 충전 중 시스템 오류 발생 - userId={}, amount={}", userId, amount, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public Point usePoint(Long userId, int amount) {
        try {
            // 1. 포인트 조회
            Optional<Point> currentPoint = pointRepository.findPointByUserId(userId);

            // 2. 잔액 검증
            if(!currentPoint.isPresent()){
                log.warn("포인트 사용 불가 계정 - userId={}", userId);
                throw new PointException(ErrorCode.POINT_NOT_FOUND);
            }

            // 3. 포인트 차감
            currentPoint.get().usePoint(amount);
            log.info("포인트 사용 완료 - userId={}, deductedAmount={}, remainingAmount={}", userId, amount, currentPoint.get().getAmount());

            return currentPoint.get();

        } catch (PointException e) {
            log.warn("포인트 사용 실패 - userId={}, amount={}", userId, amount, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 사용 중 시스템 오류 발생 - userId={}, amount={}", userId, amount, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
