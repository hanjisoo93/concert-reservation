package kr.hhplus.be.server.infra.repository.point;

import kr.hhplus.be.server.domain.entity.point.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findAllByUserId(Long userId);
}
