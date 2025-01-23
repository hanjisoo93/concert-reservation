package kr.hhplus.be.server.infra.repository.point;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.entity.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // 비관적 락 적용
    @Query("SELECT p FROM Point p WHERE p.userId = :userId")
    Optional<Point> findPointByUserIdForUpdate(@Param("userId") Long userId);

    Optional<Point> findPointByUserId(@Param("userId") Long userId);
}
