package ai.zevaro.core.domain.specification;

public enum SpecificationStatus {
    DRAFT,          // Initial state, being written
    IN_REVIEW,      // Submitted for review
    APPROVED,       // Approved by reviewer, ready for work
    IN_PROGRESS,    // Being built (AI or human)
    DELIVERED,      // All requirements implemented
    ACCEPTED        // Verified and accepted by stakeholder/owner
}
