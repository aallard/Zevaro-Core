package ai.zevaro.core.domain.program;

public enum ProgramType {
    INITIATIVE,  // Has target end date (e.g. "Replace Lawson")
    ONGOING      // No end date, KTLO-style (e.g. "Legacy Integration Support")
}
