package ai.zevaro.core.domain.ticket;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.ticket.dto.CreateTicketRequest;
import ai.zevaro.core.domain.ticket.dto.ResolveTicketRequest;
import ai.zevaro.core.domain.ticket.dto.TicketResponse;
import ai.zevaro.core.domain.ticket.dto.TriageTicketRequest;
import ai.zevaro.core.domain.ticket.dto.UpdateTicketRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.domain.workstream.WorkstreamMode;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import ai.zevaro.core.event.EventPublisher;
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
public class TicketService {

    private final TicketRepository ticketRepository;
    private final WorkstreamRepository workstreamRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.NEW, Set.of(TicketStatus.TRIAGED, TicketStatus.WONT_FIX),
            TicketStatus.TRIAGED, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.WONT_FIX),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.IN_REVIEW, TicketStatus.WONT_FIX),
            TicketStatus.IN_REVIEW, Set.of(TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED)
    );

    private static final Map<TicketType, String> TYPE_PREFIXES = Map.of(
            TicketType.BUG, "BUG-",
            TicketType.ENHANCEMENT, "ENH-",
            TicketType.MAINTENANCE, "MAINT-",
            TicketType.SECURITY, "SEC-",
            TicketType.TECH_DEBT, "DEBT-"
    );

    // --- CRUD ---

    @Transactional
    public TicketResponse create(UUID workstreamId, CreateTicketRequest req, UUID tenantId, UUID userId) {
        Workstream workstream = workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        if (workstream.getMode() != WorkstreamMode.OPS) {
            throw new IllegalArgumentException("Tickets can only be created in OPS mode Workstreams");
        }

        String identifier = generateIdentifier(tenantId, workstreamId, req.type());

        Ticket ticket = ticketMapper.toEntity(req, workstreamId, workstream.getProgramId(),
                tenantId, userId, identifier);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Created ticket: " + ticket.getIdentifier() + " - " + ticket.getTitle()));

        eventPublisher.publishTicketCreated(ticket, userId);

        return buildResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(UUID id, UUID tenantId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return buildResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listByWorkstream(UUID workstreamId, UUID tenantId) {
        workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        return ticketRepository.findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(tenantId, workstreamId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> listByWorkstreamPaged(UUID workstreamId, UUID tenantId, Pageable pageable) {
        workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));

        return ticketRepository.findByTenantIdAndWorkstreamId(tenantId, workstreamId, pageable)
                .map(this::buildResponse);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listByProgram(UUID programId, UUID tenantId) {
        programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        return ticketRepository.findByTenantIdAndProgramId(tenantId, programId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional
    public TicketResponse update(UUID id, UpdateTicketRequest req, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticketMapper.applyUpdate(ticket, req);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Updated ticket: " + ticket.getIdentifier()));

        return buildResponse(ticket);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticketRepository.delete(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Deleted ticket: " + ticket.getIdentifier()));
    }

    // --- Workflow ---

    @Transactional
    public TicketResponse triage(UUID id, TriageTicketRequest req, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.TRIAGED);
        ticket.setSeverity(req.severity());
        if (req.assignedToId() != null) {
            ticket.setAssignedToId(req.assignedToId());
        }
        ticket.setStatus(TicketStatus.TRIAGED);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Triaged ticket: " + ticket.getIdentifier() + " severity=" + req.severity()));

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse assign(UUID id, UUID assignedToId, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        if (ticket.getStatus() != TicketStatus.TRIAGED && ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only assign tickets in TRIAGED or IN_PROGRESS status");
        }

        ticket.setAssignedToId(assignedToId);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Assigned ticket: " + ticket.getIdentifier() + " to " + assignedToId));

        eventPublisher.publishTicketAssigned(ticket, userId);

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse startWork(UUID id, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.IN_PROGRESS);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Started work on ticket: " + ticket.getIdentifier()));

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse submitForReview(UUID id, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.IN_REVIEW);
        ticket.setStatus(TicketStatus.IN_REVIEW);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Submitted ticket for review: " + ticket.getIdentifier()));

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse resolve(UUID id, ResolveTicketRequest req, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.RESOLVED);
        ticket.setResolution(req.resolution());
        if (req.actualHours() != null) {
            ticket.setActualHours(req.actualHours());
        }
        ticket.setResolvedAt(Instant.now());
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Resolved ticket: " + ticket.getIdentifier() + " resolution=" + req.resolution()));

        eventPublisher.publishTicketResolved(ticket, userId);

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse close(UUID id, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        ticket.setStatus(TicketStatus.CLOSED);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Closed ticket: " + ticket.getIdentifier()));

        return buildResponse(ticket);
    }

    @Transactional
    public TicketResponse wontFix(UUID id, UUID tenantId, UUID userId) {
        Ticket ticket = ticketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        validateTransition(ticket.getStatus(), TicketStatus.WONT_FIX);
        ticket.setResolution(TicketResolution.WONT_FIX);
        ticket.setStatus(TicketStatus.WONT_FIX);
        ticket = ticketRepository.save(ticket);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("TICKET", ticket.getId(), ticket.getIdentifier())
                .description("Marked ticket as won't fix: " + ticket.getIdentifier()));

        return buildResponse(ticket);
    }

    // --- Helpers ---

    private void validateTransition(TicketStatus current, TicketStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new IllegalStateException("Cannot transition from " + current + " to " + target);
        }
    }

    private String generateIdentifier(UUID tenantId, UUID workstreamId, TicketType type) {
        String prefix = TYPE_PREFIXES.get(type);
        int nextNumber = ticketRepository.findMaxIdentifierNumber(tenantId, workstreamId, prefix)
                .map(max -> max + 1)
                .orElse(1);
        return prefix + String.format("%03d", nextNumber);
    }

    private TicketResponse buildResponse(Ticket ticket) {
        String workstreamName = workstreamRepository.findByIdAndTenantId(ticket.getWorkstreamId(), ticket.getTenantId())
                .map(Workstream::getName)
                .orElse(null);
        String programName = programRepository.findByIdAndTenantId(ticket.getProgramId(), ticket.getTenantId())
                .map(Program::getName)
                .orElse(null);
        String reportedByName = resolveUserName(ticket.getReportedById(), ticket.getTenantId());
        String assignedToName = resolveUserName(ticket.getAssignedToId(), ticket.getTenantId());

        return ticketMapper.toResponse(ticket, workstreamName, programName, reportedByName, assignedToName);
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
