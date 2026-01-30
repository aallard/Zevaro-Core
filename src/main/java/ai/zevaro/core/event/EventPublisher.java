package ai.zevaro.core.event;

import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomePriority;
import ai.zevaro.core.event.decision.DecisionCreatedEvent;
import ai.zevaro.core.event.decision.DecisionEscalatedEvent;
import ai.zevaro.core.event.decision.DecisionResolvedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisConcludedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisCreatedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisStatusChangedEvent;
import ai.zevaro.core.event.outcome.OutcomeCreatedEvent;
import ai.zevaro.core.event.outcome.OutcomeInvalidatedEvent;
import ai.zevaro.core.event.outcome.OutcomeValidatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Map<String, String> TOPIC_MAP = Map.of(
            "decision.created", "zevaro.decisions.created",
            "decision.resolved", "zevaro.decisions.resolved",
            "decision.escalated", "zevaro.decisions.escalated",
            "outcome.created", "zevaro.outcomes.created",
            "outcome.validated", "zevaro.outcomes.validated",
            "outcome.invalidated", "zevaro.outcomes.invalidated",
            "hypothesis.created", "zevaro.hypotheses.created",
            "hypothesis.status-changed", "zevaro.hypotheses.status-changed",
            "hypothesis.concluded", "zevaro.hypotheses.concluded"
    );

    public void publish(DomainEvent event) {
        String topic = TOPIC_MAP.getOrDefault(event.getEventType(), "zevaro.events.unknown");
        String key = event.getTenantId().toString();

        try {
            kafkaTemplate.send(topic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event {} to {}: {}",
                                    event.getEventType(), topic, ex.getMessage());
                        } else {
                            log.debug("Published event {} to {} partition {}",
                                    event.getEventType(), topic, result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing event: {}", e.getMessage(), e);
        }
    }

    public void publishDecisionCreated(Decision decision, UUID actorId) {
        publish(new DecisionCreatedEvent(
                decision.getTenantId(),
                actorId,
                decision.getId(),
                decision.getTitle(),
                decision.getPriority(),
                decision.getDecisionType(),
                decision.getAssignedTo() != null ? decision.getAssignedTo().getId() : null,
                decision.getOutcome() != null ? decision.getOutcome().getId() : null,
                decision.getHypothesis() != null ? decision.getHypothesis().getId() : null,
                decision.getDueAt()
        ));
    }

    public void publishDecisionResolved(Decision decision, UUID actorId, List<UUID> unblockedHypothesisIds) {
        long cycleTimeHours = Duration.between(decision.getCreatedAt(), decision.getDecidedAt()).toHours();
        publish(new DecisionResolvedEvent(
                decision.getTenantId(),
                actorId,
                decision.getId(),
                decision.getTitle(),
                decision.getPriority(),
                decision.getDecidedBy().getId(),
                decision.getDecisionRationale(),
                cycleTimeHours,
                decision.getEscalationLevel() > 0,
                decision.getEscalationLevel(),
                unblockedHypothesisIds
        ));
    }

    public void publishDecisionEscalated(Decision decision, UUID actorId, UUID escalatedFromId, String reason) {
        publish(new DecisionEscalatedEvent(
                decision.getTenantId(),
                actorId,
                decision.getId(),
                decision.getTitle(),
                decision.getPriority(),
                escalatedFromId,
                decision.getEscalatedTo().getId(),
                decision.getEscalationLevel(),
                reason,
                decision.getWaitTimeHours()
        ));
    }

    public void publishOutcomeCreated(Outcome outcome, UUID actorId) {
        publish(new OutcomeCreatedEvent(
                outcome.getTenantId(),
                actorId,
                outcome.getId(),
                outcome.getTitle(),
                outcome.getPriority(),
                outcome.getTeam() != null ? outcome.getTeam().getId() : null,
                outcome.getOwner() != null ? outcome.getOwner().getId() : null
        ));
    }

    public void publishOutcomeValidated(Outcome outcome, UUID actorId, Map<String, Object> finalMetrics) {
        long daysToValidation = outcome.getStartedAt() != null
                ? Duration.between(outcome.getStartedAt(), outcome.getValidatedAt()).toDays()
                : Duration.between(outcome.getCreatedAt(), outcome.getValidatedAt()).toDays();
        publish(new OutcomeValidatedEvent(
                outcome.getTenantId(),
                actorId,
                outcome.getId(),
                outcome.getTitle(),
                outcome.getValidatedBy().getId(),
                outcome.getValidationNotes(),
                finalMetrics,
                daysToValidation
        ));
    }

    public void publishOutcomeInvalidated(Outcome outcome, UUID actorId) {
        publish(new OutcomeInvalidatedEvent(
                outcome.getTenantId(),
                actorId,
                outcome.getId(),
                outcome.getTitle(),
                outcome.getValidatedBy().getId(),
                outcome.getValidationNotes()
        ));
    }

    public void publishHypothesisCreated(Hypothesis hypothesis, UUID actorId) {
        publish(new HypothesisCreatedEvent(
                hypothesis.getTenantId(),
                actorId,
                hypothesis.getId(),
                hypothesis.getTitle(),
                hypothesis.getOutcome().getId(),
                hypothesis.getOwner() != null ? hypothesis.getOwner().getId() : null
        ));
    }

    public void publishHypothesisStatusChanged(Hypothesis hypothesis, HypothesisStatus previousStatus,
                                                UUID actorId, UUID blockedByDecisionId) {
        publish(new HypothesisStatusChangedEvent(
                hypothesis.getTenantId(),
                actorId,
                hypothesis.getId(),
                hypothesis.getTitle(),
                previousStatus,
                hypothesis.getStatus(),
                hypothesis.getOutcome().getId(),
                blockedByDecisionId
        ));
    }

    public void publishHypothesisConcluded(Hypothesis hypothesis, UUID actorId, Map<String, Object> experimentResults) {
        long daysToConclusion = hypothesis.getStartedAt() != null
                ? Duration.between(hypothesis.getStartedAt(), hypothesis.getConcludedAt()).toDays()
                : Duration.between(hypothesis.getCreatedAt(), hypothesis.getConcludedAt()).toDays();
        publish(new HypothesisConcludedEvent(
                hypothesis.getTenantId(),
                actorId,
                hypothesis.getId(),
                hypothesis.getTitle(),
                hypothesis.getStatus() == HypothesisStatus.VALIDATED,
                hypothesis.getOutcome().getId(),
                experimentResults,
                daysToConclusion
        ));
    }
}
