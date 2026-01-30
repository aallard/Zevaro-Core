package ai.zevaro.core.domain.decision;

public enum DecisionPriority {
    BLOCKING,   // SLA: 4 hours - Blocking active development
    HIGH,       // SLA: 8 hours - Important, prioritize
    NORMAL,     // SLA: 24 hours - Standard priority
    LOW         // SLA: 72 hours - Can wait
}
