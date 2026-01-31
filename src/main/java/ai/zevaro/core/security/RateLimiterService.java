package ai.zevaro.core.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int GENERAL_AUTH_REQUESTS_PER_MINUTE = 10;
    private static final int LOGIN_REQUESTS_PER_MINUTE = 5;

    public boolean tryConsumeGeneral(String key) {
        return getBucket(key, GENERAL_AUTH_REQUESTS_PER_MINUTE).tryConsume(1);
    }

    public boolean tryConsumeLogin(String key) {
        return getBucket("login:" + key, LOGIN_REQUESTS_PER_MINUTE).tryConsume(1);
    }

    private Bucket getBucket(String key, int requestsPerMinute) {
        return buckets.computeIfAbsent(key, k -> createBucket(requestsPerMinute));
    }

    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
