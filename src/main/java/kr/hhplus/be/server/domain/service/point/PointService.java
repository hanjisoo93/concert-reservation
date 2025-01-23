package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.common.lock.DistributedLock;
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
     * 분산 락 기반 : 포인트 충전 처리
     */
    @DistributedLock(key = "#userId")
    public void creditPointWithRedissonLock(Long userId, int amount) {
        // 1. 포인트 충전 처리
        Point currentPoint = addPointWithRedissonLock(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.DEPOSIT);
    }

    /**
     * 분산 락 기반 : 포인트 사용 처리
     */
    @DistributedLock(key = "#userId", waitTime = 10, leaseTime = 15)
    public void spendPointWithRedissonLock(Long userId, int amount) {
        // 1. 포인트 사용 처리
        Point currentPoint = usePointWithRedissonLock(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.WITHDRAWAL);
    }
    /**
     * 분산 락 기반 : 포인트 충전
     */
    private Point addPointWithRedissonLock(Long userId, int amount) {
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
     * 분산 락 기반 : 포인트 사용
     */
    private Point usePointWithRedissonLock(Long userId, int amount) {
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

    @Transactional
    public void addPoint(Long userId, int amount) {
        try {
            Point currentPoint = pointRepository.findPointByUserId(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));
            currentPoint.addPoint(amount);
            log.info("포인트 추가 - userId={}, addedAmount={}, totalAmount={}",
                    userId, amount, currentPoint.getAmount());
        } catch (PointException e) {
            log.warn("포인트 추가 실패 - userId={}, amount={}", userId, amount, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 추가 중 시스템 오류 발생 - userId={}, amount={}", userId, amount, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void usePoint(Long userId, int amount) {
        try {
            Point currentPoint = pointRepository.findPointByUserIdForUpdate(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));
            currentPoint.usePoint(amount);
            log.info("포인트 사용 - userId={}, usedAmount={}, remainingAmount={}",
                    userId, amount, currentPoint.getAmount());
        } catch (PointException e) {
            log.warn("포인트 사용 실패 - userId={}, amount={}", userId, amount, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 사용 중 시스템 오류 발생 - userId={}, amount={}", userId, amount, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void charge(Long userId, int price) {
        try {
            // 1. 포인트 조회
            Point point = pointRepository.findPointByUserIdForUpdate(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));

            // 2. 가격 검증
            if (point.isAmountLessThan(price)) {
                log.warn("포인트 잔액 부족 - userId={}, currentAmount={}, requiredAmount={}",
                        userId, point.getAmount(), price);
                throw new PointException(ErrorCode.INSUFFICIENT_POINT);
            }

            // 3. 포인트 차감
            point.usePoint(price);
            log.info("포인트 차감 완료 - userId={}, deductedAmount={}, remainingAmount={}",
                    userId, price, point.getAmount());

            // 4. 포인트 히스토리 저장
            pointHistoryService.createPointHistory(userId, price, point.getAmount(), PointChangeType.WITHDRAWAL);

        } catch (PointException e) {
            log.warn("포인트 처리 실패 - userId={}, price={}", userId, price, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 처리 중 시스템 오류 발생 - userId={}, price={}", userId, price, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
