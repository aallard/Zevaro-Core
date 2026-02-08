package ai.zevaro.core.domain.link;

import ai.zevaro.core.domain.link.dto.CreateEntityLinkRequest;
import ai.zevaro.core.domain.link.dto.EntityLinkResponse;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entity-links")
@RequiredArgsConstructor
public class EntityLinkController {

    private final EntityLinkService entityLinkService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntityLinkResponse> createLink(
            @Valid @RequestBody CreateEntityLinkRequest request,
            @CurrentUser UserPrincipal user) {
        EntityLinkResponse response = entityLinkService.create(request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/from/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntityLinkResponse>> getLinksFrom(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(entityLinkService.getLinksFrom(entityType, entityId, user.getTenantId()));
    }

    @GetMapping("/to/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntityLinkResponse>> getLinksTo(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(entityLinkService.getLinksTo(entityType, entityId, user.getTenantId()));
    }

    @GetMapping("/all/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntityLinkResponse>> getAllLinks(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(entityLinkService.getAllLinks(entityType, entityId, user.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteLink(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        entityLinkService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
