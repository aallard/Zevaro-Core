package ai.zevaro.core.domain.hypothesis;

import ai.zevaro.core.domain.hypothesis.dto.CreateHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisResponse;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisSummary;
import ai.zevaro.core.domain.hypothesis.dto.UpdateHypothesisRequest;
import ai.zevaro.core.domain.outcome.OutcomeMapper;
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
public class HypothesisMapper {

    private final ObjectMapper objectMapper;
    private final OutcomeMapper outcomeMapper;
    private final UserMapper userMapper;

    public HypothesisResponse toResponse(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return null;
        }

        return new HypothesisResponse(
                hypothesis.getId(),
                hypothesis.getOutcome() != null ? outcomeMapper.toSummary(hypothesis.getOutcome()) : null,
                hypothesis.getTitle(),
                hypothesis.getBelief(),
                hypothesis.getExpectedResult(),
                hypothesis.getMeasurementCriteria(),
                hypothesis.getStatus(),
                hypothesis.getPriority(),
                hypothesis.getOwner() != null ? userMapper.toSummary(hypothesis.getOwner()) : null,
                parseJsonToMap(hypothesis.getExperimentConfig()),
                parseJsonToMap(hypothesis.getExperimentResults()),
                hypothesis.getBlockedReason(),
                hypothesis.getConclusionNotes(),
                parseJsonToStringMap(hypothesis.getExternalRefs()),
                parseJsonToList(hypothesis.getTags()),
                hypothesis.getStartedAt(),
                hypothesis.getDeployedAt(),
                hypothesis.getMeasuringStartedAt(),
                hypothesis.getConcludedAt(),
                hypothesis.getConcludedBy() != null ? userMapper.toSummary(hypothesis.getConcludedBy()) : null,
                hypothesis.getCreatedAt(),
                hypothesis.getUpdatedAt()
        );
    }

    public HypothesisSummary toSummary(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return null;
        }
        return new HypothesisSummary(
                hypothesis.getId(),
                hypothesis.getTitle(),
                hypothesis.getStatus(),
                hypothesis.getPriority()
        );
    }

    public Hypothesis toEntity(CreateHypothesisRequest request, UUID tenantId, UUID createdById) {
        Hypothesis hypothesis = new Hypothesis();
        hypothesis.setTenantId(tenantId);
        hypothesis.setTitle(request.title());
        hypothesis.setBelief(request.belief());
        hypothesis.setExpectedResult(request.expectedResult());
        hypothesis.setMeasurementCriteria(request.measurementCriteria());
        hypothesis.setPriority(request.priority() != null ? request.priority() : HypothesisPriority.MEDIUM);
        hypothesis.setExperimentConfig(mapToJson(request.experimentConfig()));
        hypothesis.setTags(listToJson(request.tags()));
        hypothesis.setCreatedById(createdById);
        hypothesis.setStatus(HypothesisStatus.DRAFT);
        return hypothesis;
    }

    public void updateEntity(Hypothesis hypothesis, UpdateHypothesisRequest request) {
        if (request.title() != null) {
            hypothesis.setTitle(request.title());
        }
        if (request.belief() != null) {
            hypothesis.setBelief(request.belief());
        }
        if (request.expectedResult() != null) {
            hypothesis.setExpectedResult(request.expectedResult());
        }
        if (request.measurementCriteria() != null) {
            hypothesis.setMeasurementCriteria(request.measurementCriteria());
        }
        if (request.priority() != null) {
            hypothesis.setPriority(request.priority());
        }
        if (request.experimentConfig() != null) {
            hypothesis.setExperimentConfig(mapToJson(request.experimentConfig()));
        }
        if (request.tags() != null) {
            hypothesis.setTags(listToJson(request.tags()));
        }
        if (request.externalRefs() != null) {
            hypothesis.setExternalRefs(mapToJson(request.externalRefs()));
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
