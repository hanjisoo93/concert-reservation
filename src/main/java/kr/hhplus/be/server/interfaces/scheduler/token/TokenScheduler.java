package kr.hhplus.be.server.interfaces.scheduler.token;

import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TokenScheduler {

    private final TokenRepository tokenRepository;

    @Scheduled(fixedRate = 5000)    // 5초마다 실행
    @Transactional
    public void processTokens() {
        // 1. 만료 토큰 처리
        expireTokens();

        // 2. 토큰 발급 처리
        activateTokens();
    }

    public void activateTokens() {
        // 1. 현재 활성화 토큰 수 확인
        int activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);

        if(activeCount >= 100) {
            return; // 이미 100명이 활성 상태라면 종료
        }

        // 2. WAIT 상태의 토큰 중 오래된 순서대로 ACTIVE로 변경
        List<Token> waitingTokens = tokenRepository.findTop100ByStatusOrderByCreatedAtAsc(TokenStatus.WAIT);

        for(Token token : waitingTokens) {
            token.updateStatus(TokenStatus.ACTIVE);
            tokenRepository.save(token);

            // ACTIVE 상태가 100명을 초과하면 중단
            activeCount++;
            if (activeCount >= 100) {
                break;
            }
        }
    }

    public void expireTokens() {
        // 1. 활성화 토큰 만료 처리
        List<Token> expiredTokens = tokenRepository.findAllByExpiredAtBeforeAndStatus(LocalDateTime.now(), TokenStatus.ACTIVE);

        for (Token token : expiredTokens) {
            token.expireToken();
            tokenRepository.save(token);
        }

        // 2. WAIT 상태 토큰 만료 처리
        List<Token> expiredWaitingTokens = tokenRepository.findAllByExpiredAtBeforeAndStatus(LocalDateTime.now(), TokenStatus.WAIT);

        for (Token token : expiredWaitingTokens) {
            token.expireToken();
            tokenRepository.save(token);
        }
    }

}
