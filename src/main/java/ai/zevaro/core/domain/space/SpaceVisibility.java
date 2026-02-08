package ai.zevaro.core.domain.space;

public enum SpaceVisibility {
    PUBLIC,      // Visible to all tenant users
    PRIVATE,     // Only owner and explicit members
    RESTRICTED   // Visible but read-only to non-members
}
