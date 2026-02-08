package ai.zevaro.core.domain.specification;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.specification.dto.CreateSpecificationRequest;
import ai.zevaro.core.domain.specification.dto.SpecificationResponse;
import ai.zevaro.core.domain.specification.dto.UpdateSpecificationRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.domain.workstream.WorkstreamMode;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecificationService {

    private final SpecificationRepository specificationRepository;
    private final WorkstreamRepository workstreamRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final SpecificationMapper specificationMapper;
    private final AuditService auditService;

    private static final Map<SpecificationStatus, Set<SpecificationStatus>> VALID_TRANSITIONS = Map.of(
            SpecificationStatus.DRAFT, Set.of(SpecificationStatus.IN_REVIEW),
            SpecificationStatus.IN_REVIEW, Set.of(SpecificationStatus.APPROVED, SpecificationStatus.DRAFT),
            SpecificationStatus.APPROVED, Set.of(SpecificationStatus.IN_PROGRESS),
            SpecificationStatus.IN_PROGRESS, Set.of(SpecificationStatus.DELIVERED),
            SpecificationStatus.DELIVERED, Set.of(SpecificationStatus.ACCEPTED)
    );

    // --- CRUD ---

    @Transactional
    public SpecificationResponse create(UUID workstreamId, CreateSpecificationRequest req, UUID tenantId, UUID userId) {
        Workstream workstream = workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        if (workstream.getMode() != WorkstreamMode.BUILD) {
            throw new IllegalArgumentException("Specifications can only be created in BUILD mode Workstreams");
        }

        Specification spec = specificationMapper.toEntity(req, workstreamId, workstream.getProgramId(), tenantId, userId);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Created specification: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional(readOnly = true)
    public SpecificationResponse getById(UUID id, UUID tenantId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));
        return buildResponse(spec);
    }

    @Transactional(readOnly = true)
    public List<SpecificationResponse> listByWorkstream(UUID workstreamId, UUID tenantId) {
        workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        return specificationRepository.findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(tenantId, workstreamId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SpecificationResponse> listByWorkstreamPaged(UUID workstreamId, UUID tenantId, Pageable pageable) {
        workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        return specificationRepository.findByTenantIdAndWorkstreamId(tenantId, workstreamId, pageable)
                .map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public List<SpecificationResponse> listByProgram(UUID programId, UUID tenantId) {
        programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        return specificationRepository.findByTenantIdAndProgramId(tenantId, programId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional
    public SpecificationResponse update(UUID id, UpdateSpecificationRequest req, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        specificationMapper.applyUpdate(spec, req);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Updated specification: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        specificationRepository.delete(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Deleted specification: " + spec.getName()));
    }

    // --- Workflow ---

    @Transactional
    public SpecificationResponse submitForReview(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.IN_REVIEW);
        spec.setStatus(SpecificationStatus.IN_REVIEW);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Submitted specification for review: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public SpecificationResponse approve(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.APPROVED);
        spec.setStatus(SpecificationStatus.APPROVED);
        spec.setApprovedAt(Instant.now());
        spec.setApprovedById(userId);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Approved specification: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public SpecificationResponse reject(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.DRAFT);
        spec.setStatus(SpecificationStatus.DRAFT);
        spec.setApprovedAt(null);
        spec.setApprovedById(null);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Rejected specification: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public SpecificationResponse startWork(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.IN_PROGRESS);
        spec.setStatus(SpecificationStatus.IN_PROGRESS);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Started work on specification: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public SpecificationResponse markDelivered(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.DELIVERED);
        spec.setStatus(SpecificationStatus.DELIVERED);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Marked specification as delivered: " + spec.getName()));

        return buildResponse(spec);
    }

    @Transactional
    public SpecificationResponse markAccepted(UUID id, UUID tenantId, UUID userId) {
        Specification spec = specificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification", "id", id));

        validateTransition(spec.getStatus(), SpecificationStatus.ACCEPTED);
        spec.setStatus(SpecificationStatus.ACCEPTED);
        spec = specificationRepository.save(spec);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("SPECIFICATION", spec.getId(), spec.getName())
                .description("Accepted specification: " + spec.getName()));

        return buildResponse(spec);
    }

    // --- Helpers ---

    private void validateTransition(SpecificationStatus current, SpecificationStatus target) {
        if (!VALID_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new IllegalStateException("Cannot transition from " + current + " to " + target);
        }
    }

    private SpecificationResponse buildResponse(Specification spec) {
        String workstreamName = workstreamRepository.findByIdAndTenantId(spec.getWorkstreamId(), spec.getTenantId())
                .map(Workstream::getName)
                .orElse(null);
        String programName = programRepository.findByIdAndTenantId(spec.getProgramId(), spec.getTenantId())
                .map(Program::getName)
                .orElse(null);
        String authorName = resolveUserName(spec.getAuthorId(), spec.getTenantId());
        String reviewerName = resolveUserName(spec.getReviewerId(), spec.getTenantId());
        String approvedByName = resolveUserName(spec.getApprovedById(), spec.getTenantId());

        return specificationMapper.toResponse(spec, workstreamName, programName,
                authorName, reviewerName, approvedByName, 0);
    }

    private String resolveUserName(UUID userId, UUID tenantId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .map(User::getFullName)
                .orElse(null);
    }
}
