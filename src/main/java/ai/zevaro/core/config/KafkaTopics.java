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
    public static final String HYPOTHESIS_CONCLUDED = "zevaro.core.hypothesis.concluded";
}
