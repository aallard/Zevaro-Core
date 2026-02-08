package ai.zevaro.core.config;

public final class KafkaTopics {
    private KafkaTopics() {}

    // Decision events
    public static final String DECISION_CREATED = "zevaro.core.decision.created";
    public static final String DECISION_RESOLVED = "zevaro.core.decision.resolved";
    public static final String DECISION_ESCALATED = "zevaro.core.decision.escalated";

    // Outcome events
    public static final String OUTCOME_CREATED = "zevaro.core.outcome.created";
    public static final String OUTCOME_VALIDATED = "zevaro.core.outcome.validated";
    public static final String OUTCOME_INVALIDATED = "zevaro.core.outcome.invalidated";

    // Hypothesis events
    public static final String HYPOTHESIS_CREATED = "zevaro.core.hypothesis.created";
    public static final String HYPOTHESIS_STATUS_CHANGED = "zevaro.core.hypothesis.status-changed";
    public static final String HYPOTHESIS_CONCLUDED = "zevaro.core.hypothesis.concluded";

    // Program events
    public static final String PROGRAM_CREATED = "zevaro.core.program.created";
    public static final String PROGRAM_STATUS_CHANGED = "zevaro.core.program.status-changed";

    // Workstream events
    public static final String WORKSTREAM_CREATED = "zevaro.core.workstream.created";
    public static final String WORKSTREAM_STATUS_CHANGED = "zevaro.core.workstream.status-changed";

    // Specification events
    public static final String SPECIFICATION_CREATED = "zevaro.core.specification.created";
    public static final String SPECIFICATION_STATUS_CHANGED = "zevaro.core.specification.status-changed";
    public static final String SPECIFICATION_APPROVED = "zevaro.core.specification.approved";

    // Requirement events
    public static final String REQUIREMENT_STATUS_CHANGED = "zevaro.core.requirement.status-changed";

    // Ticket events
    public static final String TICKET_CREATED = "zevaro.core.ticket.created";
    public static final String TICKET_RESOLVED = "zevaro.core.ticket.resolved";
    public static final String TICKET_ASSIGNED = "zevaro.core.ticket.assigned";

    // Document events
    public static final String DOCUMENT_PUBLISHED = "zevaro.core.document.published";

    // Comment events
    public static final String COMMENT_CREATED = "zevaro.core.comment.created";

    // Audit events
    public static final String AUDIT_EVENTS = "zevaro.core.audit.events";
}
