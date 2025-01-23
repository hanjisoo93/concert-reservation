package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.domain.entity.point.PointChangeType;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointConcurrencyService {

    private final PointRepository pointRepository;
    private final PointHistoryService pointHistoryService;

    /**
     * 비관적 락 기반 : 포인트 충전 처리
     */
    @Transactional
    public void creditPoint(Long userId, int amount){
        // 1. 포인트 충전 처리
        Point currentPoint = addPoint(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.DEPOSIT);
    }

    /**
     * 비관적 락 기반 : 포인트 사용 처리
     */
    @Transactional
    public void spendPoint(Long userId, int amount){
        // 1. 포인트 사용 처리
        Point currentPoint = usePoint(userId, amount);

        // 2. 포인트 히스토리 저장
        pointHistoryService.createPointHistory(userId, amount, currentPoint.getAmount(), PointChangeType.WITHDRAWAL);
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
     * 비관적 락 기반 : 포인트 충전
     */
    @Transactional
    public Point addPoint(Long userId, int amount) {
        // 1. 포인트 계정 조회
        Optional<Point> optionalPoint = pointRepository.findPointByUserIdForUpdate(userId);
        Point currentPoint;

        if (optionalPoint.isPresent()) {
            currentPoint = optionalPoint.get();
            currentPoint.addPoint(amount);
        } else {
            currentPoint = pointRepository.save(Point.builder()
                    .userId(userId)
                    .amount(amount)  // 초기 포인트 설정
                    .build());
            log.info("새로운 포인트 계정 생성 - userId={}", userId);
        }

        log.info("포인트 츙전 - userId={}, addedAmount={}, totalAmount={}", userId, amount, currentPoint.getAmount());

        return currentPoint;
    }

    /**
     * 비관적 락 기반 : 포인트 사용
     */
    @Transactional
    public Point usePoint(Long userId, int amount) {
        // 1. 포인트 조회 (없으면 새로 생성)
        Point currentPoint = pointRepository.findPointByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    log.warn("포인트 없음 - userId={}, 초기 포인트 0 생성", userId);
                    return Point.builder()
                            .userId(userId)
                            .amount(0)
                            .build();
                });

        // 2. 잔액 검증
        if (currentPoint.isAmountLessThan(amount)) {
            log.warn("포인트 부족 - userId={}, 요청 차감 금액={}, 현재 잔액={}", userId, amount, currentPoint.getAmount());
            return currentPoint;  // 예외 발생 X, 실패 처리
        }

        // 3. 포인트 차감
        currentPoint.usePoint(amount);
        log.info("포인트 사용 완료 - userId={}, 차감 금액={}, 남은 잔액={}", userId, amount, currentPoint.getAmount());

        return currentPoint;
    }

    /**
     * 분산 락 기반 : 포인트 충전
     */
    private Point addPointWithRedissonLock(Long userId, int amount) {
        // 1. 포인트 계정 조회
        Optional<Point> optionalPoint = pointRepository.findPointByUserId(userId);
        Point currentPoint;

        if (optionalPoint.isPresent()) {
            currentPoint = optionalPoint.get();
            currentPoint.addPoint(amount);
        } else {
            currentPoint = pointRepository.save(Point.builder()
                    .userId(userId)
                    .amount(amount)  // 초기 포인트 설정
                    .build());
            log.info("새로운 포인트 계정 생성 - userId={}", userId);
        }

        log.info("포인트 츙전 - userId={}, addedAmount={}, totalAmount={}", userId, amount, currentPoint.getAmount());

        return currentPoint;
    }

    /**
     * 분산 락 기반 : 포인트 사용
     */
    private Point usePointWithRedissonLock(Long userId, int amount) {
        // 1. 포인트 조회
        Point currentPoint = pointRepository.findPointByUserId(userId)
                .orElseGet(() -> {
                    log.warn("포인트 없음 - userId={}, 초기 포인트 0 생성", userId);
                    return Point.builder()
                            .userId(userId)
                            .amount(0)
                            .build();
                });

        // 2. 잔액 검증
        if(currentPoint.isAmountLessThan(amount)) {
            log.warn("포인트 부족 - userId={}, 요청 차감 금액={}, 현재 잔액={}", userId, amount, currentPoint.getAmount());
            return currentPoint;  // 예외 발생 X, 실패 처리
        }

        // 3. 포인트 차감
        currentPoint.usePoint(amount);
        log.info("포인트 사용 완료 - userId={}, deductedAmount={}, remainingAmount={}", userId, amount, currentPoint.getAmount());

        return currentPoint;
    }
}
