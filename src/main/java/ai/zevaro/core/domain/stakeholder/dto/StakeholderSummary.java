package ai.zevaro.core.domain.stakeholder.dto;

import ai.zevaro.core.domain.stakeholder.StakeholderType;

import java.util.UUID;

public record StakeholderSummary(
        UUID id,
        String name,
        String email,
        String title,
        String organization,
        StakeholderType type
) {}
