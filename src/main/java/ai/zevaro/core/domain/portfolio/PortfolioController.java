package ai.zevaro.core.domain.portfolio;

import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.portfolio.dto.CreatePortfolioRequest;
import ai.zevaro.core.domain.portfolio.dto.PortfolioDashboardResponse;
import ai.zevaro.core.domain.portfolio.dto.PortfolioResponse;
import ai.zevaro.core.domain.portfolio.dto.UpdatePortfolioRequest;
import ai.zevaro.core.domain.project.Project;
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
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:create')")
    public ResponseEntity<PortfolioResponse> create(
            @Valid @RequestBody CreatePortfolioRequest request,
            @CurrentUser UserPrincipal user) {
        PortfolioResponse response = portfolioService.create(user.getTenantId(), request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<List<PortfolioResponse>> list(
            @RequestParam(required = false) PortfolioStatus status,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.list(user.getTenantId(), status));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<Page<PortfolioResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(portfolioService.listPaged(user.getTenantId(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<PortfolioResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.getById(id, user.getTenantId()));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<PortfolioResponse> getBySlug(
            @PathVariable String slug,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.getBySlug(slug, user.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:update')")
    public ResponseEntity<PortfolioResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePortfolioRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.update(id, user.getTenantId(), request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        portfolioService.delete(id, user.getTenantId(), user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/programs")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<List<Project>> getPrograms(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.getPrograms(id, user.getTenantId()));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<PortfolioDashboardResponse> getDashboard(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.getDashboard(id, user.getTenantId()));
    }

    @GetMapping("/{id}/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('portfolio:read')")
    public ResponseEntity<List<Decision>> getDecisions(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(portfolioService.getDecisions(id, user.getTenantId()));
    }
}
