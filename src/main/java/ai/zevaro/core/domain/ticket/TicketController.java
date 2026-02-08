package ai.zevaro.core.domain.ticket;

import ai.zevaro.core.domain.ticket.dto.AssignTicketRequest;
import ai.zevaro.core.domain.ticket.dto.CreateTicketRequest;
import ai.zevaro.core.domain.ticket.dto.ResolveTicketRequest;
import ai.zevaro.core.domain.ticket.dto.TicketResponse;
import ai.zevaro.core.domain.ticket.dto.TriageTicketRequest;
import ai.zevaro.core.domain.ticket.dto.UpdateTicketRequest;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // --- Nested endpoints (under Workstreams) ---

    @PostMapping("/workstreams/{wsId}/tickets")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:create')")
    public ResponseEntity<TicketResponse> create(
            @PathVariable UUID wsId,
            @Valid @RequestBody CreateTicketRequest request,
            @CurrentUser UserPrincipal user) {
        TicketResponse response = ticketService.create(
                wsId, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/workstreams/{wsId}/tickets")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:read')")
    public ResponseEntity<List<TicketResponse>> listByWorkstream(
            @PathVariable UUID wsId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.listByWorkstream(wsId, user.getTenantId()));
    }

    @GetMapping("/workstreams/{wsId}/tickets/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:read')")
    public ResponseEntity<Page<TicketResponse>> listByWorkstreamPaged(
            @PathVariable UUID wsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(ticketService.listByWorkstreamPaged(
                wsId, user.getTenantId(), pageable));
    }

    // --- Flat endpoints (direct access) ---

    @GetMapping("/tickets/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:read')")
    public ResponseEntity<TicketResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.getById(id, user.getTenantId()));
    }

    @PutMapping("/tickets/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.update(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/tickets/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        ticketService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    // --- Workflow endpoints ---

    @PostMapping("/tickets/{id}/triage")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:assign')")
    public ResponseEntity<TicketResponse> triage(
            @PathVariable UUID id,
            @Valid @RequestBody TriageTicketRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.triage(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/assign")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:assign')")
    public ResponseEntity<TicketResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.assign(
                id, request.assignedToId(), user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/start-work")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> startWork(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.startWork(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/submit-review")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> submitForReview(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.submitForReview(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/resolve")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveTicketRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.resolve(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/close")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:close')")
    public ResponseEntity<TicketResponse> close(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.close(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/tickets/{id}/wont-fix")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('ticket:update')")
    public ResponseEntity<TicketResponse> wontFix(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ticketService.wontFix(
                id, user.getTenantId(), user.getUserId()));
    }
}
