package kr.hhplus.be.server.domain.service.point;

import kr.hhplus.be.server.interfaces.controller.point.dto.PointRequest;
import kr.hhplus.be.server.interfaces.controller.point.dto.PointResponse;
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
    public PointResponse getPoint(Long userId) {
        Point point = pointRepository.findAllByUserId(userId);
        return PointResponse.of(point);
    }

    @Transactional
    public void addPoint(PointRequest pointRequest) {
        Point currentPoint = pointRepository.findAllByUserId(pointRequest.getUserId());
        currentPoint.addPoint(pointRequest.getAmount());
    }

    @Transactional
    public void usePoint(PointRequest pointRequest) {
        Point currentPoint = pointRepository.findAllByUserId(pointRequest.getUserId());
        currentPoint.userPoint(pointRequest.getAmount());
    }
}
