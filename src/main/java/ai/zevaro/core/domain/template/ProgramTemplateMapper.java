package ai.zevaro.core.domain.template;

import ai.zevaro.core.domain.template.dto.CreateTemplateRequest;
import ai.zevaro.core.domain.template.dto.TemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProgramTemplateMapper {

    public ProgramTemplate toEntity(CreateTemplateRequest req, UUID tenantId, UUID userId) {
        ProgramTemplate template = new ProgramTemplate();
        template.setTenantId(tenantId);
        template.setName(req.name());
        template.setDescription(req.description());
        template.setStructure(req.structure());
        template.setIsSystem(false);
        template.setCreatedById(userId);
        return template;
    }

    public TemplateResponse toResponse(ProgramTemplate template) {
        if (template == null) {
            return null;
        }

        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getStructure(),
                template.getIsSystem(),
                template.getCreatedById(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
