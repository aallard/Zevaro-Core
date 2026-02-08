package ai.zevaro.core.domain.template;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.program.ProgramService;
import ai.zevaro.core.domain.program.dto.CreateProgramRequest;
import ai.zevaro.core.domain.program.dto.ProgramResponse;
import ai.zevaro.core.domain.template.dto.ApplyTemplateRequest;
import ai.zevaro.core.domain.template.dto.ApplyTemplateResponse;
import ai.zevaro.core.domain.template.dto.CreateTemplateRequest;
import ai.zevaro.core.domain.template.dto.TemplateResponse;
import ai.zevaro.core.domain.workstream.WorkstreamService;
import ai.zevaro.core.domain.workstream.dto.CreateWorkstreamRequest;
import ai.zevaro.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramTemplateService {

    private final ProgramTemplateRepository templateRepository;
    private final ProgramService programService;
    private final WorkstreamService workstreamService;
    private final ProgramTemplateMapper templateMapper;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TemplateResponse create(CreateTemplateRequest req, UUID tenantId, UUID userId) {
        validateStructureJson(req.structure());

        ProgramTemplate template = templateMapper.toEntity(req, tenantId, userId);
        template = templateRepository.save(template);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("PROGRAM_TEMPLATE", template.getId(), template.getName())
                .description("Created program template: " + template.getName()));

        return templateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getById(UUID id, UUID tenantId) {
        ProgramTemplate template = templateRepository.findByIdAccessible(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramTemplate", "id", id));
        return templateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> list(UUID tenantId) {
        return templateRepository.findByTenantIdOrIsSystemTrue(tenantId)
                .stream()
                .map(templateMapper::toResponse)
                .toList();
    }

    @Transactional
    public TemplateResponse update(UUID id, CreateTemplateRequest req, UUID tenantId, UUID userId) {
        ProgramTemplate template = templateRepository.findByIdAccessible(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramTemplate", "id", id));

        if (template.getIsSystem()) {
            throw new IllegalArgumentException("System templates cannot be modified");
        }

        validateStructureJson(req.structure());

        template.setName(req.name());
        template.setDescription(req.description());
        template.setStructure(req.structure());
        template = templateRepository.save(template);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("PROGRAM_TEMPLATE", template.getId(), template.getName())
                .description("Updated program template: " + template.getName()));

        return templateMapper.toResponse(template);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        ProgramTemplate template = templateRepository.findByIdAccessible(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramTemplate", "id", id));

        if (template.getIsSystem()) {
            throw new IllegalArgumentException("System templates cannot be deleted");
        }

        templateRepository.delete(template);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("PROGRAM_TEMPLATE", template.getId(), template.getName())
                .description("Deleted program template: " + template.getName()));
    }

    @Transactional
    public ApplyTemplateResponse applyTemplate(UUID templateId, ApplyTemplateRequest req, UUID tenantId, UUID userId) {
        ProgramTemplate template = templateRepository.findByIdAccessible(templateId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProgramTemplate", "id", templateId));

        List<Map<String, String>> workstreamDefs = parseStructure(template.getStructure());

        // Create the Program
        UUID ownerId = req.ownerId() != null ? req.ownerId() : userId;
        CreateProgramRequest programReq = new CreateProgramRequest(
                req.programName(),
                req.programDescription(),
                null,  // status — defaults
                null,  // color
                ownerId,
                null,  // type
                req.portfolioId(),
                null,  // startDate
                null,  // targetDate
                null   // tags
        );
        ProgramResponse program = programService.createProgram(tenantId, programReq, userId);

        // Create Workstreams from template structure
        List<String> workstreamNames = new ArrayList<>();
        int sortOrder = 0;
        for (Map<String, String> wsDef : workstreamDefs) {
            String wsName = wsDef.get("name");
            String wsDescription = wsDef.get("description");
            String wsMode = wsDef.get("mode");
            String wsExecutionMode = wsDef.get("executionMode");

            CreateWorkstreamRequest wsReq = new CreateWorkstreamRequest(
                    wsName,
                    wsDescription,
                    ai.zevaro.core.domain.workstream.WorkstreamMode.valueOf(wsMode),
                    ai.zevaro.core.domain.workstream.ExecutionMode.valueOf(wsExecutionMode),
                    null,        // ownerId
                    sortOrder++, // sortOrder
                    null         // tags
            );
            workstreamService.create(program.id(), wsReq, tenantId, userId);
            workstreamNames.add(wsName);
        }

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("PROGRAM", program.id(), program.name())
                .description("Applied template '" + template.getName() + "' — created program with " + workstreamNames.size() + " workstreams"));

        return new ApplyTemplateResponse(
                program.id(),
                program.name(),
                workstreamNames.size(),
                workstreamNames
        );
    }

    private void validateStructureJson(String json) {
        try {
            List<Map<String, String>> parsed = objectMapper.readValue(json, new TypeReference<>() {});
            if (parsed == null || parsed.isEmpty()) {
                throw new IllegalArgumentException("Template structure must contain at least one workstream definition");
            }
            for (Map<String, String> ws : parsed) {
                if (ws.get("name") == null || ws.get("name").isBlank()) {
                    throw new IllegalArgumentException("Each workstream definition must have a name");
                }
                if (ws.get("mode") == null) {
                    throw new IllegalArgumentException("Each workstream definition must have a mode");
                }
                if (ws.get("executionMode") == null) {
                    throw new IllegalArgumentException("Each workstream definition must have an executionMode");
                }
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON structure: " + e.getMessage());
        }
    }

    private List<Map<String, String>> parseStructure(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid template structure JSON: " + e.getMessage());
        }
    }
}
