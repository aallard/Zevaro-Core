package ai.zevaro.core.domain.stakeholder.dto;

import ai.zevaro.core.domain.stakeholder.StakeholderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateStakeholderRequest(
        @Size(max = 200) String name,
        @Email String email,
        String title,
        String organization,
        String phone,
        String avatarUrl,
        StakeholderType type,
        UUID userId,
        List<String> expertise,
        String preferredContactMethod,
        String availabilityNotes,
        String timezone,
        String notes,
        Map<String, String> externalRefs,
        Boolean active
) {}
