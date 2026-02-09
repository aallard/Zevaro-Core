package ai.zevaro.core.domain.template;

import ai.zevaro.core.domain.template.dto.ApplyTemplateRequest;
import ai.zevaro.core.domain.template.dto.ApplyTemplateResponse;
import ai.zevaro.core.domain.template.dto.CreateTemplateRequest;
import ai.zevaro.core.domain.template.dto.TemplateResponse;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/program-templates")
@Tag(name = "Program Templates", description = "Program template management")
@RequiredArgsConstructor
public class ProgramTemplateController {

    private final ProgramTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('template:create')")
    public ResponseEntity<TemplateResponse> create(
            @Valid @RequestBody CreateTemplateRequest request,
            @CurrentUser UserPrincipal user) {
        TemplateResponse response = templateService.create(request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('template:read')")
    public ResponseEntity<List<TemplateResponse>> list(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(templateService.list(user.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('template:read')")
    public ResponseEntity<TemplateResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(templateService.getById(id, user.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('template:update')")
    public ResponseEntity<TemplateResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTemplateRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(templateService.update(id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('template:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        templateService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('program:create')")
    public ResponseEntity<ApplyTemplateResponse> applyTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyTemplateRequest request,
            @CurrentUser UserPrincipal user) {
        ApplyTemplateResponse response = templateService.applyTemplate(id, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
