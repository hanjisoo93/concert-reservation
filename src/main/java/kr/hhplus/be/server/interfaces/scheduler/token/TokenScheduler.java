package kr.hhplus.be.server.interfaces.scheduler.token;

import kr.hhplus.be.server.domain.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenScheduler {

    private final TokenService tokenService;

    @Scheduled(fixedRate = 5000)
    public void processActivateTokensWithRedis() {
        tokenService.activateTokens();
    }

    @Scheduled(fixedRate = 5000)
    public void processExpireTokensWithRedis() {
        tokenService.expireTokens();
    }
}
