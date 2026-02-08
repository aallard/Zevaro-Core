package ai.zevaro.core.domain.requirement;

public enum RequirementPriority {
    MUST_HAVE,    // Non-negotiable for this release
    SHOULD_HAVE,  // Important but not critical
    COULD_HAVE,   // Desirable if time permits
    WONT_HAVE     // Explicitly out of scope (documented for clarity)
}
