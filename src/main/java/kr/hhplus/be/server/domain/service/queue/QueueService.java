package kr.hhplus.be.server.domain.service.queue;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueueService {

    private final TokenRepository tokenRepository;

    @Transactional
    public void activateTokens() {
        try {
            int activeCount = tokenRepository.countByStatus(TokenStatus.ACTIVE);
            log.debug("현재 활성 토큰 수: {}", activeCount);

            if (activeCount >= 100) {
                log.info("활성화 가능한 토큰이 없음 (이미 100개 활성화)");
                return;
            }

            int remainingCount = 100 - activeCount;
            Pageable pageable = PageRequest.of(0, remainingCount);
            List<Token> waitingTokens = tokenRepository.findTopByTokens(TokenStatus.WAIT, pageable);

            if (waitingTokens.isEmpty()) {
                log.info("대기 중인 토큰이 없음");
                return;
            }

            waitingTokens.forEach(token -> token.updateStatus(TokenStatus.ACTIVE));
            tokenRepository.saveAll(waitingTokens);

            log.info("토큰 활성화 완료 - 활성화된 토큰 개수: {}", waitingTokens.size());

        } catch (Exception e) {
            log.error("토큰 활성화 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void expireTokens() {

        try {
            List<Token> expiredTokens = tokenRepository.findAllByExpiredAtBeforeAndStatuses(
                    LocalDateTime.now(),
                    List.of(TokenStatus.ACTIVE, TokenStatus.WAIT)
            );

            if (expiredTokens.isEmpty()) {
                log.info("만료 대상 토큰 없음");
                return;
            }

            expiredTokens.forEach(Token::expireToken);
            tokenRepository.saveAll(expiredTokens);

            log.info("토큰 만료 처리 완료 - 만료된 토큰 개수: {}", expiredTokens.size());

        } catch (Exception e) {
            log.error("토큰 만료 처리 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
