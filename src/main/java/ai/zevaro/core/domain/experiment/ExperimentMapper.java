package ai.zevaro.core.domain.experiment;

import ai.zevaro.core.domain.experiment.dto.CreateExperimentRequest;
import ai.zevaro.core.domain.experiment.dto.ExperimentResponse;
import ai.zevaro.core.domain.experiment.dto.ExperimentSummary;
import ai.zevaro.core.domain.experiment.dto.UpdateExperimentRequest;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisSummary;
import ai.zevaro.core.domain.program.ProgramMapper;
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
public class ExperimentMapper {

    private final ObjectMapper objectMapper;
    private final ProgramMapper programMapper;
    private final UserMapper userMapper;

    public ExperimentResponse toResponse(Experiment experiment) {
        if (experiment == null) {
            return null;
        }

        HypothesisSummary hypothesisSummary = null;
        if (experiment.getHypothesis() != null) {
            hypothesisSummary = new HypothesisSummary(
                    experiment.getHypothesis().getId(),
                    experiment.getHypothesis().getTitle(),
                    experiment.getHypothesis().getStatus(),
                    experiment.getHypothesis().getPriority()
            );
        }

        return new ExperimentResponse(
                experiment.getId(),
                experiment.getName(),
                experiment.getDescription(),
                experiment.getType(),
                experiment.getStatus(),
                parseJsonToMap(experiment.getConfig()),
                experiment.getStartDate(),
                experiment.getEndDate(),
                experiment.getDurationDays(),
                parseJsonToMap(experiment.getResults()),
                experiment.getConclusion(),
                experiment.getTrafficSplit(),
                experiment.getPrimaryMetric(),
                parseJsonToList(experiment.getSecondaryMetrics()),
                experiment.getAudienceFilter(),
                experiment.getSampleSizeTarget(),
                experiment.getCurrentSampleSize(),
                experiment.getControlValue(),
                experiment.getVariantValue(),
                experiment.getConfidenceLevel(),
                experiment.getProgram() != null ? programMapper.toSummary(experiment.getProgram()) : null,
                hypothesisSummary,
                experiment.getOwner() != null ? userMapper.toSummary(experiment.getOwner()) : null,
                experiment.getCreatedAt(),
                experiment.getUpdatedAt()
        );
    }

    public ExperimentSummary toSummary(Experiment experiment) {
        if (experiment == null) {
            return null;
        }
        return new ExperimentSummary(
                experiment.getId(),
                experiment.getName(),
                experiment.getType(),
                experiment.getStatus(),
                experiment.getDurationDays(),
                experiment.getConfidenceLevel()
        );
    }

    public Experiment toEntity(CreateExperimentRequest request, UUID tenantId, UUID createdById) {
        Experiment experiment = new Experiment();
        experiment.setTenantId(tenantId);
        experiment.setName(request.name());
        experiment.setDescription(request.description());
        experiment.setType(request.type());
        experiment.setStatus(ExperimentStatus.DRAFT);
        experiment.setDurationDays(request.durationDays());
        experiment.setTrafficSplit(request.trafficSplit());
        experiment.setPrimaryMetric(request.primaryMetric());
        experiment.setSecondaryMetrics(listToJson(request.secondaryMetrics()));
        experiment.setAudienceFilter(request.audienceFilter());
        experiment.setSampleSizeTarget(request.sampleSizeTarget());
        experiment.setCurrentSampleSize(0);
        experiment.setCreatedById(createdById);
        return experiment;
    }

    public void updateEntity(Experiment experiment, UpdateExperimentRequest request) {
        if (request.name() != null) {
            experiment.setName(request.name());
        }
        if (request.description() != null) {
            experiment.setDescription(request.description());
        }
        if (request.type() != null) {
            experiment.setType(request.type());
        }
        if (request.durationDays() != null) {
            experiment.setDurationDays(request.durationDays());
        }
        if (request.trafficSplit() != null) {
            experiment.setTrafficSplit(request.trafficSplit());
        }
        if (request.primaryMetric() != null) {
            experiment.setPrimaryMetric(request.primaryMetric());
        }
        if (request.secondaryMetrics() != null) {
            experiment.setSecondaryMetrics(listToJson(request.secondaryMetrics()));
        }
        if (request.audienceFilter() != null) {
            experiment.setAudienceFilter(request.audienceFilter());
        }
        if (request.sampleSizeTarget() != null) {
            experiment.setSampleSizeTarget(request.sampleSizeTarget());
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
