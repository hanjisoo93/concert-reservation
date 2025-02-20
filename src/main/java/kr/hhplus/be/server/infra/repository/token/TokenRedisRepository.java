package kr.hhplus.be.server.infra.repository.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void saveToken(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public Optional<Double> getToken(String key, String value) {
        Double score = redisTemplate.opsForZSet().score(key, value);
        return Optional.ofNullable(score);
    }

    public Long getTokenCount(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTokensInRangeForWaiting(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public void removeToken(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    public Long removeExpiredTokens(String key, double currentTimeMillis) {
        return redisTemplate.opsForZSet().removeRangeByScore(key,0, currentTimeMillis);
    }
}
