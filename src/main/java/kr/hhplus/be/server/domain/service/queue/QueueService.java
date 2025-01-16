package kr.hhplus.be.server.domain.service.queue;

import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class QueueService {

    private final TokenRepository tokenRepository;

    @Transactional
    public void activateTokens() {
        // 1. 현재 활성화된 토큰 수 확인
        int activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);

        if (activeCount >= 100) {
            return; // 이미 활성화된 토큰이 100개 이상이면 종료
        }

        // 2. 활성화가 필요한 토큰 수 계산
        int remainingCount = 100 - activeCount;

        // 3. WAIT 상태의 토큰 조회 (최대 remainingCount만큼)
        Pageable pageable = PageRequest.of(0, remainingCount);
        List<Token> waitingTokens = tokenRepository.findTopByTokens(TokenStatus.WAIT, pageable);

        // 4. 조회된 토큰 활성화 및 저장
        waitingTokens.forEach(token -> token.updateStatus(TokenStatus.ACTIVE));
        tokenRepository.saveAll(waitingTokens);
    }

    @Transactional
    public void expireTokens() {
        // 1. 만료 대상 토큰 조회
        List<Token> expiredTokens = tokenRepository.findAllByExpiredAtBeforeAndStatuses(
                LocalDateTime.now(),
                List.of(TokenStatus.ACTIVE, TokenStatus.WAIT)
        );

        // 2. 만료 처리
        expiredTokens.forEach(Token::expireToken);
        tokenRepository.saveAll(expiredTokens);
    }
}
