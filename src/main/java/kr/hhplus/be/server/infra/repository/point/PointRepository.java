package kr.hhplus.be.server.infra.repository.point;

import kr.hhplus.be.server.domain.entity.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findPointByUserId(Long userId);
}
