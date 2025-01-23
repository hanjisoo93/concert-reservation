package kr.hhplus.be.server.domain.service.point.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("포인트 충전/사용 - Redisson AOP 기반 분산 락 동시성 테스트")
public class PointRedissonLockTest {


}
