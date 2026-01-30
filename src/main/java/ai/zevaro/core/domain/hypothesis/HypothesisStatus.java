package ai.zevaro.core.domain.hypothesis;

/**
 * Status state machine for hypotheses.
 * Flow: DRAFT -> READY -> BUILDING -> DEPLOYED -> MEASURING -> VALIDATED/INVALIDATED
 * BLOCKED and ABANDONED can be reached from most states.
 */
public enum HypothesisStatus {
    DRAFT,
    READY,
    BLOCKED,
    BUILDING,
    DEPLOYED,
    MEASURING,
    VALIDATED,
    INVALIDATED,
    ABANDONED
}
