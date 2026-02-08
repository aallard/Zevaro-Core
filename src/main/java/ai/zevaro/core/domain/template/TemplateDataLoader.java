package ai.zevaro.core.domain.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class TemplateDataLoader implements CommandLineRunner {

    private final ProgramTemplateRepository templateRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedTemplate("ERP Replacement",
                "Template for large-scale ERP replacement programs with vendor evaluation, data migration, and compliance tracks",
                """
                [
                  {"name": "Vendor Evaluation", "description": "Assess ERP vendor options and capabilities", "mode": "DISCOVERY", "executionMode": "HYBRID"},
                  {"name": "Code Audit", "description": "Analyze existing codebase and dependencies", "mode": "DISCOVERY", "executionMode": "AI_FIRST"},
                  {"name": "Data Migration", "description": "Design and build data migration pipelines", "mode": "BUILD", "executionMode": "AI_FIRST"},
                  {"name": "Integration Rewrites", "description": "Rebuild integrations for new platform", "mode": "BUILD", "executionMode": "AI_FIRST"},
                  {"name": "Compliance & Testing", "description": "Regulatory compliance and QA operations", "mode": "OPS", "executionMode": "HYBRID"}
                ]""");

        seedTemplate("New Product Launch",
                "Template for launching a new product from discovery through MVP to operational support",
                """
                [
                  {"name": "Discovery", "description": "Market research, user interviews, hypothesis validation", "mode": "DISCOVERY", "executionMode": "TRADITIONAL"},
                  {"name": "Core Build", "description": "MVP development", "mode": "BUILD", "executionMode": "AI_FIRST"},
                  {"name": "Launch Support", "description": "Bug fixes, monitoring, incident response", "mode": "OPS", "executionMode": "HYBRID"}
                ]""");

        seedTemplate("KTLO Operations",
                "Template for keeping-the-lights-on operational programs with tech debt and maintenance tracks",
                """
                [
                  {"name": "Tech Debt Assessment", "description": "Identify and prioritize technical debt", "mode": "DISCOVERY", "executionMode": "AI_FIRST"},
                  {"name": "Bug Fixes & Maintenance", "description": "Ongoing bug triage and resolution", "mode": "OPS", "executionMode": "HYBRID"}
                ]""");
    }

    private void seedTemplate(String name, String description, String structure) {
        if (templateRepository.existsByName(name)) {
            log.info("Template '{}' already exists, skipping", name);
            return;
        }

        ProgramTemplate template = new ProgramTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setStructure(structure);
        template.setIsSystem(true);
        templateRepository.save(template);
        log.info("Seeded system template: {}", name);
    }
}
