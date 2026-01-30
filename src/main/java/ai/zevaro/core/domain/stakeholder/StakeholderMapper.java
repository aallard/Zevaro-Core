package ai.zevaro.core.domain.stakeholder;

import ai.zevaro.core.domain.decision.dto.DecisionSummary;
import ai.zevaro.core.domain.stakeholder.dto.CreateStakeholderRequest;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderMetrics;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderResponse;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderSummary;
import ai.zevaro.core.domain.stakeholder.dto.UpdateStakeholderRequest;
import ai.zevaro.core.domain.user.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StakeholderMapper {

    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public StakeholderResponse toResponse(Stakeholder stakeholder) {
        if (stakeholder == null) {
            return null;
        }

        return new StakeholderResponse(
                stakeholder.getId(),
                stakeholder.getName(),
                stakeholder.getEmail(),
                stakeholder.getTitle(),
                stakeholder.getOrganization(),
                stakeholder.getPhone(),
                stakeholder.getAvatarUrl(),
                stakeholder.getType(),
                stakeholder.getUser() != null ? userMapper.toSummary(stakeholder.getUser()) : null,
                parseJsonToList(stakeholder.getExpertise()),
                stakeholder.getPreferredContactMethod(),
                stakeholder.getAvailabilityNotes(),
                stakeholder.getTimezone(),
                stakeholder.getDecisionsPending(),
                stakeholder.getDecisionsCompleted(),
                stakeholder.getDecisionsEscalated(),
                stakeholder.getAvgResponseTimeHours(),
                stakeholder.getLastDecisionAt(),
                stakeholder.isActive(),
                stakeholder.getNotes(),
                parseJsonToStringMap(stakeholder.getExternalRefs()),
                stakeholder.getCreatedAt(),
                stakeholder.getUpdatedAt()
        );
    }

    public StakeholderSummary toSummary(Stakeholder stakeholder) {
        if (stakeholder == null) {
            return null;
        }
        return new StakeholderSummary(
                stakeholder.getId(),
                stakeholder.getName(),
                stakeholder.getEmail(),
                stakeholder.getTitle(),
                stakeholder.getOrganization(),
                stakeholder.getType()
        );
    }

    public StakeholderMetrics toMetrics(Stakeholder stakeholder, List<DecisionSummary> pendingDecisions) {
        if (stakeholder == null) {
            return null;
        }

        Double escalationRate = null;
        if (stakeholder.getDecisionsCompleted() != null && stakeholder.getDecisionsCompleted() > 0) {
            escalationRate = (double) stakeholder.getDecisionsEscalated() / stakeholder.getDecisionsCompleted();
        }

        return new StakeholderMetrics(
                stakeholder.getId(),
                stakeholder.getName(),
                stakeholder.getDecisionsPending(),
                stakeholder.getDecisionsCompleted(),
                stakeholder.getDecisionsEscalated(),
                stakeholder.getAvgResponseTimeHours(),
                escalationRate,
                stakeholder.getLastDecisionAt(),
                pendingDecisions
        );
    }

    public Stakeholder toEntity(CreateStakeholderRequest request, UUID tenantId, UUID createdById) {
        Stakeholder stakeholder = new Stakeholder();
        stakeholder.setTenantId(tenantId);
        stakeholder.setName(request.name());
        stakeholder.setEmail(request.email());
        stakeholder.setTitle(request.title());
        stakeholder.setOrganization(request.organization());
        stakeholder.setPhone(request.phone());
        stakeholder.setAvatarUrl(request.avatarUrl());
        stakeholder.setType(request.type() != null ? request.type() : StakeholderType.INTERNAL);
        stakeholder.setExpertise(listToJson(request.expertise()));
        stakeholder.setPreferredContactMethod(request.preferredContactMethod());
        stakeholder.setAvailabilityNotes(request.availabilityNotes());
        stakeholder.setTimezone(request.timezone());
        stakeholder.setNotes(request.notes());
        stakeholder.setExternalRefs(mapToJson(request.externalRefs()));
        stakeholder.setCreatedById(createdById);
        stakeholder.setActive(true);
        stakeholder.setDecisionsPending(0);
        stakeholder.setDecisionsCompleted(0);
        stakeholder.setDecisionsEscalated(0);
        return stakeholder;
    }

    public void updateEntity(Stakeholder stakeholder, UpdateStakeholderRequest request) {
        if (request.name() != null) {
            stakeholder.setName(request.name());
        }
        if (request.email() != null) {
            stakeholder.setEmail(request.email());
        }
        if (request.title() != null) {
            stakeholder.setTitle(request.title());
        }
        if (request.organization() != null) {
            stakeholder.setOrganization(request.organization());
        }
        if (request.phone() != null) {
            stakeholder.setPhone(request.phone());
        }
        if (request.avatarUrl() != null) {
            stakeholder.setAvatarUrl(request.avatarUrl());
        }
        if (request.type() != null) {
            stakeholder.setType(request.type());
        }
        if (request.expertise() != null) {
            stakeholder.setExpertise(listToJson(request.expertise()));
        }
        if (request.preferredContactMethod() != null) {
            stakeholder.setPreferredContactMethod(request.preferredContactMethod());
        }
        if (request.availabilityNotes() != null) {
            stakeholder.setAvailabilityNotes(request.availabilityNotes());
        }
        if (request.timezone() != null) {
            stakeholder.setTimezone(request.timezone());
        }
        if (request.notes() != null) {
            stakeholder.setNotes(request.notes());
        }
        if (request.externalRefs() != null) {
            stakeholder.setExternalRefs(mapToJson(request.externalRefs()));
        }
        if (request.active() != null) {
            stakeholder.setActive(request.active());
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to list: {}", json, e);
            return null;
        }
    }

    private Map<String, String> parseJsonToStringMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to string map: {}", json, e);
            return null;
        }
    }

    private String listToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert list to JSON", e);
            return null;
        }
    }

    private String mapToJson(Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert map to JSON", e);
            return null;
        }
    }
}
