package kr.hhplus.be.server.domain.service.token;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.SystemException;
import kr.hhplus.be.server.domain.entity.token.TokenType;
import kr.hhplus.be.server.domain.exception.token.TokenException;
import kr.hhplus.be.server.infra.repository.token.TokenRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value) != null;
        } catch (Exception e) {
            log.error("토큰 유효성 검사 중 시스템 오류 발생 - tokenUserId={}", userId, e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

//    @Transactional(readOnly = true)
//    public boolean isValidTokenWithRedis(Long userId) {
//        try {
//            String value = String.valueOf(userId);
//
//            boolean waitingTokens = tokenRedisRepository.getToken(TokenType.WAITING.getKeyPrefix(), value) != null;
//            boolean activeTokens = tokenRedisRepository.getToken(TokenType.ACTIVE.getKeyPrefix(), value) != null;
//
//            return waitingTokens || activeTokens;
//
//        } catch (Exception e) {
//            log.error("토큰 유효성 검사 중 시스템 오류 발생 - tokenUserId={}", userId, e);
//            throw new SystemException(ErrorCode.SYSTEM_ERROR);
//        }
//    }

    @Transactional(readOnly = true)
    public String getToken(Long userId, TokenType tokenType) {
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
    public String issueWaitToken(Long userId) {
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
                log.info("활성화 가능한 슬롯이 없음");
                return;
            }

            // 2. 대기열 토큰 조회 : 순위가 낮은(먼저 등록된) 것부터 availableSlots 개수 만큼
            Set<ZSetOperations.TypedTuple<String>> waitingTokens =
                    tokenRedisRepository.getTokensInRangeForWaiting(waitingQueueKey, 0, availableSlots - 1);

            if (waitingTokens == null || waitingTokens.isEmpty()) {
                log.info("대기 중인 토큰이 없음");
                return;
            }

            long currentTimeMillis = System.currentTimeMillis();
            double activeExpirationTime = currentTimeMillis + (TokenType.ACTIVE.getTtlSeconds() * 1000L);
            int activatedCount = 0;

            // 3. 대기열 토큰 활성 토큰으로 전환
            for (ZSetOperations.TypedTuple<String> tokenTuple : waitingTokens) {
                String value = tokenTuple.getValue();
                // 3-1. 대기열 토큰 제거
                tokenRedisRepository.removeToken(waitingQueueKey, value);
                // 3-2. 활성 토큰 생성
                tokenRedisRepository.saveToken(activeQueueKey, value, activeExpirationTime);
                activatedCount++;
            }
            log.info("토큰 활성화 완료 - 활성화 된 토큰 개수: {}", activatedCount);

        } catch (Exception e) {
            log.error("토큰 활성화 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void expireTokens() {
        try {
            long currentTimeMillis = System.currentTimeMillis();

            // 1. 대기열 토큰 만료 처리
            Long removedWaiting = tokenRedisRepository.removeExpiredTokens(TokenType.WAITING.getKeyPrefix(), currentTimeMillis);
            if (removedWaiting == null) {
                removedWaiting = 0L;
            }

            // 2. 활성 토큰 만료 처리
            Long removedActive = tokenRedisRepository.removeExpiredTokens(TokenType.ACTIVE.getKeyPrefix(), currentTimeMillis);
            if (removedActive == null) {
                removedActive = 0L;
            }

            long totalRemoved = removedWaiting + removedActive;
            if (totalRemoved == 0) {
                log.info("만료 대상 토큰 없음");
                return;
            }

            log.info("토큰 만료 처리 완료 - 만료된 토큰 개수: {}", totalRemoved);
        } catch (Exception e) {
            log.error("토큰 만료 처리 중 시스템 오류 발생", e);
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
