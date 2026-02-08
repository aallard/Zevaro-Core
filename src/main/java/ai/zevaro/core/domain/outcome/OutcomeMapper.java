package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.OutcomeResponse;
import ai.zevaro.core.domain.outcome.dto.OutcomeSummary;
import ai.zevaro.core.domain.outcome.dto.UpdateOutcomeRequest;
import ai.zevaro.core.domain.program.ProgramMapper;
import ai.zevaro.core.domain.team.TeamMapper;
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
public class OutcomeMapper {

    private final ObjectMapper objectMapper;
    private final TeamMapper teamMapper;
    private final ProgramMapper programMapper;
    private final UserMapper userMapper;

    public OutcomeResponse toResponse(Outcome outcome, int hypothesisCount, String workstreamName) {
        if (outcome == null) {
            return null;
        }

        return new OutcomeResponse(
                outcome.getId(),
                outcome.getTitle(),
                outcome.getDescription(),
                outcome.getSuccessCriteria(),
                parseJsonToMap(outcome.getTargetMetrics()),
                parseJsonToMap(outcome.getCurrentMetrics()),
                outcome.getStatus(),
                outcome.getPriority(),
                outcome.getTeam() != null ? teamMapper.toSummary(outcome.getTeam()) : null,
                outcome.getOwner() != null ? userMapper.toSummary(outcome.getOwner()) : null,
                outcome.getProgram() != null ? programMapper.toSummary(outcome.getProgram()) : null,
                outcome.getWorkstreamId(),
                workstreamName,
                outcome.getTargetDate(),
                outcome.getStartedAt(),
                outcome.getValidatedAt(),
                outcome.getValidatedBy() != null ? userMapper.toSummary(outcome.getValidatedBy()) : null,
                outcome.getValidationNotes(),
                parseJsonToStringMap(outcome.getExternalRefs()),
                parseJsonToList(outcome.getTags()),
                hypothesisCount,
                outcome.getCreatedAt(),
                outcome.getUpdatedAt()
        );
    }

    public OutcomeSummary toSummary(Outcome outcome) {
        if (outcome == null) {
            return null;
        }
        return new OutcomeSummary(
                outcome.getId(),
                outcome.getTitle(),
                outcome.getStatus(),
                outcome.getPriority(),
                outcome.getTargetDate()
        );
    }

    public Outcome toEntity(CreateOutcomeRequest request, UUID tenantId, UUID createdById) {
        Outcome outcome = new Outcome();
        outcome.setTenantId(tenantId);
        outcome.setTitle(request.title());
        outcome.setDescription(request.description());
        outcome.setSuccessCriteria(request.successCriteria());
        outcome.setTargetMetrics(mapToJson(request.targetMetrics()));
        outcome.setPriority(request.priority() != null ? request.priority() : OutcomePriority.MEDIUM);
        outcome.setTargetDate(request.targetDate());
        outcome.setTags(listToJson(request.tags()));
        outcome.setCreatedById(createdById);
        outcome.setStatus(OutcomeStatus.DRAFT);
        return outcome;
    }

    public void updateEntity(Outcome outcome, UpdateOutcomeRequest request) {
        if (request.title() != null) {
            outcome.setTitle(request.title());
        }
        if (request.description() != null) {
            outcome.setDescription(request.description());
        }
        if (request.successCriteria() != null) {
            outcome.setSuccessCriteria(request.successCriteria());
        }
        if (request.targetMetrics() != null) {
            outcome.setTargetMetrics(mapToJson(request.targetMetrics()));
        }
        if (request.currentMetrics() != null) {
            outcome.setCurrentMetrics(mapToJson(request.currentMetrics()));
        }
        if (request.priority() != null) {
            outcome.setPriority(request.priority());
        }
        if (request.targetDate() != null) {
            outcome.setTargetDate(request.targetDate());
        }
        if (request.tags() != null) {
            outcome.setTags(listToJson(request.tags()));
        }
        if (request.externalRefs() != null) {
            outcome.setExternalRefs(mapToJson(request.externalRefs()));
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to map: {}", json, e);
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
}
