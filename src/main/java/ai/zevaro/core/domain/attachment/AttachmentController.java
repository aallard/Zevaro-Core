package ai.zevaro.core.domain.attachment;

import ai.zevaro.core.domain.attachment.dto.AttachmentResponse;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@Tag(name = "Attachments", description = "File attachment management")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("parentType") AttachmentParentType parentType,
            @RequestParam("parentId") UUID parentId,
            @CurrentUser UserPrincipal user) {
        AttachmentResponse response = attachmentService.upload(file, parentType, parentId,
                user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> getAttachment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(attachmentService.getById(id, user.getTenantId()));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        AttachmentResponse metadata = attachmentService.getById(id, user.getTenantId());
        Resource resource = attachmentService.getFileById(id, user.getTenantId());

        String contentType = metadata.fileType() != null ? metadata.fileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.fileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/by-parent/{parentType}/{parentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentResponse>> listByParent(
            @PathVariable AttachmentParentType parentType,
            @PathVariable UUID parentId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(attachmentService.listByParent(parentType, parentId, user.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        attachmentService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
