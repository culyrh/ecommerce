package ecommerce.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 값 저장 (만료 시간 포함)
     */
    public void setValue(String key, Object value, long timeoutSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
            log.debug("Redis set: key={}, timeout={}s", key, timeoutSeconds);
        } catch (Exception e) {
            log.error("Redis set error: key={}", key, e);
        }
    }

    /**
     * 값 저장 (만료 시간 없음)
     */
    public void setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis set: key={}", key);
        } catch (Exception e) {
            log.error("Redis set error: key={}", key, e);
        }
    }

    /**
     * 값 조회
     */
    public Object getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error: key={}", key, e);
            return null;
        }
    }

    /**
     * String 값 조회
     */
    public String getStringValue(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error: key={}", key, e);
            return null;
        }
    }

    /**
     * 값 증가 (INCR)
     */
    public Long increment(String key) {
        try {
            return stringRedisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis increment error: key={}", key, e);
            return null;
        }
    }

    /**
     * 값 증가 (INCRBY)
     */
    public Long increment(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment error: key={}, delta={}", key, delta, e);
            return null;
        }
    }

    /**
     * 값 감소 (DECR)
     */
    public Long decrement(String key) {
        try {
            return stringRedisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Redis decrement error: key={}", key, e);
            return null;
        }
    }

    /**
     * 키 삭제
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete error: key={}", key, e);
            return false;
        }
    }

    /**
     * 패턴으로 키 검색
     */
    public Set<String> getKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Redis keys error: pattern={}", pattern, e);
            return Set.of();
        }
    }

    /**
     * 키 존재 여부 확인
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey error: key={}", key, e);
            return false;
        }
    }

    /**
     * 만료 시간 설정
     */
    public Boolean expire(String key, long timeoutSeconds) {
        try {
            return redisTemplate.expire(key, Duration.ofSeconds(timeoutSeconds));
        } catch (Exception e) {
            log.error("Redis expire error: key={}, timeout={}s", key, timeoutSeconds, e);
            return false;
        }
    }
}