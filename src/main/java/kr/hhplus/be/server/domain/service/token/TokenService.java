package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.infra.repository.token.TokenRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRedisRepository tokenRedisRepository;

    private static final int MAX_ACTIVE_TOKENS = 100;

    @Transactional(readOnly = true)
    public boolean isValidToken(Long userId) {
        try {
            String value = String.valueOf(userId);
            Optional<Double> token = tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value);
            return token.isPresent();
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Token getToken(Long userId, TokenType tokenType) {
        try {
            Optional<Double> existToken = tokenRedisRepository.getToken(tokenType.getKeyPrefix(), String.valueOf(userId));
            if(existToken.isPresent()) {
                throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
            }

            return new Token(TokenStatus.ACTIVE, existToken.get());
        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public Token issueWaitToken(Long userId) {
        try {
            String value = String.valueOf(userId);

            Optional<Double> waitingToken = tokenRedisRepository.getToken(TokenType.WAITING.getKeyPrefix(), value);
            if (waitingToken.isPresent()) {
                return new Token(TokenStatus.WAIT, waitingToken.get());
            }

            Optional<Double> activeToken = tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value);
            if (activeToken.isPresent()) {
                return new Token(TokenStatus.ACTIVE, activeToken.get());
            }

            double score = System.currentTimeMillis();
            tokenRedisRepository.saveToken(TokenType.WAITING.getKeyPrefix(), String.valueOf(userId), score);
            return new Token(TokenStatus.WAIT, score);

        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void activateTokens() {
        try {
            String waitingQueueKey = TokenType.WAITING.getKeyPrefix();
            String activeQueueKey = TokenType.ACTIVE.getKeyPrefix();

            // 1. 현재 활성 토큰 수 조회
            Long activeCount = tokenRedisRepository.getTokenCount(activeQueueKey);
            if (activeCount == null) {
                activeCount = 0L;
            }

            int availableSlots = MAX_ACTIVE_TOKENS - activeCount.intValue();
            if (availableSlots <= 0) {
                return;
            }

            Set<ZSetOperations.TypedTuple<String>> waitingTokens =
                    tokenRedisRepository.getTokensInRangeForWaiting(waitingQueueKey, 0, availableSlots - 1);
            if (waitingTokens == null || waitingTokens.isEmpty()) return;

            long currentTimeMillis = System.currentTimeMillis();
            double activeExpirationTime = currentTimeMillis + (TokenType.ACTIVE.getTtlSeconds() * 1000L);
            int activatedCount = 0;

            for (ZSetOperations.TypedTuple<String> tokenTuple : waitingTokens) {
                String value = tokenTuple.getValue();
                tokenRedisRepository.removeToken(waitingQueueKey, value);
                tokenRedisRepository.saveToken(activeQueueKey, value, activeExpirationTime);
                activatedCount++;
            }

        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void expireTokens() {
        try {
            long currentTimeMillis = System.currentTimeMillis();

            Long removedWaiting = tokenRedisRepository.removeExpiredTokens(TokenType.WAITING.getKeyPrefix(), currentTimeMillis);
            if (removedWaiting == null) removedWaiting = 0L;

            Long removedActive = tokenRedisRepository.removeExpiredTokens(TokenType.ACTIVE.getKeyPrefix(), currentTimeMillis);
            if (removedActive == null) removedWaiting = 0L;

            long totalRemoved = removedWaiting + removedActive;
            if (totalRemoved == 0) return;
        } catch (Exception e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
