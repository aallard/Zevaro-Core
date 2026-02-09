package ai.zevaro.core.domain.document;

import ai.zevaro.core.domain.document.dto.CreateDocumentRequest;
import ai.zevaro.core.domain.document.dto.DocumentResponse;
import ai.zevaro.core.domain.document.dto.DocumentTreeNode;
import ai.zevaro.core.domain.document.dto.DocumentVersionResponse;
import ai.zevaro.core.domain.document.dto.UpdateDocumentRequest;
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
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document management")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:create')")
    public ResponseEntity<DocumentResponse> create(
            @Valid @RequestBody CreateDocumentRequest request,
            @CurrentUser UserPrincipal user) {
        DocumentResponse response = documentService.create(request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<DocumentResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.getById(id, user.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:update')")
    public ResponseEntity<DocumentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDocumentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.update(id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        documentService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:publish')")
    public ResponseEntity<DocumentResponse> publish(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.publish(id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:update')")
    public ResponseEntity<DocumentResponse> archive(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.archive(id, user.getTenantId(), user.getUserId()));
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<List<DocumentVersionResponse>> getVersions(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.getVersions(id, user.getTenantId()));
    }

    @GetMapping("/{id}/versions/{version}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<DocumentVersionResponse> getVersion(
            @PathVariable UUID id,
            @PathVariable int version,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.getVersion(id, version, user.getTenantId()));
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<List<DocumentResponse>> listChildren(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.listChildren(id, user.getTenantId()));
    }

    @GetMapping("/by-space/{spaceId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<List<DocumentResponse>> listBySpace(
            @PathVariable UUID spaceId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.listBySpace(spaceId, user.getTenantId()));
    }

    @GetMapping("/by-space/{spaceId}/roots")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<List<DocumentResponse>> listRootsBySpace(
            @PathVariable UUID spaceId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.listRootsBySpace(spaceId, user.getTenantId()));
    }

    @GetMapping("/by-space/{spaceId}/tree")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('document:read')")
    public ResponseEntity<List<DocumentTreeNode>> getTree(
            @PathVariable UUID spaceId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(documentService.getTree(spaceId, user.getTenantId()));
    }
}
