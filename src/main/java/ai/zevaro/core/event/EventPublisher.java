package ai.zevaro.core.event;

import ai.zevaro.core.config.KafkaProducerInterface;
import ai.zevaro.core.config.KafkaTopics;
import ai.zevaro.core.domain.comment.Comment;
import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import ai.zevaro.core.domain.document.Document;
import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomePriority;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.requirement.Requirement;
import ai.zevaro.core.domain.specification.Specification;
import ai.zevaro.core.domain.ticket.Ticket;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.event.comment.CommentCreatedEvent;
import ai.zevaro.core.event.decision.DecisionCreatedEvent;
import ai.zevaro.core.event.decision.DecisionEscalatedEvent;
import ai.zevaro.core.event.decision.DecisionResolvedEvent;
import ai.zevaro.core.event.document.DocumentPublishedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisConcludedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisCreatedEvent;
import ai.zevaro.core.event.hypothesis.HypothesisStatusChangedEvent;
import ai.zevaro.core.event.outcome.OutcomeCreatedEvent;
import ai.zevaro.core.event.outcome.OutcomeInvalidatedEvent;
import ai.zevaro.core.event.outcome.OutcomeValidatedEvent;
import ai.zevaro.core.event.program.ProgramCreatedEvent;
import ai.zevaro.core.event.program.ProgramStatusChangedEvent;
import ai.zevaro.core.event.requirement.RequirementStatusChangedEvent;
import ai.zevaro.core.event.specification.SpecificationApprovedEvent;
import ai.zevaro.core.event.specification.SpecificationCreatedEvent;
import ai.zevaro.core.event.specification.SpecificationStatusChangedEvent;
import ai.zevaro.core.event.ticket.TicketAssignedEvent;
import ai.zevaro.core.event.ticket.TicketCreatedEvent;
import ai.zevaro.core.event.ticket.TicketResolvedEvent;
import ai.zevaro.core.event.workstream.WorkstreamCreatedEvent;
import ai.zevaro.core.event.workstream.WorkstreamStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event publisher service using KafkaProducerService with circuit breaker protection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaProducerInterface kafkaProducerService;

    private static final Map<String, String> TOPIC_MAP = Map.ofEntries(
            Map.entry("decision.created", KafkaTopics.DECISION_CREATED),
            Map.entry("decision.resolved", KafkaTopics.DECISION_RESOLVED),
            Map.entry("decision.escalated", KafkaTopics.DECISION_ESCALATED),
            Map.entry("outcome.created", KafkaTopics.OUTCOME_CREATED),
            Map.entry("outcome.validated", KafkaTopics.OUTCOME_VALIDATED),
            Map.entry("outcome.invalidated", KafkaTopics.OUTCOME_INVALIDATED),
            Map.entry("hypothesis.created", KafkaTopics.HYPOTHESIS_CREATED),
            Map.entry("hypothesis.status-changed", KafkaTopics.HYPOTHESIS_STATUS_CHANGED),
            Map.entry("hypothesis.concluded", KafkaTopics.HYPOTHESIS_CONCLUDED),
            Map.entry("program.created", KafkaTopics.PROGRAM_CREATED),
            Map.entry("program.status-changed", KafkaTopics.PROGRAM_STATUS_CHANGED),
            Map.entry("workstream.created", KafkaTopics.WORKSTREAM_CREATED),
            Map.entry("workstream.status-changed", KafkaTopics.WORKSTREAM_STATUS_CHANGED),
            Map.entry("specification.created", KafkaTopics.SPECIFICATION_CREATED),
            Map.entry("specification.status-changed", KafkaTopics.SPECIFICATION_STATUS_CHANGED),
            Map.entry("specification.approved", KafkaTopics.SPECIFICATION_APPROVED),
            Map.entry("requirement.status-changed", KafkaTopics.REQUIREMENT_STATUS_CHANGED),
            Map.entry("ticket.created", KafkaTopics.TICKET_CREATED),
            Map.entry("ticket.resolved", KafkaTopics.TICKET_RESOLVED),
            Map.entry("ticket.assigned", KafkaTopics.TICKET_ASSIGNED),
            Map.entry("document.published", KafkaTopics.DOCUMENT_PUBLISHED),
            Map.entry("comment.created", KafkaTopics.COMMENT_CREATED)
    );

    public void publish(DomainEvent event) {
        String topic = TOPIC_MAP.getOrDefault(event.getEventType(), "zevaro.events.unknown");
        String key = event.getTenantId().toString();

        log.debug("Publishing event {} to topic {}", event.getEventType(), topic);
        kafkaProducerService.send(topic, key, event);
    }

    // --- Decision events ---

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

    // --- Outcome events ---

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
                outcome.getInvalidatedBy().getId(),
                outcome.getValidationNotes()
        ));
    }

    // --- Hypothesis events ---

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

    // --- Program events ---

    public void publishProgramCreated(Program program, UUID actorId) {
        publish(new ProgramCreatedEvent(
                program.getTenantId(), actorId, program.getId(),
                program.getName(), program.getStatus().name()));
    }

    public void publishProgramStatusChanged(Program program, String oldStatus, UUID actorId) {
        publish(new ProgramStatusChangedEvent(
                program.getTenantId(), actorId, program.getId(),
                oldStatus, program.getStatus().name()));
    }

    // --- Workstream events ---

    public void publishWorkstreamCreated(Workstream workstream, UUID actorId) {
        publish(new WorkstreamCreatedEvent(
                workstream.getTenantId(), actorId, workstream.getId(),
                workstream.getProgramId(), workstream.getName(),
                workstream.getMode().name(), workstream.getExecutionMode().name()));
    }

    public void publishWorkstreamStatusChanged(Workstream workstream, String oldStatus, UUID actorId) {
        publish(new WorkstreamStatusChangedEvent(
                workstream.getTenantId(), actorId, workstream.getId(),
                oldStatus, workstream.getStatus().name()));
    }

    // --- Specification events ---

    public void publishSpecificationCreated(Specification spec, UUID actorId) {
        publish(new SpecificationCreatedEvent(
                spec.getTenantId(), actorId, spec.getId(),
                spec.getWorkstreamId(), spec.getProgramId(), spec.getName()));
    }

    public void publishSpecificationStatusChanged(Specification spec, String oldStatus, UUID actorId) {
        publish(new SpecificationStatusChangedEvent(
                spec.getTenantId(), actorId, spec.getId(),
                oldStatus, spec.getStatus().name()));
    }

    public void publishSpecificationApproved(Specification spec, UUID actorId) {
        publish(new SpecificationApprovedEvent(
                spec.getTenantId(), actorId, spec.getId()));
    }

    // --- Requirement events ---

    public void publishRequirementStatusChanged(Requirement req, String oldStatus, UUID actorId) {
        publish(new RequirementStatusChangedEvent(
                req.getTenantId(), actorId, req.getId(),
                req.getSpecificationId(), oldStatus, req.getStatus().name()));
    }

    // --- Ticket events ---

    public void publishTicketCreated(Ticket ticket, UUID actorId) {
        publish(new TicketCreatedEvent(
                ticket.getTenantId(), actorId, ticket.getId(),
                ticket.getWorkstreamId(), ticket.getType().name(),
                ticket.getSeverity() != null ? ticket.getSeverity().name() : null));
    }

    public void publishTicketResolved(Ticket ticket, UUID actorId) {
        publish(new TicketResolvedEvent(
                ticket.getTenantId(), actorId, ticket.getId(),
                ticket.getResolution() != null ? ticket.getResolution().name() : null));
    }

    public void publishTicketAssigned(Ticket ticket, UUID actorId) {
        publish(new TicketAssignedEvent(
                ticket.getTenantId(), actorId, ticket.getId(),
                ticket.getAssignedToId()));
    }

    // --- Document events ---

    public void publishDocumentPublished(Document document, UUID actorId) {
        publish(new DocumentPublishedEvent(
                document.getTenantId(), actorId, document.getId(),
                document.getSpaceId(), document.getTitle(), document.getVersion()));
    }

    // --- Comment events ---

    public void publishCommentCreated(Comment comment, UUID actorId) {
        publish(new CommentCreatedEvent(
                comment.getTenantId(), actorId, comment.getId(),
                comment.getParentType().name(), comment.getParentId()));
    }
}
