package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.domain.exception.point.PointException;
import kr.hhplus.be.server.domain.entity.point.Point;
import kr.hhplus.be.server.infra.repository.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Point getPoint(Long userId) {
        return pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new PointException("포인트 정보가 존재하지 않습니다."));
    }

    @Transactional
    public void addPoint(Long userId, int amount) {
        Point currentPoint = pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new PointException("포인트 정보가 존재하지 않습니다."));
        currentPoint.addPoint(amount);
    }

    @Transactional
    public void usePoint(Long userId, int amount) {
        Point currentPoint = pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new PointException("포인트 정보가 존재하지 않습니다."));
        currentPoint.usePoint(amount);
    }

    @Transactional
    public Point processPoint(Long userId, int price) {
        // 1. 포인트 조회
        Point point = pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new PointException("포인트 정보가 존재하지 않습니다."));

        // 2. 가격 검증
        if(point.isAmountLessThan(price)){
            throw new PointException("포인트 잔액이 부족합니다. 충전 후 다시 시도해주세요.");
        }

        // 3. 포인트 차감
        point.usePoint(price);
        pointRepository.save(point);

        return point;
    }
}
