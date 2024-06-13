package latipe.auth.services;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "tokens", key = "#token", unless = "#result == null")
    public String getUserDetails(String token) {
        return null; // This will be overridden by the cached value if it exists
    }

    @CachePut(value = "tokens", key = "#token")
    public String cacheToken(String token, String userDetails) {
        return userDetails;
    }

    @CacheEvict(value = "tokens", key = "#token")
    public void evictToken(String token) {

    }

    public void evictTokenByUserId(String userId) {
        Set<String> keys = redisTemplate.keys("*+%s".formatted(userId));
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    public void saveSid(String sessionKey, String sessionData) {
        redisTemplate.opsForValue().set(sessionKey, sessionData, Duration.ofMinutes(5));
    }

    public String getSid(String sessionKey) {
        return (String) redisTemplate.opsForValue().get(sessionKey);
    }
}
