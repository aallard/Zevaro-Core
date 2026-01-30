package ai.zevaro.core.domain.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RoleDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper permissionMapper;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Roles already exist, skipping initialization");
            return;
        }

        log.info("Initializing system roles...");

        List<Role> roles = List.of(
                // System Roles
                createRole("SUPER_ADMIN", "Super Administrator", "Full system access across all tenants", RoleCategory.SYSTEM, RoleLevel.L9_OWNER),
                createRole("TENANT_ADMIN", "Tenant Administrator", "Full access within tenant", RoleCategory.SYSTEM, RoleLevel.L8_CXXX),

                // Executive Roles
                createRole("CEO", "Chief Executive Officer", "Company CEO", RoleCategory.EXECUTIVE, RoleLevel.L8_CXXX),
                createRole("CPO", "Chief Product Officer", "Head of Product", RoleCategory.EXECUTIVE, RoleLevel.L8_CXXX),
                createRole("CTO", "Chief Technology Officer", "Head of Technology", RoleCategory.EXECUTIVE, RoleLevel.L8_CXXX),
                createRole("VP_PRODUCT", "VP of Product", "Vice President of Product", RoleCategory.EXECUTIVE, RoleLevel.L6_VP),
                createRole("VP_ENGINEERING", "VP of Engineering", "Vice President of Engineering", RoleCategory.EXECUTIVE, RoleLevel.L6_VP),
                createRole("VP_UX", "VP of User Experience", "Vice President of UX", RoleCategory.EXECUTIVE, RoleLevel.L6_VP),

                // Product Roles
                createRole("PRODUCT_DIRECTOR", "Product Director", "Director of Product Management", RoleCategory.PRODUCT, RoleLevel.L5_SENIOR_MANAGER),
                createRole("PRODUCT_MANAGER_SR", "Senior Product Manager", "Senior PM", RoleCategory.PRODUCT, RoleLevel.L4_MANAGER),
                createRole("PRODUCT_MANAGER", "Product Manager", "Product Manager", RoleCategory.PRODUCT, RoleLevel.L3_LEAD),
                createRole("PRODUCT_ANALYST", "Product Analyst", "Product Analyst", RoleCategory.PRODUCT, RoleLevel.L2_SENIOR),
                createRole("PRODUCT_ASSOCIATE", "Product Associate", "Associate PM", RoleCategory.PRODUCT, RoleLevel.L1_INDIVIDUAL),

                // Engineering Roles
                createRole("ENG_DIRECTOR", "Engineering Director", "Director of Engineering", RoleCategory.ENGINEERING, RoleLevel.L5_SENIOR_MANAGER),
                createRole("ENG_MANAGER", "Engineering Manager", "Engineering Manager", RoleCategory.ENGINEERING, RoleLevel.L4_MANAGER),
                createRole("TECH_LEAD", "Technical Lead", "Tech Lead", RoleCategory.ENGINEERING, RoleLevel.L3_LEAD),
                createRole("STAFF_ENGINEER", "Staff Engineer", "Staff Software Engineer", RoleCategory.ENGINEERING, RoleLevel.L3_LEAD),
                createRole("SR_ENGINEER", "Senior Engineer", "Senior Software Engineer", RoleCategory.ENGINEERING, RoleLevel.L2_SENIOR),
                createRole("ENGINEER", "Software Engineer", "Software Engineer", RoleCategory.ENGINEERING, RoleLevel.L1_INDIVIDUAL),
                createRole("JR_ENGINEER", "Junior Engineer", "Junior Software Engineer", RoleCategory.ENGINEERING, RoleLevel.L1_INDIVIDUAL),

                // UX Roles
                createRole("UX_DIRECTOR", "UX Director", "Director of UX", RoleCategory.UX, RoleLevel.L5_SENIOR_MANAGER),
                createRole("UX_MANAGER", "UX Manager", "UX Manager", RoleCategory.UX, RoleLevel.L4_MANAGER),
                createRole("UX_LEAD", "UX Lead", "UX Lead Designer", RoleCategory.UX, RoleLevel.L3_LEAD),
                createRole("SR_UX_DESIGNER", "Senior UX Designer", "Senior UX Designer", RoleCategory.UX, RoleLevel.L2_SENIOR),
                createRole("UX_DESIGNER", "UX Designer", "UX Designer", RoleCategory.UX, RoleLevel.L1_INDIVIDUAL),
                createRole("UX_RESEARCHER", "UX Researcher", "UX Researcher", RoleCategory.UX, RoleLevel.L2_SENIOR),

                // QA Roles
                createRole("QA_DIRECTOR", "QA Director", "Director of Quality Assurance", RoleCategory.QA, RoleLevel.L5_SENIOR_MANAGER),
                createRole("QA_MANAGER", "QA Manager", "QA Manager", RoleCategory.QA, RoleLevel.L4_MANAGER),
                createRole("QA_LEAD", "QA Lead", "QA Lead", RoleCategory.QA, RoleLevel.L3_LEAD),
                createRole("SR_QA_ENGINEER", "Senior QA Engineer", "Senior QA Engineer", RoleCategory.QA, RoleLevel.L2_SENIOR),
                createRole("QA_ENGINEER", "QA Engineer", "QA Engineer", RoleCategory.QA, RoleLevel.L1_INDIVIDUAL),
                createRole("QA_ANALYST", "QA Analyst", "QA Analyst", RoleCategory.QA, RoleLevel.L1_INDIVIDUAL),

                // Data Roles
                createRole("DATA_DIRECTOR", "Data Director", "Director of Data", RoleCategory.DATA, RoleLevel.L5_SENIOR_MANAGER),
                createRole("DATA_MANAGER", "Data Manager", "Data Manager", RoleCategory.DATA, RoleLevel.L4_MANAGER),
                createRole("SR_DATA_ANALYST", "Senior Data Analyst", "Senior Data Analyst", RoleCategory.DATA, RoleLevel.L2_SENIOR),
                createRole("DATA_ANALYST", "Data Analyst", "Data Analyst", RoleCategory.DATA, RoleLevel.L1_INDIVIDUAL),
                createRole("DATA_SCIENTIST", "Data Scientist", "Data Scientist", RoleCategory.DATA, RoleLevel.L2_SENIOR),

                // Business Roles
                createRole("BIZ_DIRECTOR", "Business Director", "Director of Business Operations", RoleCategory.BUSINESS, RoleLevel.L5_SENIOR_MANAGER),
                createRole("BIZ_MANAGER", "Business Operations Manager", "Business Ops Manager", RoleCategory.BUSINESS, RoleLevel.L4_MANAGER),
                createRole("SR_BIZ_ANALYST", "Senior Business Analyst", "Senior Business Analyst", RoleCategory.BUSINESS, RoleLevel.L2_SENIOR),
                createRole("BIZ_ANALYST", "Business Analyst", "Business Analyst", RoleCategory.BUSINESS, RoleLevel.L1_INDIVIDUAL),
                createRole("BIZ_USER", "Business User", "Business User", RoleCategory.BUSINESS, RoleLevel.L1_INDIVIDUAL),

                // Stakeholder Roles
                createRole("STAKEHOLDER_EXEC", "Executive Stakeholder", "Executive level stakeholder", RoleCategory.STAKEHOLDER, RoleLevel.L6_VP),
                createRole("STAKEHOLDER", "Stakeholder", "Standard stakeholder", RoleCategory.STAKEHOLDER, RoleLevel.L3_LEAD),
                createRole("VIEWER", "View Only", "Read-only access", RoleCategory.STAKEHOLDER, RoleLevel.L1_INDIVIDUAL)
        );

        // Assign permissions based on level
        for (Role role : roles) {
            List<String> permissionCodes = permissionMapper.getPermissionsForLevel(role.getLevel(), role.getCategory());
            Set<Permission> permissions = new HashSet<>(permissionRepository.findByCodeIn(permissionCodes));
            role.setPermissions(permissions);
        }

        roleRepository.saveAll(roles);
        log.info("Created {} system roles", roles.size());
    }

    private Role createRole(String code, String name, String description, RoleCategory category, RoleLevel level) {
        return new Role(code, name, description, category, level, true);
    }
}
