package ai.zevaro.core.domain.attachment;

import ai.zevaro.core.domain.attachment.dto.AttachmentResponse;
import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Value("${zevaro.attachments.storage-path:./attachments}")
    private String storagePath;

    @Transactional
    public AttachmentResponse upload(MultipartFile file, AttachmentParentType parentType, UUID parentId,
                                     UUID tenantId, UUID userId) {
        try {
            Path dir = Paths.get(storagePath, tenantId.toString(), parentType.name(), parentId.toString());
            Files.createDirectories(dir);

            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            Attachment attachment = new Attachment();
            attachment.setTenantId(tenantId);
            attachment.setParentType(parentType);
            attachment.setParentId(parentId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setStorageUrl(filePath.toAbsolutePath().toString());
            attachment.setUploadedById(userId);

            attachment = attachmentRepository.save(attachment);

            auditService.log(AuditLogBuilder.create()
                    .tenant(tenantId)
                    .actor(userId, null, null)
                    .action(AuditAction.CREATE)
                    .entity("Attachment", attachment.getId(), file.getOriginalFilename())
                    .description("File uploaded to " + parentType + " " + parentId));

            return toResponse(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AttachmentResponse getById(UUID id, UUID tenantId) {
        Attachment attachment = attachmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
        return toResponse(attachment);
    }

    @Transactional(readOnly = true)
    public Resource getFileById(UUID id, UUID tenantId) {
        Attachment attachment = attachmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));

        Path filePath = Paths.get(attachment.getStorageUrl());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Attachment file", "path", attachment.getStorageUrl());
        }

        return new FileSystemResource(filePath);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listByParent(AttachmentParentType parentType, UUID parentId, UUID tenantId) {
        return attachmentRepository.findByTenantIdAndParentTypeAndParentIdOrderByCreatedAtDesc(tenantId, parentType, parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Attachment attachment = attachmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));

        try {
            Path filePath = Paths.get(attachment.getStorageUrl());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", attachment.getStorageUrl(), e);
        }

        attachmentRepository.delete(attachment);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("Attachment", id, attachment.getFileName())
                .description("Attachment deleted"));
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        String uploadedByName = userRepository.findById(attachment.getUploadedById())
                .map(User::getFullName)
                .orElse(null);
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getParentType(),
                attachment.getParentId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getUploadedById(),
                uploadedByName,
                attachment.getCreatedAt()
        );
    }
}
