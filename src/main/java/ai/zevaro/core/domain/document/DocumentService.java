package ai.zevaro.core.domain.document;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.document.dto.CreateDocumentRequest;
import ai.zevaro.core.domain.document.dto.DocumentResponse;
import ai.zevaro.core.domain.document.dto.DocumentTreeNode;
import ai.zevaro.core.domain.document.dto.DocumentVersionResponse;
import ai.zevaro.core.domain.document.dto.UpdateDocumentRequest;
import ai.zevaro.core.domain.space.Space;
import ai.zevaro.core.domain.space.SpaceRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.event.EventPublisher;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    // --- CRUD ---

    @Transactional
    public DocumentResponse create(CreateDocumentRequest req, UUID tenantId, UUID userId) {
        spaceRepository.findByIdAndTenantId(req.spaceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", req.spaceId()));

        if (req.parentDocumentId() != null) {
            Document parent = documentRepository.findByIdAndTenantId(req.parentDocumentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document", "id", req.parentDocumentId()));
            if (!parent.getSpaceId().equals(req.spaceId())) {
                throw new IllegalArgumentException("Parent document must be in the same space");
            }
        }

        Document doc = documentMapper.toEntity(req, tenantId, userId);
        doc = documentRepository.save(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Created document: " + doc.getTitle()));

        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(UUID id, UUID tenantId) {
        Document doc = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listBySpace(UUID spaceId, UUID tenantId) {
        spaceRepository.findByIdAndTenantId(spaceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", spaceId));

        return documentRepository.findByTenantIdAndSpaceIdOrderBySortOrderAsc(tenantId, spaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listRootsBySpace(UUID spaceId, UUID tenantId) {
        spaceRepository.findByIdAndTenantId(spaceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", spaceId));

        return documentRepository.findByTenantIdAndSpaceIdAndParentDocumentIdIsNullOrderBySortOrderAsc(tenantId, spaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listChildren(UUID parentDocumentId, UUID tenantId) {
        documentRepository.findByIdAndTenantId(parentDocumentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", parentDocumentId));

        return documentRepository.findByTenantIdAndParentDocumentIdOrderBySortOrderAsc(tenantId, parentDocumentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentTreeNode> getTree(UUID spaceId, UUID tenantId) {
        spaceRepository.findByIdAndTenantId(spaceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Space", "id", spaceId));

        List<Document> allDocs = documentRepository.findByTenantIdAndSpaceIdOrderBySortOrderAsc(tenantId, spaceId);

        // Group by parentDocumentId
        Map<UUID, List<Document>> childrenMap = new HashMap<>();
        List<Document> roots = new ArrayList<>();

        for (Document doc : allDocs) {
            if (doc.getParentDocumentId() == null) {
                roots.add(doc);
            } else {
                childrenMap.computeIfAbsent(doc.getParentDocumentId(), k -> new ArrayList<>()).add(doc);
            }
        }

        return roots.stream()
                .map(root -> buildTreeNode(root, childrenMap))
                .toList();
    }

    @Transactional
    public DocumentResponse update(UUID id, UpdateDocumentRequest req, UUID tenantId, UUID userId) {
        Document doc = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        documentMapper.applyUpdate(doc, req, userId);
        doc = documentRepository.save(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Updated document: " + doc.getTitle()));

        return toResponse(doc);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Document doc = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        long childCount = documentRepository.countByTenantIdAndParentDocumentId(tenantId, id);
        if (childCount > 0) {
            throw new IllegalStateException("Cannot delete document with " + childCount + " child document(s). Delete children first.");
        }

        documentVersionRepository.deleteByDocumentId(doc.getId());
        documentRepository.delete(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Deleted document: " + doc.getTitle()));
    }

    // --- Publish / Version ---

    @Transactional
    public DocumentResponse publish(UUID id, UUID tenantId, UUID userId) {
        Document doc = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Create a version snapshot of the current state
        DocumentVersion snapshot = new DocumentVersion();
        snapshot.setDocumentId(doc.getId());
        snapshot.setVersion(doc.getVersion());
        snapshot.setTitle(doc.getTitle());
        snapshot.setBody(doc.getBody());
        snapshot.setEditedById(userId);
        documentVersionRepository.save(snapshot);

        // Increment version, set published
        doc.setVersion(doc.getVersion() + 1);
        doc.setStatus(DocumentStatus.PUBLISHED);
        doc.setPublishedAt(Instant.now());
        doc.setLastEditedById(userId);
        doc = documentRepository.save(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Published document: " + doc.getTitle() + " (version " + (doc.getVersion() - 1) + ")"));

        eventPublisher.publishDocumentPublished(doc, userId);

        return toResponse(doc);
    }

    @Transactional
    public DocumentResponse archive(UUID id, UUID tenantId, UUID userId) {
        Document doc = documentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        doc.setStatus(DocumentStatus.ARCHIVED);
        doc = documentRepository.save(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Archived document: " + doc.getTitle()));

        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getVersions(UUID documentId, UUID tenantId) {
        documentRepository.findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        return documentVersionRepository.findByDocumentIdOrderByVersionDesc(documentId)
                .stream()
                .map(ver -> {
                    String editedByName = resolveUserName(ver.getEditedById(), tenantId);
                    return documentMapper.toVersionResponse(ver, editedByName);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentVersionResponse getVersion(UUID documentId, int version, UUID tenantId) {
        documentRepository.findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        DocumentVersion ver = documentVersionRepository.findByDocumentIdAndVersion(documentId, version)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentVersion", "version", version));

        String editedByName = resolveUserName(ver.getEditedById(), tenantId);
        return documentMapper.toVersionResponse(ver, editedByName);
    }

    // --- Internal: Specification bridge ---

    @Transactional
    public Document createForSpecification(UUID spaceId, String specificationName, UUID tenantId, UUID authorId) {
        Document doc = new Document();
        doc.setTenantId(tenantId);
        doc.setSpaceId(spaceId);
        doc.setTitle(specificationName);
        doc.setBody("# " + specificationName + "\n\n_Write your specification here._");
        doc.setType(DocumentType.SPECIFICATION);
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setVersion(1);
        doc.setAuthorId(authorId);
        doc.setLastEditedById(authorId);
        doc.setSortOrder(0);

        doc = documentRepository.save(doc);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(authorId, null, null)
                .action(AuditAction.CREATE)
                .entity("DOCUMENT", doc.getId(), doc.getTitle())
                .description("Auto-created document for specification: " + specificationName));

        return doc;
    }

    // --- Helpers ---

    private DocumentResponse toResponse(Document doc) {
        String spaceName = resolveSpaceName(doc.getSpaceId(), doc.getTenantId());
        String parentDocTitle = resolveDocumentTitle(doc.getParentDocumentId(), doc.getTenantId());
        String authorName = resolveUserName(doc.getAuthorId(), doc.getTenantId());
        String lastEditedByName = resolveUserName(doc.getLastEditedById(), doc.getTenantId());
        int childCount = (int) documentRepository.countByTenantIdAndParentDocumentId(doc.getTenantId(), doc.getId());
        return documentMapper.toResponse(doc, spaceName, parentDocTitle, authorName, lastEditedByName, childCount);
    }

    private DocumentTreeNode buildTreeNode(Document doc, Map<UUID, List<Document>> childrenMap) {
        List<Document> children = childrenMap.getOrDefault(doc.getId(), List.of());
        List<DocumentTreeNode> childNodes = children.stream()
                .map(child -> buildTreeNode(child, childrenMap))
                .toList();
        return documentMapper.toTreeNode(doc, childNodes);
    }

    private String resolveSpaceName(UUID spaceId, UUID tenantId) {
        if (spaceId == null) {
            return null;
        }
        return spaceRepository.findByIdAndTenantId(spaceId, tenantId)
                .map(Space::getName)
                .orElse(null);
    }

    private String resolveDocumentTitle(UUID documentId, UUID tenantId) {
        if (documentId == null) {
            return null;
        }
        return documentRepository.findByIdAndTenantId(documentId, tenantId)
                .map(Document::getTitle)
                .orElse(null);
    }

    private String resolveUserName(UUID userId, UUID tenantId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .map(User::getFullName)
                .orElse(null);
    }
}
