package ai.zevaro.core.domain.link;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.link.dto.CreateEntityLinkRequest;
import ai.zevaro.core.domain.link.dto.EntityLinkResponse;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.requirement.RequirementRepository;
import ai.zevaro.core.domain.specification.SpecificationRepository;
import ai.zevaro.core.domain.ticket.TicketRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityLinkService {

    private final EntityLinkRepository entityLinkRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ProgramRepository programRepository;
    private final WorkstreamRepository workstreamRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final DecisionRepository decisionRepository;
    private final SpecificationRepository specificationRepository;
    private final RequirementRepository requirementRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public EntityLinkResponse create(CreateEntityLinkRequest request, UUID tenantId, UUID userId) {
        if (request.sourceType() == request.targetType()
                && request.sourceId().equals(request.targetId())) {
            throw new IllegalStateException("Cannot create a self-link");
        }

        if (entityLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetIdAndLinkType(
                request.sourceType(), request.sourceId(),
                request.targetType(), request.targetId(),
                request.linkType())) {
            throw new IllegalStateException("This link already exists");
        }

        EntityLink link = new EntityLink();
        link.setTenantId(tenantId);
        link.setSourceType(request.sourceType());
        link.setSourceId(request.sourceId());
        link.setTargetType(request.targetType());
        link.setTargetId(request.targetId());
        link.setLinkType(request.linkType());
        link.setCreatedById(userId);

        link = entityLinkRepository.save(link);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("EntityLink", link.getId(), null)
                .description(request.sourceType() + " " + request.sourceId()
                        + " " + request.linkType() + " " + request.targetType() + " " + request.targetId()));

        return toResponse(link, tenantId);
    }

    @Transactional(readOnly = true)
    public List<EntityLinkResponse> getLinksFrom(EntityType entityType, UUID entityId, UUID tenantId) {
        return entityLinkRepository.findByTenantIdAndSourceTypeAndSourceId(tenantId, entityType, entityId)
                .stream()
                .map(link -> toResponse(link, tenantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntityLinkResponse> getLinksTo(EntityType entityType, UUID entityId, UUID tenantId) {
        return entityLinkRepository.findByTenantIdAndTargetTypeAndTargetId(tenantId, entityType, entityId)
                .stream()
                .map(link -> toResponse(link, tenantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntityLinkResponse> getAllLinks(EntityType entityType, UUID entityId, UUID tenantId) {
        Set<EntityLinkResponse> combined = new LinkedHashSet<>();
        combined.addAll(getLinksFrom(entityType, entityId, tenantId));
        combined.addAll(getLinksTo(entityType, entityId, tenantId));
        return new ArrayList<>(combined);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        EntityLink link = entityLinkRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EntityLink", "id", id));

        entityLinkRepository.delete(link);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("EntityLink", id, null)
                .description("Entity link deleted"));
    }

    private String resolveTitle(EntityType type, UUID id, UUID tenantId) {
        if (id == null) {
            return null;
        }
        return switch (type) {
            case PROGRAM -> programRepository.findByIdAndTenantId(id, tenantId)
                    .map(p -> p.getName()).orElse(null);
            case WORKSTREAM -> workstreamRepository.findByIdAndTenantId(id, tenantId)
                    .map(w -> w.getName()).orElse(null);
            case OUTCOME -> outcomeRepository.findByIdAndTenantId(id, tenantId)
                    .map(o -> o.getTitle()).orElse(null);
            case HYPOTHESIS -> hypothesisRepository.findByIdAndTenantId(id, tenantId)
                    .map(h -> h.getTitle()).orElse(null);
            case DECISION -> decisionRepository.findByIdAndTenantId(id, tenantId)
                    .map(d -> d.getTitle()).orElse(null);
            case SPECIFICATION -> specificationRepository.findByIdAndTenantId(id, tenantId)
                    .map(s -> s.getName()).orElse(null);
            case REQUIREMENT -> requirementRepository.findByIdAndTenantId(id, tenantId)
                    .map(r -> r.getTitle()).orElse(null);
            case TICKET -> ticketRepository.findByIdAndTenantId(id, tenantId)
                    .map(t -> t.getTitle()).orElse(null);
            case PORTFOLIO, EXPERIMENT, DOCUMENT, SPACE -> null;
        };
    }

    private EntityLinkResponse toResponse(EntityLink link, UUID tenantId) {
        String sourceTitle = resolveTitle(link.getSourceType(), link.getSourceId(), tenantId);
        String targetTitle = resolveTitle(link.getTargetType(), link.getTargetId(), tenantId);
        String createdByName = userRepository.findById(link.getCreatedById())
                .map(User::getFullName)
                .orElse(null);

        return new EntityLinkResponse(
                link.getId(),
                link.getSourceType(),
                link.getSourceId(),
                sourceTitle,
                link.getTargetType(),
                link.getTargetId(),
                targetTitle,
                link.getLinkType(),
                link.getCreatedById(),
                createdByName,
                link.getCreatedAt()
        );
    }
}
