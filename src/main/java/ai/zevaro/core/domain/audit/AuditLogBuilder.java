package ai.zevaro.core.domain.audit;

import ai.zevaro.core.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class AuditLogBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private UUID tenantId;
    private UUID actorId;
    private String actorEmail;
    private String actorName;
    private AuditAction action;
    private String entityType;
    private UUID entityId;
    private String entityTitle;
    private String description;
    private Object oldValues;
    private Object newValues;
    private String ipAddress;
    private String userAgent;
    private String requestId;

    public static AuditLogBuilder create() {
        return new AuditLogBuilder();
    }

    public AuditLogBuilder tenant(UUID tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public AuditLogBuilder actor(UserPrincipal user) {
        if (user != null) {
            this.actorId = user.getUserId();
            this.actorEmail = user.getEmail();
            this.actorName = null;
        }
        return this;
    }

    public AuditLogBuilder actor(UUID actorId, String email, String name) {
        this.actorId = actorId;
        this.actorEmail = email;
        this.actorName = name;
        return this;
    }

    public AuditLogBuilder action(AuditAction action) {
        this.action = action;
        return this;
    }

    public AuditLogBuilder entity(String entityType, UUID entityId, String entityTitle) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityTitle = entityTitle;
        return this;
    }

    public AuditLogBuilder description(String description) {
        this.description = description;
        return this;
    }

    public AuditLogBuilder oldValues(Object oldValues) {
        this.oldValues = oldValues;
        return this;
    }

    public AuditLogBuilder newValues(Object newValues) {
        this.newValues = newValues;
        return this;
    }

    public AuditLogBuilder request(String ipAddress, String userAgent, String requestId) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestId = requestId;
        return this;
    }

    public AuditLog build() {
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(tenantId);
        auditLog.setActorId(actorId);
        auditLog.setActorEmail(actorEmail);
        auditLog.setActorName(actorName);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setEntityTitle(entityTitle);
        auditLog.setDescription(description);
        auditLog.setOldValues(toJson(oldValues));
        auditLog.setNewValues(toJson(newValues));
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setRequestId(requestId);
        return auditLog;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }
}
