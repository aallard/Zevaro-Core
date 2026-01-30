package ai.zevaro.core.domain.decision;

import ai.zevaro.core.domain.decision.dto.BlockedItem;
import ai.zevaro.core.domain.decision.dto.CommentResponse;
import ai.zevaro.core.domain.decision.dto.CreateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.DecisionOption;
import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.decision.dto.DecisionSummary;
import ai.zevaro.core.domain.decision.dto.UpdateDecisionRequest;
import ai.zevaro.core.domain.hypothesis.HypothesisMapper;
import ai.zevaro.core.domain.outcome.OutcomeMapper;
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
public class DecisionMapper {

    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final TeamMapper teamMapper;
    private final OutcomeMapper outcomeMapper;
    private final HypothesisMapper hypothesisMapper;

    public DecisionResponse toResponse(Decision decision, int commentCount) {
        if (decision == null) {
            return null;
        }

        return new DecisionResponse(
                decision.getId(),
                decision.getTitle(),
                decision.getDescription(),
                decision.getContext(),
                parseOptions(decision.getOptions()),
                decision.getStatus(),
                decision.getPriority(),
                decision.getDecisionType(),
                decision.getOwner() != null ? userMapper.toSummary(decision.getOwner()) : null,
                decision.getAssignedTo() != null ? userMapper.toSummary(decision.getAssignedTo()) : null,
                decision.getOutcome() != null ? outcomeMapper.toSummary(decision.getOutcome()) : null,
                decision.getHypothesis() != null ? hypothesisMapper.toSummary(decision.getHypothesis()) : null,
                decision.getTeam() != null ? teamMapper.toSummary(decision.getTeam()) : null,
                decision.getSlaHours(),
                decision.getDueAt(),
                decision.isOverdue(),
                decision.getWaitTimeHours(),
                decision.getEscalationLevel(),
                decision.getEscalatedAt(),
                decision.getEscalatedTo() != null ? userMapper.toSummary(decision.getEscalatedTo()) : null,
                decision.getDecidedBy() != null ? userMapper.toSummary(decision.getDecidedBy()) : null,
                decision.getDecidedAt(),
                decision.getDecisionRationale(),
                parseSelectedOption(decision.getSelectedOption()),
                parseBlockedItems(decision.getBlockedItems()),
                commentCount,
                parseJsonToStringMap(decision.getExternalRefs()),
                parseJsonToList(decision.getTags()),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }

    public DecisionSummary toSummary(Decision decision) {
        if (decision == null) {
            return null;
        }
        return new DecisionSummary(
                decision.getId(),
                decision.getTitle(),
                decision.getStatus(),
                decision.getPriority(),
                decision.getWaitTimeHours(),
                decision.isOverdue(),
                decision.getAssignedTo() != null ? userMapper.toSummary(decision.getAssignedTo()) : null
        );
    }

    public Decision toEntity(CreateDecisionRequest request, UUID tenantId, UUID createdById) {
        Decision decision = new Decision();
        decision.setTenantId(tenantId);
        decision.setTitle(request.title());
        decision.setDescription(request.description());
        decision.setContext(request.context());
        decision.setOptions(optionsToJson(request.options()));
        decision.setPriority(request.priority());
        decision.setDecisionType(request.decisionType());
        decision.setSlaHours(request.slaHours());
        decision.setBlockedItems(blockedItemsToJson(request.blockedItems()));
        decision.setTags(listToJson(request.tags()));
        decision.setCreatedById(createdById);
        decision.setStatus(DecisionStatus.NEEDS_INPUT);
        decision.setEscalationLevel(0);
        return decision;
    }

    public void updateEntity(Decision decision, UpdateDecisionRequest request) {
        if (request.title() != null) {
            decision.setTitle(request.title());
        }
        if (request.description() != null) {
            decision.setDescription(request.description());
        }
        if (request.context() != null) {
            decision.setContext(request.context());
        }
        if (request.options() != null) {
            decision.setOptions(optionsToJson(request.options()));
        }
        if (request.priority() != null) {
            decision.setPriority(request.priority());
        }
        if (request.decisionType() != null) {
            decision.setDecisionType(request.decisionType());
        }
        if (request.slaHours() != null) {
            decision.setSlaHours(request.slaHours());
        }
        if (request.blockedItems() != null) {
            decision.setBlockedItems(blockedItemsToJson(request.blockedItems()));
        }
        if (request.externalRefs() != null) {
            decision.setExternalRefs(mapToJson(request.externalRefs()));
        }
        if (request.tags() != null) {
            decision.setTags(listToJson(request.tags()));
        }
    }

    public CommentResponse toCommentResponse(DecisionComment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor() != null ? userMapper.toSummary(comment.getAuthor()) : null,
                comment.getContent(),
                comment.getOptionId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.isEdited(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private List<DecisionOption> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse options JSON: {}", json, e);
            return null;
        }
    }

    private DecisionOption parseSelectedOption(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, DecisionOption.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse selected option JSON: {}", json, e);
            return null;
        }
    }

    private List<BlockedItem> parseBlockedItems(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse blocked items JSON: {}", json, e);
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

    public String optionsToJson(List<DecisionOption> options) {
        if (options == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert options to JSON", e);
            return null;
        }
    }

    public String selectedOptionToJson(DecisionOption option) {
        if (option == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(option);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert selected option to JSON", e);
            return null;
        }
    }

    private String blockedItemsToJson(List<BlockedItem> items) {
        if (items == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert blocked items to JSON", e);
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
