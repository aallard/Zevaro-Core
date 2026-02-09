package ai.zevaro.core.domain.seed;

import ai.zevaro.core.domain.document.Document;
import ai.zevaro.core.domain.document.DocumentRepository;
import ai.zevaro.core.domain.document.DocumentStatus;
import ai.zevaro.core.domain.document.DocumentType;
import ai.zevaro.core.domain.portfolio.Portfolio;
import ai.zevaro.core.domain.portfolio.PortfolioRepository;
import ai.zevaro.core.domain.portfolio.PortfolioStatus;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.program.ProgramStatus;
import ai.zevaro.core.domain.requirement.DependencyType;
import ai.zevaro.core.domain.requirement.Requirement;
import ai.zevaro.core.domain.requirement.RequirementDependency;
import ai.zevaro.core.domain.requirement.RequirementDependencyRepository;
import ai.zevaro.core.domain.requirement.RequirementPriority;
import ai.zevaro.core.domain.requirement.RequirementRepository;
import ai.zevaro.core.domain.requirement.RequirementStatus;
import ai.zevaro.core.domain.requirement.RequirementType;
import ai.zevaro.core.domain.space.Space;
import ai.zevaro.core.domain.space.SpaceRepository;
import ai.zevaro.core.domain.space.SpaceType;
import ai.zevaro.core.domain.space.SpaceVisibility;
import ai.zevaro.core.domain.specification.Specification;
import ai.zevaro.core.domain.specification.SpecificationRepository;
import ai.zevaro.core.domain.specification.SpecificationStatus;
import ai.zevaro.core.domain.ticket.Ticket;
import ai.zevaro.core.domain.ticket.TicketRepository;
import ai.zevaro.core.domain.ticket.TicketSource;
import ai.zevaro.core.domain.ticket.TicketStatus;
import ai.zevaro.core.domain.ticket.TicketType;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.ExecutionMode;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.domain.workstream.WorkstreamMode;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import ai.zevaro.core.domain.workstream.WorkstreamStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Order(4)
@RequiredArgsConstructor
@Slf4j
public class V2SeedDataLoader implements CommandLineRunner {

    private final PortfolioRepository portfolioRepository;
    private final ProgramRepository programRepository;
    private final WorkstreamRepository workstreamRepository;
    private final SpecificationRepository specificationRepository;
    private final RequirementRepository requirementRepository;
    private final RequirementDependencyRepository requirementDependencyRepository;
    private final TicketRepository ticketRepository;
    private final SpaceRepository spaceRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Override
    @Transactional
    public void run(String... args) {
        User seedUser = userRepository.findByEmailAndTenantId("adam@zevarolabs.com", TENANT_ID)
                .orElse(null);
        if (seedUser == null) {
            log.warn("Seed user adam@zevarolabs.com not found for tenant {}. Skipping v2 seed data.", TENANT_ID);
            return;
        }
        UUID userId = seedUser.getId();

        // Portfolio
        Portfolio portfolio = ensurePortfolio("Digital Transformation", "digital-transformation",
                "Enterprise-wide digital transformation initiative", userId);

        // Program (linked to portfolio)
        Program program = ensureProgram("ERP Replacement", "erp-replacement",
                "Full ERP platform replacement program", portfolio.getId(), userId);

        // Workstreams
        Workstream wsVendor = ensureWorkstream(program.getId(), "Vendor Evaluation",
                WorkstreamMode.DISCOVERY, ExecutionMode.HYBRID, userId);
        Workstream wsData = ensureWorkstream(program.getId(), "Data Migration",
                WorkstreamMode.BUILD, ExecutionMode.AI_FIRST, userId);
        ensureWorkstream(program.getId(), "Compliance & Testing",
                WorkstreamMode.OPS, ExecutionMode.HYBRID, userId);

        // Specifications
        Specification specVendor = ensureSpecification(wsVendor.getId(), program.getId(),
                "Vendor Requirements Spec", SpecificationStatus.APPROVED, userId);
        Specification specData = ensureSpecification(wsData.getId(), program.getId(),
                "Data Migration Plan", SpecificationStatus.DRAFT, userId);

        // Requirements
        Requirement reqVE001 = ensureRequirement(specVendor.getId(), wsVendor.getId(), program.getId(),
                "VE-001", "Cloud hosting support", RequirementType.FUNCTIONAL,
                RequirementPriority.MUST_HAVE, RequirementStatus.APPROVED, userId);
        Requirement reqVE002 = ensureRequirement(specVendor.getId(), wsVendor.getId(), program.getId(),
                "VE-002", "API integration layer", RequirementType.FUNCTIONAL,
                RequirementPriority.SHOULD_HAVE, RequirementStatus.DRAFT, userId);
        ensureRequirement(specData.getId(), wsData.getId(), program.getId(),
                "DM-001", "Zero-downtime migration", RequirementType.NON_FUNCTIONAL,
                RequirementPriority.MUST_HAVE, RequirementStatus.DRAFT, userId);

        // Requirement dependency: VE-002 REQUIRES VE-001
        ensureRequirementDependency(reqVE002.getId(), reqVE001.getId(), DependencyType.REQUIRES);

        // Tickets
        ensureTicket(wsVendor.getId(), program.getId(), "VE-T001",
                "Evaluate AWS vs Azure", TicketType.ENHANCEMENT, TicketStatus.IN_PROGRESS, userId);
        ensureTicket(wsData.getId(), program.getId(), "DM-T001",
                "Schema mapping document", TicketType.ENHANCEMENT, TicketStatus.NEW, userId);

        // Space
        Space space = ensureSpace("ERP Program Space", "erp-program-space",
                SpaceType.PROGRAM, program.getId(), userId);

        // Documents
        ensureDocument(space.getId(), "Architecture Decision Log",
                DocumentType.PAGE, DocumentStatus.PUBLISHED, userId);
        ensureDocument(space.getId(), "Vendor Evaluation Criteria",
                DocumentType.SPECIFICATION, DocumentStatus.DRAFT, userId);

        log.info("V2 seed data loaded successfully");
    }

    private Portfolio ensurePortfolio(String name, String slug, String description, UUID userId) {
        if (portfolioRepository.existsByTenantIdAndSlug(TENANT_ID, slug)) {
            log.info("Portfolio '{}' already exists, skipping", name);
            return portfolioRepository.findByTenantIdAndSlug(TENANT_ID, slug).orElseThrow();
        }
        Portfolio p = new Portfolio();
        p.setTenantId(TENANT_ID);
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(description);
        p.setStatus(PortfolioStatus.ACTIVE);
        p.setOwnerId(userId);
        p.setCreatedById(userId);
        p = portfolioRepository.save(p);
        log.info("Seeded portfolio: {}", name);
        return p;
    }

    private Program ensureProgram(String name, String slug, String description, UUID portfolioId, UUID userId) {
        var existing = programRepository.findBySlugAndTenantId(slug, TENANT_ID);
        if (existing.isPresent()) {
            log.info("Program '{}' already exists, skipping", name);
            Program prog = existing.get();
            if (prog.getPortfolioId() == null && portfolioId != null) {
                prog.setPortfolioId(portfolioId);
                prog = programRepository.save(prog);
                log.info("Linked program '{}' to portfolio", name);
            }
            return prog;
        }
        Program p = new Program();
        p.setTenantId(TENANT_ID);
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(description);
        p.setStatus(ProgramStatus.ACTIVE);
        p.setPortfolioId(portfolioId);
        p.setCreatedById(userId);
        p = programRepository.save(p);
        log.info("Seeded program: {}", name);
        return p;
    }

    private Workstream ensureWorkstream(UUID programId, String name,
                                         WorkstreamMode mode, ExecutionMode executionMode, UUID userId) {
        if (workstreamRepository.existsByTenantIdAndProgramIdAndName(TENANT_ID, programId, name)) {
            log.info("Workstream '{}' already exists, skipping", name);
            return workstreamRepository.findByTenantIdAndProgramIdOrderBySortOrderAsc(TENANT_ID, programId)
                    .stream().filter(w -> w.getName().equals(name)).findFirst().orElseThrow();
        }
        Workstream w = new Workstream();
        w.setTenantId(TENANT_ID);
        w.setProgramId(programId);
        w.setName(name);
        w.setMode(mode);
        w.setExecutionMode(executionMode);
        w.setStatus(WorkstreamStatus.ACTIVE);
        w.setOwnerId(userId);
        w.setCreatedById(userId);
        w = workstreamRepository.save(w);
        log.info("Seeded workstream: {}", name);
        return w;
    }

    private Specification ensureSpecification(UUID workstreamId, UUID programId, String name,
                                               SpecificationStatus status, UUID userId) {
        var existing = specificationRepository.findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(TENANT_ID, workstreamId)
                .stream().filter(s -> s.getName().equals(name)).findFirst();
        if (existing.isPresent()) {
            log.info("Specification '{}' already exists, skipping", name);
            return existing.get();
        }
        Specification s = new Specification();
        s.setTenantId(TENANT_ID);
        s.setWorkstreamId(workstreamId);
        s.setProgramId(programId);
        s.setName(name);
        s.setStatus(status);
        s.setAuthorId(userId);
        s.setCreatedById(userId);
        s = specificationRepository.save(s);
        log.info("Seeded specification: {}", name);
        return s;
    }

    private Requirement ensureRequirement(UUID specificationId, UUID workstreamId, UUID programId,
                                           String identifier, String title, RequirementType type,
                                           RequirementPriority priority, RequirementStatus status, UUID userId) {
        var existing = requirementRepository.findByTenantIdAndSpecificationIdOrderBySortOrderAsc(TENANT_ID, specificationId)
                .stream().filter(r -> r.getIdentifier().equals(identifier)).findFirst();
        if (existing.isPresent()) {
            log.info("Requirement '{}' already exists, skipping", identifier);
            return existing.get();
        }
        Requirement r = new Requirement();
        r.setTenantId(TENANT_ID);
        r.setSpecificationId(specificationId);
        r.setWorkstreamId(workstreamId);
        r.setProgramId(programId);
        r.setIdentifier(identifier);
        r.setTitle(title);
        r.setType(type);
        r.setPriority(priority);
        r.setStatus(status);
        r.setCreatedById(userId);
        r = requirementRepository.save(r);
        log.info("Seeded requirement: {} - {}", identifier, title);
        return r;
    }

    private void ensureRequirementDependency(UUID requirementId, UUID dependsOnId, DependencyType type) {
        if (requirementDependencyRepository.existsByRequirementIdAndDependsOnId(requirementId, dependsOnId)) {
            log.info("RequirementDependency already exists, skipping");
            return;
        }
        RequirementDependency d = new RequirementDependency();
        d.setRequirementId(requirementId);
        d.setDependsOnId(dependsOnId);
        d.setType(type);
        requirementDependencyRepository.save(d);
        log.info("Seeded requirement dependency: {} -> {}", requirementId, dependsOnId);
    }

    private void ensureTicket(UUID workstreamId, UUID programId, String identifier,
                               String title, TicketType type, TicketStatus status, UUID userId) {
        var existing = ticketRepository.findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(TENANT_ID, workstreamId)
                .stream().filter(t -> t.getIdentifier().equals(identifier)).findFirst();
        if (existing.isPresent()) {
            log.info("Ticket '{}' already exists, skipping", identifier);
            return;
        }
        Ticket t = new Ticket();
        t.setTenantId(TENANT_ID);
        t.setWorkstreamId(workstreamId);
        t.setProgramId(programId);
        t.setIdentifier(identifier);
        t.setTitle(title);
        t.setType(type);
        t.setStatus(status);
        t.setReportedById(userId);
        t.setSource(TicketSource.MANUAL);
        t.setCreatedById(userId);
        ticketRepository.save(t);
        log.info("Seeded ticket: {} - {}", identifier, title);
    }

    private Space ensureSpace(String name, String slug, SpaceType type, UUID programId, UUID userId) {
        if (spaceRepository.existsByTenantIdAndSlug(TENANT_ID, slug)) {
            log.info("Space '{}' already exists, skipping", name);
            return spaceRepository.findByTenantIdAndSlug(TENANT_ID, slug).orElseThrow();
        }
        Space s = new Space();
        s.setTenantId(TENANT_ID);
        s.setName(name);
        s.setSlug(slug);
        s.setType(type);
        s.setProgramId(programId);
        s.setOwnerId(userId);
        s.setVisibility(SpaceVisibility.PUBLIC);
        s.setCreatedById(userId);
        s = spaceRepository.save(s);
        log.info("Seeded space: {}", name);
        return s;
    }

    private void ensureDocument(UUID spaceId, String title, DocumentType type,
                                 DocumentStatus status, UUID userId) {
        var existing = documentRepository.findByTenantIdAndSpaceIdOrderBySortOrderAsc(TENANT_ID, spaceId)
                .stream().filter(d -> d.getTitle().equals(title)).findFirst();
        if (existing.isPresent()) {
            log.info("Document '{}' already exists, skipping", title);
            return;
        }
        Document d = new Document();
        d.setTenantId(TENANT_ID);
        d.setSpaceId(spaceId);
        d.setTitle(title);
        d.setType(type);
        d.setStatus(status);
        d.setAuthorId(userId);
        documentRepository.save(d);
        log.info("Seeded document: {}", title);
    }
}
