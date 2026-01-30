package ai.zevaro.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic decisionCreatedTopic() {
        return TopicBuilder.name("zevaro.decisions.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic decisionResolvedTopic() {
        return TopicBuilder.name("zevaro.decisions.resolved").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic decisionEscalatedTopic() {
        return TopicBuilder.name("zevaro.decisions.escalated").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic outcomeCreatedTopic() {
        return TopicBuilder.name("zevaro.outcomes.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic outcomeValidatedTopic() {
        return TopicBuilder.name("zevaro.outcomes.validated").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic outcomeInvalidatedTopic() {
        return TopicBuilder.name("zevaro.outcomes.invalidated").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic hypothesisCreatedTopic() {
        return TopicBuilder.name("zevaro.hypotheses.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic hypothesisStatusChangedTopic() {
        return TopicBuilder.name("zevaro.hypotheses.status-changed").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic hypothesisConcludedTopic() {
        return TopicBuilder.name("zevaro.hypotheses.concluded").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name("zevaro.audit.events").partitions(3).replicas(1).build();
    }
}
