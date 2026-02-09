package ai.zevaro.core.domain.activity;

import ai.zevaro.core.domain.activity.dto.ActivityEvent;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity")
@Tag(name = "Activity", description = "Activity feed")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('program:read')")
    public ResponseEntity<Page<ActivityEvent>> getActivity(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID workstreamId,
            @RequestParam(required = false) String entityType,
            Pageable pageable,
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(activityService.getActivity(
                principal.getTenantId(), programId, workstreamId, entityType, pageable));
    }
}
