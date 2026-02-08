package ai.zevaro.core.domain.workstream;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.dto.CreateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.UpdateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.WorkstreamResponse;
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
public class WorkstreamService {

    private final WorkstreamRepository workstreamRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final WorkstreamMapper workstreamMapper;
    private final AuditService auditService;

    @Transactional
    public WorkstreamResponse create(UUID programId, CreateWorkstreamRequest request, UUID tenantId, UUID userId) {
        Program program = programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        if (workstreamRepository.existsByTenantIdAndProgramIdAndName(tenantId, programId, request.name())) {
            throw new IllegalArgumentException("Workstream with name already exists in this program: " + request.name());
        }

        Workstream workstream = workstreamMapper.toEntity(request, programId, tenantId, userId);
        workstream = workstreamRepository.save(workstream);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("WORKSTREAM", workstream.getId(), workstream.getName())
                .description("Created workstream: " + workstream.getName()));

        String ownerName = resolveOwnerName(workstream.getOwnerId(), tenantId);
        return workstreamMapper.toResponse(workstream, program.getName(), ownerName, 0);
    }

    @Transactional(readOnly = true)
    public WorkstreamResponse getById(UUID id, UUID tenantId) {
        Workstream workstream = workstreamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", id));

        String programName = programRepository.findByIdAndTenantId(workstream.getProgramId(), tenantId)
                .map(Program::getName)
                .orElse(null);
        String ownerName = resolveOwnerName(workstream.getOwnerId(), tenantId);

        return workstreamMapper.toResponse(workstream, programName, ownerName, 0);
    }

    @Transactional(readOnly = true)
    public List<WorkstreamResponse> listByProgram(UUID programId, UUID tenantId) {
        programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        List<Workstream> workstreams = workstreamRepository.findByTenantIdAndProgramIdOrderBySortOrderAsc(tenantId, programId);

        return workstreams.stream()
                .map(ws -> {
                    String programName = programRepository.findByIdAndTenantId(ws.getProgramId(), tenantId)
                            .map(Program::getName)
                            .orElse(null);
                    String ownerName = resolveOwnerName(ws.getOwnerId(), tenantId);
                    return workstreamMapper.toResponse(ws, programName, ownerName, 0);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<WorkstreamResponse> listByProgramPaged(UUID programId, UUID tenantId, Pageable pageable) {
        programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        Page<Workstream> page = workstreamRepository.findByTenantIdAndProgramId(tenantId, programId, pageable);

        return page.map(ws -> {
            String programName = programRepository.findByIdAndTenantId(ws.getProgramId(), tenantId)
                    .map(Program::getName)
                    .orElse(null);
            String ownerName = resolveOwnerName(ws.getOwnerId(), tenantId);
            return workstreamMapper.toResponse(ws, programName, ownerName, 0);
        });
    }

    @Transactional
    public WorkstreamResponse update(UUID id, UpdateWorkstreamRequest request, UUID tenantId, UUID userId) {
        Workstream workstream = workstreamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", id));

        if (request.name() != null && !request.name().equals(workstream.getName())) {
            if (workstreamRepository.existsByTenantIdAndProgramIdAndName(tenantId, workstream.getProgramId(), request.name())) {
                throw new IllegalArgumentException("Workstream with name already exists in this program: " + request.name());
            }
        }

        workstreamMapper.applyUpdate(workstream, request);
        workstream = workstreamRepository.save(workstream);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("WORKSTREAM", workstream.getId(), workstream.getName())
                .description("Updated workstream: " + workstream.getName()));

        String programName = programRepository.findByIdAndTenantId(workstream.getProgramId(), tenantId)
                .map(Program::getName)
                .orElse(null);
        String ownerName = resolveOwnerName(workstream.getOwnerId(), tenantId);
        return workstreamMapper.toResponse(workstream, programName, ownerName, 0);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Workstream workstream = workstreamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", id));

        workstream.setStatus(WorkstreamStatus.CANCELLED);
        workstreamRepository.save(workstream);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("WORKSTREAM", workstream.getId(), workstream.getName())
                .description("Cancelled workstream: " + workstream.getName()));
    }

    @Transactional(readOnly = true)
    public List<WorkstreamResponse> listByProgramAndMode(UUID programId, UUID tenantId, WorkstreamMode mode) {
        programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        List<Workstream> workstreams = workstreamRepository.findByTenantIdAndProgramIdAndMode(tenantId, programId, mode);

        return workstreams.stream()
                .map(ws -> {
                    String programName = programRepository.findByIdAndTenantId(ws.getProgramId(), tenantId)
                            .map(Program::getName)
                            .orElse(null);
                    String ownerName = resolveOwnerName(ws.getOwnerId(), tenantId);
                    return workstreamMapper.toResponse(ws, programName, ownerName, 0);
                })
                .toList();
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
