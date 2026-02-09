package ai.zevaro.core.domain.search;

import ai.zevaro.core.domain.search.dto.SearchResult;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Cross-entity search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('program:read')")
    public ResponseEntity<List<SearchResult>> search(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(searchService.search(
                user.getTenantId(), q, type, programId, status, Math.min(limit, 100)));
    }
}
