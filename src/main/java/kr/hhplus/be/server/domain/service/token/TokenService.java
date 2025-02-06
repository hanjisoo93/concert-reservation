package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.token.Token;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.domain.entity.token.TokenStatus;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.infra.repository.token.TokenRedisRepository;
import kr.hhplus.be.server.infra.repository.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final TokenRedisRepository tokenRedisRepository;

    private static final int MAX_ACTIVE_TOKENS = 100;

    @Transactional(readOnly = true)
    public boolean isValidTokenByUuid(String tokenUuid) {
        try {
            return tokenRepository.findByUuid(tokenUuid)
                    .filter(token -> !token.isExpired()) // 만료되지 않은 토큰
                    .filter(token -> token.getStatus() == TokenStatus.WAIT || token.getStatus() == TokenStatus.ACTIVE) // 유효 상태
                    .isPresent();
        } catch (Exception e) {
            log.error("토큰 유효성 검사 중 시스템 오류 발생 - tokenUuid={}", tokenUuid, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public boolean isValidTokenWithRedis(Long userId) {
        try {
            String value = String.valueOf(userId);

            boolean waitingTokens = tokenRedisRepository.getToken(TokenType.WAITING.getKeyPrefix(), value) != null;
            boolean activeTokens = tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value) != null;

            return waitingTokens || activeTokens;

        } catch (Exception e) {
            log.error("토큰 유효성 검사 중 시스템 오류 발생 - tokenUserId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Token getToken(Long userId, TokenStatus tokenStatus) {
        try {
            return tokenRepository.findAllByUserIdAndStatus(userId, tokenStatus);
        } catch (Exception e) {
            log.error("토큰 조회 중 시스템 오류 발생 - userId={}, status={}", userId, tokenStatus, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public String getTokenWithRedis(Long userId, TokenType tokenType) {
        try {
            boolean existToken = tokenRedisRepository.getToken(tokenType.getKeyPrefix(), String.valueOf(userId)) != null;
            if(!existToken) {
                log.warn("유효한 토큰이 존재하지 않습니다 - userId={}, type={}", userId, tokenType);
                throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
            }

            return "EXISTING_TOKEN";
        } catch (TokenException e) {
            log.warn("유효한 토튼 조회 실패 - userId={}, type={}", userId, tokenType);
            throw e;
        } catch (Exception e) {
            log.error("토큰 조회 중 시스템 오류 발생 - userId={}, type={}", userId, tokenType, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public Token issueWaitToken(Long userId) {
        try {
            log.info("토큰 발급 시도 - userId={}", userId);

            // 1. 기존 토큰 조회
            Optional<Token> existingToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                    userId,
                    Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                    LocalDateTime.now()
            );

            if (existingToken.isPresent()) {
                Token token = existingToken.get();
                if (TokenStatus.ACTIVE.equals(token.getStatus()) ||
                        (TokenStatus.WAIT.equals(token.getStatus()) && !token.isExpired())) {
                    log.info("기존 유효한 토큰 반환 - tokenUuid={}, userId={}", token.getUuid(), userId);
                    return token;
                }
            }

            // 2. 새로운 토큰 발급
            Token newToken = createTokenWithLock(userId);
            log.info("새로운 토큰 발급 완료 - tokenUuid={}, userId={}", newToken.getUuid(), userId);
            return newToken;

        } catch (Exception e) {
            log.error("토큰 발급 중 시스템 오류 발생 - userId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public String issueWaitTokenWithRedis(Long userId) {
        try {
            log.info("토큰 발급 시도 - userId={}", userId);

            // 1. 기존 토큰 존재 여부 확인
            String value = String.valueOf(userId);
            boolean existsWaiting = tokenRedisRepository.getToken(TokenType.WAITING.getKeyPrefix(), value) != null;
            boolean existsActive = tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value) != null;

            if(existsWaiting || existsActive) {
                log.info("기존 유효한 토큰 존재 - userId={}", userId);
                return "EXISTING_TOKEN";
            }

            // 2. 신규 대기 토큰 발급
            double score = System.currentTimeMillis();
            tokenRedisRepository.saveToken(TokenType.WAITING.getKeyPrefix(), String.valueOf(userId), score);

            log.info("새로운 대기 토큰 발급 완료 - userId={}", userId);
            return "NEW_WAIT_TOKEN";

        } catch (Exception e) {
            log.error("토큰 발급 중 시스템 오류 발생 - userId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void activateTokensWithRedis() {
        try {
            String waitingQueueKey = TokenType.WAITING.getKeyPrefix();
            String activeQueueKey = TokenType.ACTIVE.getKeyPrefix();

            // 현재 활성 토큰 수 조회
            Long activeCount = tokenRedisRepository.getTokenCount(activeQueueKey);
            if (activeCount == null) {
                activeCount = 0L;
            }

            int availableSlots = MAX_ACTIVE_TOKENS - activeCount.intValue();
            if (availableSlots <= 0) {
                log.info("활성화 가능한 슬롯이 없음");
                return;
            }

            // 대기열 토큰 중 순위가 낮은(먼저 등록된) 것부터 availableSlots 개수만큼 조회
            Set<ZSetOperations.TypedTuple<String>> waitingTokens =
                    tokenRedisRepository.getTokensInRangeForWaiting(waitingQueueKey, 0, availableSlots - 1);

            if (waitingTokens == null || waitingTokens.isEmpty()) {
                log.info("대기 중인 토큰이 없음");
                return;
            }

            long currentTimeMillis = System.currentTimeMillis();
            double activeExpirationTime = currentTimeMillis + (TokenType.ACTIVE.getTtlSeconds() * 1000L);
            int activatedCount = 0;

            // 대기열 토큰 순서대로 활성 토큰으로 전환
            for (ZSetOperations.TypedTuple<String> tokenTuple : waitingTokens) {
                String value = tokenTuple.getValue();
                // 대기열에서 해당 토큰 제거
                tokenRedisRepository.removeToken(waitingQueueKey, value);
                // 활성 토큰 큐에 추가
                tokenRedisRepository.saveToken(activeQueueKey, value, activeExpirationTime);
                activatedCount++;
            }
            log.info("토큰 활성화 완료 - 활성화된 토큰 개수: {}", activatedCount);

        } catch (Exception e) {
            log.error("토큰 활성화 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void expireTokensWithRedis() {
        try {
            long currentTimeMillis = System.currentTimeMillis();

            // 대기열 토큰 만료 처리
            Long removedWaiting = tokenRedisRepository.removeExpiredTokens(TokenType.WAITING.getKeyPrefix(), currentTimeMillis);
            if (removedWaiting == null) {
                removedWaiting = 0L;
            }

            // 활성 토큰 만료 처리
            Long removedActive = tokenRedisRepository.removeExpiredTokens(TokenType.ACTIVE.getKeyPrefix(), currentTimeMillis);
            if (removedActive == null) {
                removedActive = 0L;
            }

            long totalRemoved = removedWaiting + removedActive;
            if (totalRemoved == 0) {
                log.info("만료 대상 토큰 없음 (Redis)");
                return;
            }

            log.info("토큰 만료 처리 완료 (Redis) - 만료된 토큰 개수: {}", totalRemoved);
        } catch (Exception e) {
            log.error("토큰 만료 처리 중 시스템 오류 발생 (Redis)", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public Token createTokenWithLock(Long userId) {
        synchronized (userId.toString().intern()) {
            try {
                log.info("토큰 생성 시도 - userId={}", userId);

                // 1. 중복 생성 방지용 다시 조회
                Optional<Token> latestToken = tokenRepository.findFirstByUserIdAndStatusAndNotExpired(
                        userId,
                        Arrays.asList(TokenStatus.WAIT, TokenStatus.ACTIVE),
                        LocalDateTime.now()
                );

                if (latestToken.isPresent()) {
                    log.info("토큰 생성 불필요 - 기존 유효 토큰 존재 - tokenUuid={}, userId={}", latestToken.get().getUuid(), userId);
                    return latestToken.get();
                }

                // 2. 새로운 토큰 생성
                Token newToken = Token.createToken(userId);
                tokenRepository.save(newToken);
                log.info("새로운 토큰 생성 완료 - tokenUuid={}, userId={}", newToken.getUuid(), userId);
                return newToken;
            } catch (Exception e) {
                log.error("토큰 생성 중 시스템 오류 발생 - userId={}", userId, e);
                throw new SystemException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    @Transactional
    public void updateTokenExpiredAt(String uuid, int minutes) {
        try {
            tokenRepository.findByUuid(uuid)
                    .ifPresentOrElse(token -> {
                        token.updateExpiredAt(LocalDateTime.now().plusMinutes(minutes));
                        tokenRepository.save(token);
                        log.info("토큰 만료 시간 연장 완료 - tokenUuid={}, newExpiredAt={}", uuid, token.getExpiredAt());
                    }, () -> {
                        log.warn("토큰 만료 시간 연장 실패 - 존재하지 않는 토큰: tokenUuid={}", uuid);
                        throw new TokenException(ErrorCode.TOKEN_NOT_FOUND);
                    });
        } catch (TokenException e) {
            log.warn("토큰 만료 시간 연장 실패 - tokenUuid={}", uuid, e);
            throw e;
        } catch (Exception e) {
            log.error("토큰 만료 시간 연장 중 시스템 오류 발생 - tokenUuid={}", uuid, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
