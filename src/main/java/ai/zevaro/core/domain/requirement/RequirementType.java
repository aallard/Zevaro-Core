package ai.zevaro.core.domain.requirement;

public enum RequirementType {
    FUNCTIONAL,       // "The system shall..."
    NON_FUNCTIONAL,   // Performance, security, scalability
    CONSTRAINT,       // Technology or business constraints
    INTERFACE         // External system integration requirements
}
