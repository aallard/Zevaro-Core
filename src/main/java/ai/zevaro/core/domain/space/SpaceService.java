package ai.zevaro.core.domain.space;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.space.dto.CreateSpaceRequest;
import ai.zevaro.core.domain.space.dto.SpaceResponse;
import ai.zevaro.core.domain.space.dto.UpdateSpaceRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import ai.zevaro.core.security.UserPrincipal;
import ai.zevaro.core.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final SpaceMapper spaceMapper;
    private final SlugGenerator slugGenerator;
    private final AuditService auditService;

    @Transactional
    public SpaceResponse create(CreateSpaceRequest request, UUID tenantId, UserPrincipal currentUser) {
        UUID userId = currentUser.getUserId();

        if (request.type() == SpaceType.PROGRAM) {
            if (request.programId() == null) {
                throw new IllegalArgumentException("programId is required for PROGRAM type spaces");
            }
            programRepository.findByIdAndTenantId(request.programId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.programId()));
            if (spaceRepository.findByTenantIdAndProgramId(tenantId, request.programId()).isPresent()) {
                throw new IllegalArgumentException("A space already exists for this program");
            }
        }

        if (spaceRepository.existsByTenantIdAndName(tenantId, request.name())) {
            throw new IllegalArgumentException("A space with this name already exists");
        }

        String uniqueSlug = slugGenerator.generateUniqueSlug(
                request.name(),
                slug -> spaceRepository.existsByTenantIdAndSlug(tenantId, slug)
        );

        Space space = spaceMapper.toEntity(request, tenantId, userId, uniqueSlug);
        space = spaceRepository.save(space);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.CREATE)
                .entity("SPACE", space.getId(), space.getName())
                .description("Created space: " + space.getName()));

        return toResponse(space);
    }

    @Transactional
    public SpaceResponse createForProgram(UUID programId, String programName, UUID tenantId, UUID ownerId) {
        String spaceName = programName;

        // If the name already exists, append " Docs"
        if (spaceRepository.existsByTenantIdAndName(tenantId, spaceName)) {
            spaceName = programName + " Docs";
        }

        String uniqueSlug = slugGenerator.generateUniqueSlug(
                spaceName,
                slug -> spaceRepository.existsByTenantIdAndSlug(tenantId, slug)
        );

        Space space = new Space();
        space.setTenantId(tenantId);
        space.setName(spaceName);
        space.setSlug(uniqueSlug);
        space.setType(SpaceType.PROGRAM);
        space.setStatus(SpaceStatus.ACTIVE);
        space.setProgramId(programId);
        space.setOwnerId(ownerId);
        space.setIcon("\uD83D\uDCC1");
        space.setVisibility(SpaceVisibility.PUBLIC);
        space.setSortOrder(0);
        space.setCreatedById(ownerId);

        space = spaceRepository.save(space);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(ownerId, null, null)
                .action(AuditAction.CREATE)
                .entity("SPACE", space.getId(), space.getName())
                .description("Auto-created space for program: " + programName));

        return toResponse(space);
    }

    @Transactional(readOnly = true)
    public SpaceResponse getById(UUID id, UUID tenantId) {
        Space space = spaceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", id));
        return toResponse(space);
    }

    @Transactional(readOnly = true)
    public SpaceResponse getBySlug(String slug, UUID tenantId) {
        Space space = spaceRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "slug", slug));
        return toResponse(space);
    }

    @Transactional(readOnly = true)
    public SpaceResponse getByProgramId(UUID programId, UUID tenantId) {
        Space space = spaceRepository.findByTenantIdAndProgramId(tenantId, programId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "programId", programId));
        return toResponse(space);
    }

    @Transactional(readOnly = true)
    public List<SpaceResponse> list(UUID tenantId) {
        return spaceRepository.findByTenantIdAndStatusOrderBySortOrderAsc(tenantId, SpaceStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceResponse> listByType(UUID tenantId, SpaceType type) {
        return spaceRepository.findByTenantIdAndTypeAndStatus(tenantId, type, SpaceStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SpaceResponse> listPaged(UUID tenantId, Pageable pageable) {
        return spaceRepository.findByTenantId(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SpaceResponse update(UUID id, UpdateSpaceRequest request, UUID tenantId, UserPrincipal currentUser) {
        Space space = spaceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", id));

        if (request.name() != null && !request.name().equals(space.getName())) {
            if (spaceRepository.existsByTenantIdAndName(tenantId, request.name())) {
                throw new IllegalArgumentException("A space with this name already exists");
            }
            String newSlug = slugGenerator.generateUniqueSlug(
                    request.name(),
                    slug -> spaceRepository.existsByTenantIdAndSlug(tenantId, slug)
            );
            space.setSlug(newSlug);
        }

        spaceMapper.applyUpdate(space, request);
        space = spaceRepository.save(space);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.UPDATE)
                .entity("SPACE", space.getId(), space.getName())
                .description("Updated space: " + space.getName()));

        return toResponse(space);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UserPrincipal currentUser) {
        Space space = spaceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", id));

        space.setStatus(SpaceStatus.ARCHIVED);
        spaceRepository.save(space);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.DELETE)
                .entity("SPACE", space.getId(), space.getName())
                .description("Archived space: " + space.getName()));
    }

    private SpaceResponse toResponse(Space space) {
        String programName = resolveProgramName(space.getProgramId(), space.getTenantId());
        String ownerName = resolveOwnerName(space.getOwnerId(), space.getTenantId());
        int documentCount = 0; // Wired in ZC-061
        return spaceMapper.toResponse(space, programName, ownerName, documentCount);
    }

    private String resolveProgramName(UUID programId, UUID tenantId) {
        if (programId == null) {
            return null;
        }
        return programRepository.findByIdAndTenantId(programId, tenantId)
                .map(Program::getName)
                .orElse(null);
    }

    private String resolveOwnerName(UUID ownerId, UUID tenantId) {
        if (ownerId == null) {
            return null;
        }
        return userRepository.findByIdAndTenantId(ownerId, tenantId)
                .map(User::getFullName)
                .orElse(null);
    }
}
