package ai.zevaro.core.domain.decision;

public enum DecisionStatus {
    NEEDS_INPUT,       // Waiting for stakeholder input
    UNDER_DISCUSSION,  // Active discussion in progress
    DECIDED,           // Decision made, ready to implement
    IMPLEMENTED,       // Decision has been acted upon
    DEFERRED,          // Postponed to later
    CANCELLED          // No longer relevant
}
