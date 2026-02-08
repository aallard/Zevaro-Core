package ai.zevaro.core.domain.requirement;

public enum DependencyType {
    BLOCKS,      // This requirement blocks the dependent
    REQUIRES,    // Dependent requires this to be done first
    RELATES_TO   // Informational relationship, no ordering constraint
}
