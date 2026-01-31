package ai.zevaro.core.domain.queue;

import ai.zevaro.core.domain.queue.dto.CreateQueueRequest;
import ai.zevaro.core.domain.queue.dto.QueueResponse;
import ai.zevaro.core.domain.queue.dto.UpdateQueueRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class DecisionQueueController {

    private final DecisionQueueService queueService;

    @GetMapping
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'READ')")
    public ResponseEntity<List<QueueResponse>> getQueues(@CurrentUser UserPrincipal principal) {
        List<QueueResponse> queues = queueService.getQueues(principal.getTenantId());
        return ResponseEntity.ok(queues);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'READ')")
    public ResponseEntity<QueueResponse> getQueueById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        QueueResponse queue = queueService.getQueueById(id, principal.getTenantId());
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/default")
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'READ')")
    public ResponseEntity<QueueResponse> getDefaultQueue(@CurrentUser UserPrincipal principal) {
        QueueResponse queue = queueService.getDefaultQueue(principal.getTenantId());
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(queue);
    }

    @PostMapping
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'CREATE')")
    public ResponseEntity<QueueResponse> createQueue(
            @Valid @RequestBody CreateQueueRequest request,
            @CurrentUser UserPrincipal principal) {
        QueueResponse queue = queueService.createQueue(principal.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(queue);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'UPDATE')")
    public ResponseEntity<QueueResponse> updateQueue(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQueueRequest request,
            @CurrentUser UserPrincipal principal) {
        QueueResponse queue = queueService.updateQueue(id, principal.getTenantId(), request);
        return ResponseEntity.ok(queue);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'DELETE')")
    public ResponseEntity<Void> deleteQueue(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        queueService.deleteQueue(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasPermission(#principal.tenantId, 'QUEUE', 'UPDATE')")
    public ResponseEntity<QueueResponse> setDefaultQueue(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        QueueResponse queue = queueService.setDefaultQueue(id, principal.getTenantId());
        return ResponseEntity.ok(queue);
    }
}
