package kr.hhplus.be.server.infra.repository.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    // 토큰 저장
    public void saveToken(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    // 토큰 조회
    public Double getToken(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // 토큰 수 조회
    public Long getTokenCount(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    // 대기열 토큰 Range 조회
    public Set<ZSetOperations.TypedTuple<String>> getTokensInRangeForWaiting(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    // 토큰 삭제
    public void removeToken(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    // 만료 토큰 삭제
    public Long removeExpiredTokens(String key, double currentTimeMillis) {
        return redisTemplate.opsForZSet().removeRangeByScore(key,0, currentTimeMillis);
    }
}
