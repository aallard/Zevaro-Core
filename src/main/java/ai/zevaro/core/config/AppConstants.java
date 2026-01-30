package ai.zevaro.core.config;

public final class AppConstants {
    private AppConstants() {}

    // Timeouts
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int DECISION_SLA_DEFAULT_HOURS = 24;
    public static final int ESCALATION_THRESHOLD_HOURS = 48;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Decision SLAs (hours)
    public static final int SLA_BLOCKING = 4;
    public static final int SLA_HIGH = 8;
    public static final int SLA_NORMAL = 24;
    public static final int SLA_LOW = 72;

    // Validation
    public static final int TITLE_MAX_LENGTH = 500;
    public static final int DESCRIPTION_MAX_LENGTH = 10000;
}
