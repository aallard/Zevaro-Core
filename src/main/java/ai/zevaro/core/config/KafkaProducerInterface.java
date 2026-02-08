package ai.zevaro.core.config;

public interface KafkaProducerInterface {

    void send(String topic, String key, Object event);

    long getDroppedEventCount();

    boolean isCircuitBreakerOpen();
}
