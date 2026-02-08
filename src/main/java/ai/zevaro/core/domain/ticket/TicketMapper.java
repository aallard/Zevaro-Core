package ai.zevaro.core.domain.ticket;

import ai.zevaro.core.domain.ticket.dto.CreateTicketRequest;
import ai.zevaro.core.domain.ticket.dto.TicketResponse;
import ai.zevaro.core.domain.ticket.dto.UpdateTicketRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TicketMapper {

    public Ticket toEntity(CreateTicketRequest req, UUID workstreamId, UUID programId,
                           UUID tenantId, UUID createdById, String identifier) {
        Ticket t = new Ticket();
        t.setTenantId(tenantId);
        t.setWorkstreamId(workstreamId);
        t.setProgramId(programId);
        t.setIdentifier(identifier);
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setType(req.type());
        t.setSeverity(req.severity());
        t.setStatus(TicketStatus.NEW);
        t.setReportedById(createdById);
        t.setAssignedToId(req.assignedToId());
        t.setEnvironment(req.environment());
        t.setStepsToReproduce(req.stepsToReproduce());
        t.setExpectedBehavior(req.expectedBehavior());
        t.setActualBehavior(req.actualBehavior());
        t.setSource(req.source() != null ? req.source() : TicketSource.MANUAL);
        t.setExternalRef(req.externalRef());
        t.setEstimatedHours(req.estimatedHours());
        t.setCreatedById(createdById);
        return t;
    }

    public TicketResponse toResponse(Ticket ticket, String workstreamName, String programName,
                                     String reportedByName, String assignedToName) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getWorkstreamId(),
                workstreamName,
                ticket.getProgramId(),
                programName,
                ticket.getIdentifier(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getType(),
                ticket.getSeverity(),
                ticket.getStatus(),
                ticket.getResolution(),
                ticket.getReportedById(),
                reportedByName,
                ticket.getAssignedToId(),
                assignedToName,
                ticket.getEnvironment(),
                ticket.getStepsToReproduce(),
                ticket.getExpectedBehavior(),
                ticket.getActualBehavior(),
                ticket.getSource(),
                ticket.getExternalRef(),
                ticket.getEstimatedHours(),
                ticket.getActualHours(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public void applyUpdate(Ticket existing, UpdateTicketRequest req) {
        if (req.title() != null) {
            existing.setTitle(req.title());
        }
        if (req.description() != null) {
            existing.setDescription(req.description());
        }
        if (req.type() != null) {
            existing.setType(req.type());
        }
        if (req.severity() != null) {
            existing.setSeverity(req.severity());
        }
        if (req.assignedToId() != null) {
            existing.setAssignedToId(req.assignedToId());
        }
        if (req.environment() != null) {
            existing.setEnvironment(req.environment());
        }
        if (req.stepsToReproduce() != null) {
            existing.setStepsToReproduce(req.stepsToReproduce());
        }
        if (req.expectedBehavior() != null) {
            existing.setExpectedBehavior(req.expectedBehavior());
        }
        if (req.actualBehavior() != null) {
            existing.setActualBehavior(req.actualBehavior());
        }
        if (req.externalRef() != null) {
            existing.setExternalRef(req.externalRef());
        }
        if (req.estimatedHours() != null) {
            existing.setEstimatedHours(req.estimatedHours());
        }
        if (req.actualHours() != null) {
            existing.setActualHours(req.actualHours());
        }
    }
}
