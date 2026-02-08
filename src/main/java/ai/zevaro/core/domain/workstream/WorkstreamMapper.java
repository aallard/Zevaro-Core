package ai.zevaro.core.domain.workstream;

import ai.zevaro.core.domain.workstream.dto.CreateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.UpdateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.WorkstreamResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkstreamMapper {

    private final ObjectMapper objectMapper;

    public Workstream toEntity(CreateWorkstreamRequest request, UUID programId, UUID tenantId, UUID createdById) {
        Workstream ws = new Workstream();
        ws.setTenantId(tenantId);
        ws.setProgramId(programId);
        ws.setName(request.name());
        ws.setDescription(request.description());
        ws.setMode(request.mode());
        ws.setExecutionMode(request.executionMode());
        ws.setStatus(WorkstreamStatus.NOT_STARTED);
        ws.setOwnerId(request.ownerId());
        ws.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        ws.setTags(listToJson(request.tags()));
        ws.setCreatedById(createdById);
        return ws;
    }

    public WorkstreamResponse toResponse(Workstream ws, String programName, String ownerName, int childCount) {
        return new WorkstreamResponse(
                ws.getId(),
                ws.getProgramId(),
                programName,
                ws.getName(),
                ws.getDescription(),
                ws.getMode(),
                ws.getExecutionMode(),
                ws.getStatus(),
                ws.getOwnerId(),
                ownerName,
                ws.getSortOrder(),
                parseJsonToList(ws.getTags()),
                childCount,
                ws.getCreatedAt(),
                ws.getUpdatedAt()
        );
    }

    public void applyUpdate(Workstream existing, UpdateWorkstreamRequest request) {
        if (request.name() != null) {
            existing.setName(request.name());
        }
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        if (request.status() != null) {
            existing.setStatus(request.status());
        }
        if (request.executionMode() != null) {
            existing.setExecutionMode(request.executionMode());
        }
        if (request.ownerId() != null) {
            existing.setOwnerId(request.ownerId());
        }
        if (request.sortOrder() != null) {
            existing.setSortOrder(request.sortOrder());
        }
        if (request.tags() != null) {
            existing.setTags(listToJson(request.tags()));
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
