package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

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
            Point currentPoint = pointRepository.findPointByUserId(userId)
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
    public Point processPoint(Long userId, int price) {
        try {
            // 1. 포인트 조회
            Point point = pointRepository.findPointByUserId(userId)
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));

            // 2. 가격 검증
            if (point.isAmountLessThan(price)) {
                log.warn("포인트 잔액 부족 - userId={}, currentAmount={}, requiredAmount={}",
                        userId, point.getAmount(), price);
                throw new PointException(ErrorCode.INSUFFICIENT_POINT);
            }

            // 3. 포인트 차감
            point.usePoint(price);
            pointRepository.save(point);

            log.info("포인트 차감 완료 - userId={}, deductedAmount={}, remainingAmount={}",
                    userId, price, point.getAmount());

            return point;
        } catch (PointException e) {
            log.warn("포인트 처리 실패 - userId={}, price={}", userId, price, e);
            throw e;
        } catch (Exception e) {
            log.error("포인트 처리 중 시스템 오류 발생 - userId={}, price={}", userId, price, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
