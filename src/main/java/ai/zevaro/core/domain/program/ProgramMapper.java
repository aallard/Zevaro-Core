package ai.zevaro.core.domain.program;

import ai.zevaro.core.domain.program.dto.CreateProgramRequest;
import ai.zevaro.core.domain.program.dto.ProgramResponse;
import ai.zevaro.core.domain.program.dto.ProgramSummary;
import ai.zevaro.core.domain.program.dto.UpdateProgramRequest;
import ai.zevaro.core.domain.user.UserMapper;
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
public class ProgramMapper {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public ProgramResponse toResponse(Program program, int decisionCount, int outcomeCount,
                                       int hypothesisCount, int teamMemberCount) {
        if (program == null) {
            return null;
        }

        return new ProgramResponse(
                program.getId(),
                program.getName(),
                program.getSlug(),
                program.getDescription(),
                program.getStatus(),
                program.getType(),
                program.getPortfolioId(),
                program.getColor(),
                program.getIconUrl(),
                program.getOwner() != null ? userMapper.toSummary(program.getOwner()) : null,
                program.getStartDate(),
                program.getTargetDate(),
                parseJsonToList(program.getTags()),
                decisionCount,
                outcomeCount,
                hypothesisCount,
                teamMemberCount,
                program.getCreatedAt(),
                program.getUpdatedAt()
        );
    }

    public ProgramSummary toSummary(Program program) {
        if (program == null) {
            return null;
        }

        return new ProgramSummary(
                program.getId(),
                program.getName(),
                program.getSlug(),
                program.getStatus(),
                program.getType(),
                program.getColor()
        );
    }

    public Program toEntity(CreateProgramRequest request, UUID tenantId, UUID createdById) {
        Program program = new Program();
        program.setTenantId(tenantId);
        program.setName(request.name());
        program.setSlug(generateSlug(request.name()));
        program.setDescription(request.description());
        program.setStatus(request.status() != null ? request.status() : ProgramStatus.ACTIVE);
        program.setType(request.type() != null ? request.type() : ProgramType.INITIATIVE);
        program.setColor(request.color());
        program.setPortfolioId(request.portfolioId());
        program.setStartDate(request.startDate());
        program.setTargetDate(request.targetDate());
        program.setTags(listToJson(request.tags()));
        program.setCreatedById(createdById);
        return program;
    }

    public void updateEntity(Program program, UpdateProgramRequest request) {
        if (request.name() != null) {
            program.setName(request.name());
        }
        if (request.description() != null) {
            program.setDescription(request.description());
        }
        if (request.status() != null) {
            program.setStatus(request.status());
        }
        if (request.color() != null) {
            program.setColor(request.color());
        }
        if (request.iconUrl() != null) {
            program.setIconUrl(request.iconUrl());
        }
        if (request.type() != null) {
            program.setType(request.type());
        }
        if (request.portfolioId() != null) {
            program.setPortfolioId(request.portfolioId());
        }
        if (request.startDate() != null) {
            program.setStartDate(request.startDate());
        }
        if (request.targetDate() != null) {
            program.setTargetDate(request.targetDate());
        }
        if (request.tags() != null) {
            program.setTags(listToJson(request.tags()));
        }
    }

    public String generateSlug(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        return name
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-+", "-");
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
