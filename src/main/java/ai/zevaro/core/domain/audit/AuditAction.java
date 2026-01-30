package ai.zevaro.core.domain.audit;

public enum AuditAction {
    // CRUD
    CREATE,
    READ,
    UPDATE,
    DELETE,

    // Status changes
    STATUS_CHANGE,

    // Decision-specific
    DECISION_RESOLVED,
    DECISION_ESCALATED,
    DECISION_ASSIGNED,
    DECISION_COMMENTED,

    // Outcome-specific
    OUTCOME_VALIDATED,
    OUTCOME_INVALIDATED,

    // Hypothesis-specific
    HYPOTHESIS_TRANSITIONED,
    HYPOTHESIS_CONCLUDED,
    HYPOTHESIS_BLOCKED,
    HYPOTHESIS_UNBLOCKED,

    // Auth
    LOGIN,
    LOGOUT,
    PASSWORD_CHANGED,

    // Admin
    USER_CREATED,
    USER_DEACTIVATED,
    ROLE_ASSIGNED,
    TEAM_MEMBER_ADDED,
    TEAM_MEMBER_REMOVED
}
