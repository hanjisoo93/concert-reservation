package kr.hhplus.be.server.interfaces.scheduler.token;

import kr.hhplus.be.server.domain.service.queue.QueueService;
import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenScheduler {

    private final QueueService queueService;
    private final TokenService tokenService;

    @Scheduled(fixedRate = 5000)
    public void processActivateTokens() {
        queueService.activateTokens();
    }
    @Scheduled(fixedRate = 5000)
    public void processActivateTokensWithRedis() {
        tokenService.activateTokensWithRedis();
    }

    @Scheduled(fixedRate = 5000)
    public void processExpireTokens() {
        queueService.expireTokens();
    }

    @Scheduled(fixedRate = 5000)
    public void processExpireTokensWithRedis() {
        tokenService.expireTokensWithRedis();
    }
}
