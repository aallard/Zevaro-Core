package ai.zevaro.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * No-op Kafka producer service when Kafka is disabled.
 *
 * Used when KAFKA_ENABLED=false for local development without Kafka.
 * All events are silently dropped (logged at DEBUG level).
 */
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false")
@Slf4j
public class KafkaProducerServiceNoOp extends KafkaProducerService {

    private final AtomicLong droppedCount = new AtomicLong(0);

    public KafkaProducerServiceNoOp() {
        super(null);
        log.info("Kafka is DISABLED (KAFKA_ENABLED=false). Events will not be published.");
    }

    @Override
    public void send(String topic, String key, Object event) {
        long count = droppedCount.incrementAndGet();
        log.debug("Kafka disabled - dropping event #{} for topic: {}", count, topic);
    }

    @Override
    public long getDroppedEventCount() {
        return droppedCount.get();
    }

    @Override
    public boolean isCircuitBreakerOpen() {
        return true; // Always "open" since Kafka is disabled
    }
}
