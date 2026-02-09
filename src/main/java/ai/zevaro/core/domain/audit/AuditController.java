package ai.zevaro.core.domain.audit;

import ai.zevaro.core.domain.audit.dto.AuditLogFilter;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Audit trail")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('system:audit_read')")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) AuditAction action,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @CurrentUser UserPrincipal user) {

        AuditLogFilter filter = new AuditLogFilter(actorId, entityType, entityId, action, null, null);
        return ResponseEntity.ok(auditService.getAuditLogs(user.getTenantId(), filter, pageable));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('system:audit_read')")
    public ResponseEntity<Page<AuditLog>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @CurrentUser UserPrincipal user) {

        AuditLogFilter filter = new AuditLogFilter(null, entityType, entityId, null, null, null);
        return ResponseEntity.ok(auditService.getAuditLogs(user.getTenantId(), filter, pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('system:audit_read')")
    public ResponseEntity<Map<AuditAction, Long>> getActionStats(
            @RequestParam(defaultValue = "30") int days,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(auditService.getActionCounts(user.getTenantId(), days));
    }
}
