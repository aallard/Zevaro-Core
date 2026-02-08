package ai.zevaro.core.domain.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class PermissionDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (permissionRepository.count() > 0) {
            log.info("Permissions already exist, skipping initialization");
            return;
        }

        log.info("Initializing permissions...");

        List<Permission> permissions = List.of(
                // Project permissions
                new Permission("project:read", "Read Projects", "View projects", "PROJECT"),
                new Permission("project:create", "Create Projects", "Create new projects", "PROJECT"),
                new Permission("project:update", "Update Projects", "Modify existing projects", "PROJECT"),
                new Permission("project:delete", "Delete Projects", "Remove projects", "PROJECT"),

                // Outcome permissions
                new Permission("outcome:read", "Read Outcomes", "View outcomes", "OUTCOME"),
                new Permission("outcome:create", "Create Outcomes", "Create new outcomes", "OUTCOME"),
                new Permission("outcome:update", "Update Outcomes", "Modify existing outcomes", "OUTCOME"),
                new Permission("outcome:delete", "Delete Outcomes", "Remove outcomes", "OUTCOME"),
                new Permission("outcome:validate", "Validate Outcomes", "Mark outcomes as validated", "OUTCOME"),
                new Permission("outcome:assign", "Assign Outcomes", "Assign outcomes to users/teams", "OUTCOME"),

                // Hypothesis permissions
                new Permission("hypothesis:read", "Read Hypotheses", "View hypotheses", "HYPOTHESIS"),
                new Permission("hypothesis:create", "Create Hypotheses", "Create new hypotheses", "HYPOTHESIS"),
                new Permission("hypothesis:update", "Update Hypotheses", "Modify existing hypotheses", "HYPOTHESIS"),
                new Permission("hypothesis:delete", "Delete Hypotheses", "Remove hypotheses", "HYPOTHESIS"),
                new Permission("hypothesis:transition", "Transition Hypotheses", "Change hypothesis status", "HYPOTHESIS"),
                new Permission("hypothesis:build", "Build Hypotheses", "Start building hypothesis", "HYPOTHESIS"),
                new Permission("hypothesis:validate", "Validate Hypotheses", "Validate or conclude hypotheses", "HYPOTHESIS"),

                // Experiment permissions
                new Permission("experiment:read", "Read Experiments", "View experiments", "EXPERIMENT"),
                new Permission("experiment:create", "Create Experiments", "Create new experiments", "EXPERIMENT"),
                new Permission("experiment:update", "Update Experiments", "Modify existing experiments", "EXPERIMENT"),
                new Permission("experiment:delete", "Delete Experiments", "Remove experiments", "EXPERIMENT"),

                // Decision permissions
                new Permission("decision:read", "Read Decisions", "View decisions", "DECISION"),
                new Permission("decision:create", "Create Decisions", "Create new decisions", "DECISION"),
                new Permission("decision:update", "Update Decisions", "Modify existing decisions", "DECISION"),
                new Permission("decision:delete", "Delete Decisions", "Remove decisions", "DECISION"),
                new Permission("decision:resolve", "Resolve Decisions", "Resolve pending decisions", "DECISION"),
                new Permission("decision:escalate", "Escalate Decisions", "Escalate decisions to higher level", "DECISION"),
                new Permission("decision:assign", "Assign Decisions", "Assign decisions to resolvers", "DECISION"),
                new Permission("decision:comment", "Comment on Decisions", "Add comments to decisions", "DECISION"),
                new Permission("decision:vote", "Vote on Decisions", "Cast votes on decisions", "DECISION"),

                // Team permissions
                new Permission("team:read", "Read Teams", "View teams", "TEAM"),
                new Permission("team:create", "Create Teams", "Create new teams", "TEAM"),
                new Permission("team:update", "Update Teams", "Modify existing teams", "TEAM"),
                new Permission("team:delete", "Delete Teams", "Remove teams", "TEAM"),
                new Permission("team:manage_members", "Manage Team Members", "Add/remove team members", "TEAM"),

                // User permissions
                new Permission("user:read", "Read Users", "View users", "USER"),
                new Permission("user:create", "Create Users", "Create new users", "USER"),
                new Permission("user:update", "Update Users", "Modify existing users", "USER"),
                new Permission("user:delete", "Delete Users", "Remove users", "USER"),
                new Permission("user:manage_roles", "Manage User Roles", "Assign roles to users", "USER"),

                // Stakeholder permissions
                new Permission("stakeholder:read", "Read Stakeholders", "View stakeholders", "STAKEHOLDER"),
                new Permission("stakeholder:create", "Create Stakeholders", "Create new stakeholders", "STAKEHOLDER"),
                new Permission("stakeholder:update", "Update Stakeholders", "Modify existing stakeholders", "STAKEHOLDER"),
                new Permission("stakeholder:delete", "Delete Stakeholders", "Remove stakeholders", "STAKEHOLDER"),

                // Analytics permissions
                new Permission("analytics:read", "Read Analytics", "View analytics and reports", "ANALYTICS"),
                new Permission("analytics:export", "Export Analytics", "Export analytics data", "ANALYTICS"),
                new Permission("analytics:admin", "Admin Analytics", "Manage analytics settings", "ANALYTICS"),

                // Integration permissions
                new Permission("integration:read", "Read Integrations", "View integrations", "INTEGRATION"),
                new Permission("integration:create", "Create Integrations", "Create new integrations", "INTEGRATION"),
                new Permission("integration:update", "Update Integrations", "Modify existing integrations", "INTEGRATION"),
                new Permission("integration:delete", "Delete Integrations", "Remove integrations", "INTEGRATION"),
                new Permission("integration:sync", "Sync Integrations", "Trigger integration sync", "INTEGRATION"),

                // System permissions
                new Permission("system:admin", "System Administration", "Full system access", "SYSTEM"),
                new Permission("system:settings", "System Settings", "Manage system settings", "SYSTEM"),
                new Permission("system:audit_read", "Read Audit Logs", "View audit logs", "SYSTEM"),
                new Permission("system:tenant_manage", "Manage Tenant", "Manage tenant settings", "SYSTEM")
        );

        permissionRepository.saveAll(permissions);
        log.info("Created {} permissions", permissions.size());
    }
}
