package ai.zevaro.core.domain.stakeholder.dto;

import ai.zevaro.core.domain.stakeholder.StakeholderType;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record StakeholderResponse(
        UUID id,
        String name,
        String email,
        String title,
        String organization,
        String phone,
        String avatarUrl,
        StakeholderType type,
        UserSummary user,
        List<String> expertise,
        String preferredContactMethod,
        String availabilityNotes,
        String timezone,
        Integer decisionsPending,
        Integer decisionsCompleted,
        Integer decisionsEscalated,
        Double avgResponseTimeHours,
        Instant lastDecisionAt,
        boolean active,
        String notes,
        Map<String, String> externalRefs,
        Instant createdAt,
        Instant updatedAt
) {}
