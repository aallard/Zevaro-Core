package ai.zevaro.core.domain.requirement;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.requirement.dto.CreateDependencyRequest;
import ai.zevaro.core.domain.requirement.dto.CreateRequirementRequest;
import ai.zevaro.core.domain.requirement.dto.DependencyResponse;
import ai.zevaro.core.domain.requirement.dto.RequirementResponse;
import ai.zevaro.core.domain.requirement.dto.UpdateRequirementRequest;
import ai.zevaro.core.domain.specification.Specification;
import ai.zevaro.core.domain.specification.SpecificationRepository;
import ai.zevaro.core.event.EventPublisher;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final RequirementDependencyRepository dependencyRepository;
    private final SpecificationRepository specificationRepository;
    private final RequirementMapper requirementMapper;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    // --- CRUD ---

    @Transactional
    public RequirementResponse create(UUID specificationId, CreateRequirementRequest req, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(specificationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", specificationId));

        String identifier = generateIdentifier(tenantId, specificationId);

        Requirement requirement = requirementMapper.toEntity(
                req, specificationId, spec.getWorkstreamId(), spec.getProgramId(), tenantId, userId, identifier);
        requirement = requirementRepository.save(requirement);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("REQUIREMENT", requirement.getId(), requirement.getIdentifier())
                .description("Created requirement: " + requirement.getIdentifier() + " - " + requirement.getTitle()));

        return buildResponse(requirement);
    }

    @Transactional(readOnly = true)
    public RequirementResponse getById(UUID id, UUID tenantId) {
        Requirement requirement = requirementRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));
        return buildResponse(requirement);
    }

    @Transactional(readOnly = true)
    public List<RequirementResponse> listBySpecification(UUID specificationId, UUID tenantId) {
        specificationRepository.findByIdAndTenantId(specificationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", specificationId));

        return requirementRepository.findByTenantIdAndSpecificationIdOrderBySortOrderAsc(tenantId, specificationId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RequirementResponse> listBySpecificationPaged(UUID specificationId, UUID tenantId, Pageable pageable) {
        specificationRepository.findByIdAndTenantId(specificationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", specificationId));

        return requirementRepository.findByTenantIdAndSpecificationId(tenantId, specificationId, pageable)
                .map(this::buildResponse);
    }

    @Transactional
    public RequirementResponse update(UUID id, UpdateRequirementRequest req, UUID tenantId, UUID userId) {
        Requirement requirement = requirementRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));

        String oldStatus = requirement.getStatus().name();
        requirementMapper.applyUpdate(requirement, req);
        requirement = requirementRepository.save(requirement);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("REQUIREMENT", requirement.getId(), requirement.getIdentifier())
                .description("Updated requirement: " + requirement.getIdentifier()));

        if (!oldStatus.equals(requirement.getStatus().name())) {
            eventPublisher.publishRequirementStatusChanged(requirement, oldStatus, userId);
        }

        return buildResponse(requirement);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Requirement requirement = requirementRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));

        // Delete all dependencies where this requirement is source or target
        List<RequirementDependency> asDep = dependencyRepository.findByRequirementId(id);
        List<RequirementDependency> asTarget = dependencyRepository.findByDependsOnId(id);
        dependencyRepository.deleteAll(asDep);
        dependencyRepository.deleteAll(asTarget);

        requirementRepository.delete(requirement);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("REQUIREMENT", requirement.getId(), requirement.getIdentifier())
                .description("Deleted requirement: " + requirement.getIdentifier()));
    }

    // --- Dependencies ---

    @Transactional
    public DependencyResponse addDependency(UUID requirementId, CreateDependencyRequest req, UUID tenantId, UUID userId) {
        Requirement source = requirementRepository.findByIdAndTenantId(requirementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", requirementId));
        Requirement target = requirementRepository.findByIdAndTenantId(req.dependsOnId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", req.dependsOnId()));

        if (requirementId.equals(req.dependsOnId())) {
            throw new IllegalArgumentException("A requirement cannot depend on itself");
        }

        if (dependencyRepository.existsByRequirementIdAndDependsOnId(requirementId, req.dependsOnId())) {
            throw new IllegalArgumentException("Dependency already exists");
        }

        RequirementDependency dep = new RequirementDependency();
        dep.setRequirementId(requirementId);
        dep.setDependsOnId(req.dependsOnId());
        dep.setType(req.type());
        dep = dependencyRepository.save(dep);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("REQUIREMENT_DEPENDENCY", dep.getId(), source.getIdentifier() + " → " + target.getIdentifier())
                .description("Added dependency: " + source.getIdentifier() + " depends on " + target.getIdentifier()));

        return requirementMapper.toDependencyResponse(dep, source, target);
    }

    @Transactional
    public void removeDependency(UUID requirementId, UUID dependsOnId, UUID tenantId, UUID userId) {
        Requirement source = requirementRepository.findByIdAndTenantId(requirementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", requirementId));

        RequirementDependency dep = dependencyRepository.findByRequirementIdAndDependsOnId(requirementId, dependsOnId)
                .orElseThrow(() -> new ResourceNotFoundException("RequirementDependency", "requirementId+dependsOnId",
                        requirementId + "+" + dependsOnId));

        dependencyRepository.delete(dep);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("REQUIREMENT_DEPENDENCY", dep.getId(), source.getIdentifier())
                .description("Removed dependency from " + source.getIdentifier()));
    }

    @Transactional(readOnly = true)
    public List<DependencyResponse> getDependencies(UUID requirementId, UUID tenantId) {
        requirementRepository.findByIdAndTenantId(requirementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", requirementId));

        return dependencyRepository.findByRequirementId(requirementId).stream()
                .map(dep -> {
                    Requirement source = requirementRepository.findById(dep.getRequirementId()).orElse(null);
                    Requirement target = requirementRepository.findById(dep.getDependsOnId()).orElse(null);
                    return requirementMapper.toDependencyResponse(dep, source, target);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DependencyResponse> getDependedOnBy(UUID requirementId, UUID tenantId) {
        requirementRepository.findByIdAndTenantId(requirementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", requirementId));

        return dependencyRepository.findByDependsOnId(requirementId).stream()
                .map(dep -> {
                    Requirement source = requirementRepository.findById(dep.getRequirementId()).orElse(null);
                    Requirement target = requirementRepository.findById(dep.getDependsOnId()).orElse(null);
                    return requirementMapper.toDependencyResponse(dep, source, target);
                })
                .toList();
    }

    // --- Helpers ---

    private String generateIdentifier(UUID tenantId, UUID specificationId) {
        int nextNumber = requirementRepository.findMaxIdentifierNumber(tenantId, specificationId)
                .map(max -> max + 1)
                .orElse(1);
        return String.format("REQ-%03d", nextNumber);
    }

    private RequirementResponse buildResponse(Requirement req) {
        String specName = specificationRepository.findByIdAndTenantId(req.getSpecificationId(), req.getTenantId())
                .map(Specification::getName)
                .orElse(null);

        List<DependencyResponse> deps = dependencyRepository.findByRequirementId(req.getId()).stream()
                .map(dep -> {
                    Requirement source = requirementRepository.findById(dep.getRequirementId()).orElse(null);
                    Requirement target = requirementRepository.findById(dep.getDependsOnId()).orElse(null);
                    return requirementMapper.toDependencyResponse(dep, source, target);
                })
                .toList();

        List<DependencyResponse> dependedOnBy = dependencyRepository.findByDependsOnId(req.getId()).stream()
                .map(dep -> {
                    Requirement source = requirementRepository.findById(dep.getRequirementId()).orElse(null);
                    Requirement target = requirementRepository.findById(dep.getDependsOnId()).orElse(null);
                    return requirementMapper.toDependencyResponse(dep, source, target);
                })
                .toList();

        return requirementMapper.toResponse(req, specName, deps, dependedOnBy);
    }
}
