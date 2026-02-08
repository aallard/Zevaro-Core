package ai.zevaro.core.domain.rbac;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class RolePermissionMapper {

    private static final Set<String> READ_PERMISSIONS = Set.of(
            "outcome:read", "hypothesis:read", "decision:read", "experiment:read",
            "team:read", "user:read", "stakeholder:read", "analytics:read",
            "portfolio:read", "workstream:read", "specification:read", "requirement:read",
            "ticket:read", "space:read", "document:read", "attachment:read",
            "comment:read", "template:read", "queue:read"
    );

    private static final Set<String> CREATE_UPDATE_PERMISSIONS = Set.of(
            "outcome:create", "outcome:update", "hypothesis:create", "hypothesis:update",
            "decision:create", "decision:update", "decision:comment",
            "experiment:create", "experiment:update",
            "hypothesis:transition", "hypothesis:build",
            "portfolio:create", "portfolio:update",
            "workstream:create", "workstream:update",
            "specification:create", "specification:update",
            "requirement:create", "requirement:update", "requirement:delete",
            "ticket:create", "ticket:update", "ticket:assign",
            "space:create", "space:update",
            "document:create", "document:update", "document:publish",
            "attachment:create", "attachment:delete",
            "comment:create", "comment:update", "comment:delete",
            "queue:create", "queue:update"
    );

    private static final Set<String> FULL_CRUD_PERMISSIONS = Set.of(
            "outcome:delete", "outcome:validate", "outcome:assign",
            "hypothesis:delete", "experiment:delete", "decision:delete", "decision:resolve",
            "decision:escalate", "decision:assign", "team:create", "team:update",
            "team:delete", "team:manage_members", "stakeholder:create",
            "stakeholder:update", "stakeholder:delete",
            "portfolio:delete", "workstream:delete",
            "specification:delete", "specification:approve",
            "ticket:delete", "ticket:close",
            "space:delete", "document:delete",
            "template:create", "template:update", "template:delete",
            "queue:delete"
    );

    private static final Set<String> MANAGEMENT_PERMISSIONS = Set.of(
            "user:create", "user:update", "user:delete", "user:manage_roles",
            "analytics:export", "integration:read", "integration:create",
            "integration:update", "integration:delete", "integration:sync"
    );

    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
            "analytics:admin", "system:admin", "system:settings",
            "system:audit_read", "system:tenant_manage"
    );

    public List<String> getPermissionsForLevel(RoleLevel level, RoleCategory category) {
        List<String> permissions = new ArrayList<>();

        // L1-L2: Read permissions
        if (level.ordinal() >= RoleLevel.L1_INDIVIDUAL.ordinal()) {
            permissions.addAll(READ_PERMISSIONS);
        }

        // L3: Read + Create/Update in domain
        if (level.ordinal() >= RoleLevel.L3_LEAD.ordinal()) {
            permissions.addAll(CREATE_UPDATE_PERMISSIONS);
        }

        // L4-L5: Full CRUD + Assign + Manage
        if (level.ordinal() >= RoleLevel.L4_MANAGER.ordinal()) {
            permissions.addAll(FULL_CRUD_PERMISSIONS);
        }

        // L6+: Cross-domain + Management
        if (level.ordinal() >= RoleLevel.L6_VP.ordinal()) {
            permissions.addAll(MANAGEMENT_PERMISSIONS);
        }

        // L8-L9: All permissions
        if (level.ordinal() >= RoleLevel.L8_CXXX.ordinal()) {
            permissions.addAll(ADMIN_PERMISSIONS);
        }

        return permissions;
    }

    public List<String> getAiAgentPermissions() {
        List<String> permissions = new ArrayList<>();
        // All :read permissions
        permissions.addAll(READ_PERMISSIONS);
        // All :create permissions
        permissions.addAll(List.of(
                "outcome:create", "hypothesis:create", "decision:create", "experiment:create",
                "portfolio:create", "workstream:create", "specification:create",
                "requirement:create", "ticket:create", "space:create", "document:create",
                "attachment:create", "comment:create", "queue:create"
        ));
        // Selective :update permissions
        permissions.addAll(List.of(
                "specification:update", "requirement:update", "ticket:update",
                "ticket:assign", "document:update", "comment:update"
        ));
        return permissions;
    }
}
