package ai.zevaro.core.domain.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class PermissionDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking permissions...");

        List<Permission> allPermissions = List.of(
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
                new Permission("system:tenant_manage", "Manage Tenant", "Manage tenant settings", "SYSTEM"),

                // Portfolio permissions
                new Permission("portfolio:read", "Read Portfolios", "View portfolios", "PORTFOLIO"),
                new Permission("portfolio:create", "Create Portfolios", "Create new portfolios", "PORTFOLIO"),
                new Permission("portfolio:update", "Update Portfolios", "Modify existing portfolios", "PORTFOLIO"),
                new Permission("portfolio:delete", "Delete Portfolios", "Remove portfolios", "PORTFOLIO"),

                // Workstream permissions
                new Permission("workstream:read", "Read Workstreams", "View workstreams", "WORKSTREAM"),
                new Permission("workstream:create", "Create Workstreams", "Create new workstreams", "WORKSTREAM"),
                new Permission("workstream:update", "Update Workstreams", "Modify existing workstreams", "WORKSTREAM"),
                new Permission("workstream:delete", "Delete Workstreams", "Remove workstreams", "WORKSTREAM"),

                // Specification permissions
                new Permission("specification:read", "Read Specifications", "View specifications", "SPECIFICATION"),
                new Permission("specification:create", "Create Specifications", "Create new specifications", "SPECIFICATION"),
                new Permission("specification:update", "Update Specifications", "Modify existing specifications", "SPECIFICATION"),
                new Permission("specification:delete", "Delete Specifications", "Remove specifications", "SPECIFICATION"),
                new Permission("specification:approve", "Approve Specifications", "Approve specifications", "SPECIFICATION"),

                // Requirement permissions
                new Permission("requirement:read", "Read Requirements", "View requirements", "REQUIREMENT"),
                new Permission("requirement:create", "Create Requirements", "Create new requirements", "REQUIREMENT"),
                new Permission("requirement:update", "Update Requirements", "Modify existing requirements", "REQUIREMENT"),
                new Permission("requirement:delete", "Delete Requirements", "Remove requirements", "REQUIREMENT"),

                // Ticket permissions
                new Permission("ticket:read", "Read Tickets", "View tickets", "TICKET"),
                new Permission("ticket:create", "Create Tickets", "Create new tickets", "TICKET"),
                new Permission("ticket:update", "Update Tickets", "Modify existing tickets", "TICKET"),
                new Permission("ticket:delete", "Delete Tickets", "Remove tickets", "TICKET"),
                new Permission("ticket:assign", "Assign Tickets", "Assign tickets to users", "TICKET"),
                new Permission("ticket:close", "Close Tickets", "Close resolved tickets", "TICKET"),

                // Space permissions
                new Permission("space:read", "Read Spaces", "View spaces", "SPACE"),
                new Permission("space:create", "Create Spaces", "Create new spaces", "SPACE"),
                new Permission("space:update", "Update Spaces", "Modify existing spaces", "SPACE"),
                new Permission("space:delete", "Delete Spaces", "Remove spaces", "SPACE"),

                // Document permissions
                new Permission("document:read", "Read Documents", "View documents", "DOCUMENT"),
                new Permission("document:create", "Create Documents", "Create new documents", "DOCUMENT"),
                new Permission("document:update", "Update Documents", "Modify existing documents", "DOCUMENT"),
                new Permission("document:delete", "Delete Documents", "Remove documents", "DOCUMENT"),
                new Permission("document:publish", "Publish Documents", "Publish documents", "DOCUMENT"),

                // Attachment permissions
                new Permission("attachment:read", "Read Attachments", "View attachments", "ATTACHMENT"),
                new Permission("attachment:create", "Create Attachments", "Upload attachments", "ATTACHMENT"),
                new Permission("attachment:delete", "Delete Attachments", "Remove attachments", "ATTACHMENT"),

                // Comment permissions
                new Permission("comment:read", "Read Comments", "View comments", "COMMENT"),
                new Permission("comment:create", "Create Comments", "Create new comments", "COMMENT"),
                new Permission("comment:update", "Update Comments", "Modify existing comments", "COMMENT"),
                new Permission("comment:delete", "Delete Comments", "Remove comments", "COMMENT"),

                // Template permissions
                new Permission("template:read", "Read Templates", "View templates", "TEMPLATE"),
                new Permission("template:create", "Create Templates", "Create new templates", "TEMPLATE"),
                new Permission("template:update", "Update Templates", "Modify existing templates", "TEMPLATE"),
                new Permission("template:delete", "Delete Templates", "Remove templates", "TEMPLATE"),

                // Queue permissions
                new Permission("queue:read", "Read Queues", "View queues", "QUEUE"),
                new Permission("queue:create", "Create Queues", "Create new queues", "QUEUE"),
                new Permission("queue:update", "Update Queues", "Modify existing queues", "QUEUE"),
                new Permission("queue:delete", "Delete Queues", "Remove queues", "QUEUE")
        );

        Set<String> existingCodes = new java.util.HashSet<>();
        permissionRepository.findAll().forEach(p -> existingCodes.add(p.getCode()));

        List<Permission> newPermissions = allPermissions.stream()
                .filter(p -> !existingCodes.contains(p.getCode()))
                .toList();

        if (newPermissions.isEmpty()) {
            log.info("All {} permissions already exist", allPermissions.size());
        } else {
            permissionRepository.saveAll(newPermissions);
            log.info("Created {} new permissions (total defined: {})", newPermissions.size(), allPermissions.size());
        }
    }
}
