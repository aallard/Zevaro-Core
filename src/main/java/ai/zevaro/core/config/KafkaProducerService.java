package ai.zevaro.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Kafka producer service with circuit breaker pattern.
 *
 * Features:
 * - Circuit breaker opens after 5 consecutive failures
 * - Auto-resets after 5 minutes to try again
 * - Rate-limited logging (max 1 per 5 minutes while down)
 * - Tracks dropped events for observability
 */
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class KafkaProducerService implements KafkaProducerInterface {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Circuit breaker state
    private final AtomicReference<Instant> circuitOpenedAt = new AtomicReference<>(null);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong droppedEvents = new AtomicLong(0);
    private final AtomicReference<Instant> lastSummaryLog = new AtomicReference<>(Instant.EPOCH);

    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration CIRCUIT_RESET_TIME = Duration.ofMinutes(5);
    private static final Duration SUMMARY_LOG_INTERVAL = Duration.ofMinutes(5);

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        log.info("KafkaProducerService initialized with circuit breaker pattern");
    }

    /**
     * Send an event to Kafka with circuit breaker protection.
     *
     * @param topic the Kafka topic
     * @param key the message key (usually tenant ID)
     * @param event the event payload
     */
    public void send(String topic, String key, Object event) {
        // Check circuit breaker
        if (isCircuitOpen()) {
            handleDroppedEvent(topic);
            return;
        }

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    handleFailure(topic, ex);
                } else {
                    handleSuccess();
                }
            });

        } catch (Exception e) {
            handleFailure(topic, e);
        }
    }

    private boolean isCircuitOpen() {
        Instant openedAt = circuitOpenedAt.get();
        if (openedAt == null) {
            return false;
        }

        // Check if circuit should reset
        if (Duration.between(openedAt, Instant.now()).compareTo(CIRCUIT_RESET_TIME) > 0) {
            // Reset circuit - try again
            circuitOpenedAt.set(null);
            consecutiveFailures.set(0);
            log.info("Kafka circuit breaker RESET. Attempting to resume publishing.");
            return false;
        }

        return true;
    }

    private void handleSuccess() {
        // Reset failure count on success
        int previousFailures = consecutiveFailures.getAndSet(0);

        // Log recovery if we had failures
        if (circuitOpenedAt.get() != null) {
            long dropped = droppedEvents.getAndSet(0);
            log.info("Kafka connection RESTORED. {} events were dropped while circuit was open.", dropped);
            circuitOpenedAt.set(null);
        } else if (previousFailures > 0) {
            log.debug("Kafka send succeeded after {} previous failures", previousFailures);
        }
    }

    private void handleFailure(String topic, Throwable ex) {
        int failures = consecutiveFailures.incrementAndGet();

        if (failures == 1) {
            // First failure - always log immediately
            log.error("Kafka publish FAILED for topic '{}'. Error: {}", topic, ex.getMessage());
        }

        if (failures >= FAILURE_THRESHOLD && circuitOpenedAt.get() == null) {
            // Open circuit
            circuitOpenedAt.set(Instant.now());
            log.error("Kafka circuit breaker OPEN after {} consecutive failures. " +
                "Events will be dropped for {} minutes. Last error: {}",
                failures, CIRCUIT_RESET_TIME.toMinutes(), ex.getMessage());
        }
    }

    private void handleDroppedEvent(String topic) {
        long dropped = droppedEvents.incrementAndGet();

        // Rate-limited summary logging
        Instant now = Instant.now();
        Instant lastLog = lastSummaryLog.get();

        if (Duration.between(lastLog, now).compareTo(SUMMARY_LOG_INTERVAL) > 0) {
            if (lastSummaryLog.compareAndSet(lastLog, now)) {
                Instant openedAt = circuitOpenedAt.get();
                if (openedAt != null) {
                    Duration downtime = Duration.between(openedAt, now);
                    Duration timeUntilReset = CIRCUIT_RESET_TIME.minus(downtime);
                    if (timeUntilReset.isNegative()) {
                        timeUntilReset = Duration.ZERO;
                    }
                    log.warn("Kafka circuit OPEN. {} events dropped in {}. Next retry in {}.",
                        dropped, formatDuration(downtime), formatDuration(timeUntilReset));
                }
            }
        }
    }

    private String formatDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            return "0s";
        }
        long minutes = duration.toMinutes();
        long seconds = duration.toSecondsPart();
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    /**
     * Get the current number of dropped events (for monitoring).
     */
    public long getDroppedEventCount() {
        return droppedEvents.get();
    }

    /**
     * Check if the circuit breaker is currently open.
     */
    public boolean isCircuitBreakerOpen() {
        return circuitOpenedAt.get() != null;
    }
}
