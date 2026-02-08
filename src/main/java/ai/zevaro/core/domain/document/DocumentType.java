package ai.zevaro.core.domain.document;

public enum DocumentType {
    PAGE,              // General wiki page
    SPECIFICATION,     // Linked to a Specification entity
    TEMPLATE,          // Reusable page template
    MEETING_NOTES,     // Meeting minutes
    DECISION_RECORD,   // Architecture Decision Record (ADR)
    RFC,               // Request for Comments
    RUNBOOK            // Operational procedure
}
