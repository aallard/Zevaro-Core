package ai.zevaro.core.domain.document;

import ai.zevaro.core.domain.document.dto.CreateDocumentRequest;
import ai.zevaro.core.domain.document.dto.DocumentResponse;
import ai.zevaro.core.domain.document.dto.DocumentTreeNode;
import ai.zevaro.core.domain.document.dto.DocumentVersionResponse;
import ai.zevaro.core.domain.document.dto.UpdateDocumentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentMapper {

    private final ObjectMapper objectMapper;

    public Document toEntity(CreateDocumentRequest req, UUID tenantId, UUID authorId) {
        Document doc = new Document();
        doc.setTenantId(tenantId);
        doc.setSpaceId(req.spaceId());
        doc.setParentDocumentId(req.parentDocumentId());
        doc.setTitle(req.title());
        doc.setBody(req.body());
        doc.setType(req.type());
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setVersion(1);
        doc.setAuthorId(authorId);
        doc.setLastEditedById(authorId);
        doc.setTags(listToJson(req.tags()));
        doc.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        return doc;
    }

    public DocumentResponse toResponse(Document doc, String spaceName, String parentDocTitle,
                                        String authorName, String lastEditedByName, int childCount) {
        if (doc == null) {
            return null;
        }

        return new DocumentResponse(
                doc.getId(),
                doc.getSpaceId(),
                spaceName,
                doc.getParentDocumentId(),
                parentDocTitle,
                doc.getTitle(),
                doc.getBody(),
                doc.getType(),
                doc.getStatus(),
                doc.getVersion(),
                doc.getAuthorId(),
                authorName,
                doc.getLastEditedById(),
                lastEditedByName,
                doc.getPublishedAt(),
                parseJsonToList(doc.getTags()),
                doc.getSortOrder(),
                childCount,
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    public void applyUpdate(Document existing, UpdateDocumentRequest req, UUID editorId) {
        if (req.title() != null) {
            existing.setTitle(req.title());
        }
        if (req.body() != null) {
            existing.setBody(req.body());
        }
        if (req.tags() != null) {
            existing.setTags(listToJson(req.tags()));
        }
        if (req.sortOrder() != null) {
            existing.setSortOrder(req.sortOrder());
        }
        existing.setLastEditedById(editorId);
    }

    public DocumentVersionResponse toVersionResponse(DocumentVersion ver, String editedByName) {
        if (ver == null) {
            return null;
        }

        return new DocumentVersionResponse(
                ver.getId(),
                ver.getDocumentId(),
                ver.getVersion(),
                ver.getTitle(),
                ver.getBody(),
                ver.getEditedById(),
                editedByName,
                ver.getCreatedAt()
        );
    }

    public DocumentTreeNode toTreeNode(Document doc, List<DocumentTreeNode> children) {
        return new DocumentTreeNode(
                doc.getId(),
                doc.getTitle(),
                doc.getType(),
                doc.getStatus(),
                doc.getSortOrder(),
                children
        );
    }

    private String listToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert list to JSON", e);
            return null;
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to list: {}", json, e);
            return null;
        }
    }
}
