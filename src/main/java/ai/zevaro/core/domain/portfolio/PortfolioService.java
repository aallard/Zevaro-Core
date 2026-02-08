package ai.zevaro.core.domain.portfolio;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.decision.DecisionStatus;
import ai.zevaro.core.domain.portfolio.dto.CreatePortfolioRequest;
import ai.zevaro.core.domain.portfolio.dto.PortfolioDashboardResponse;
import ai.zevaro.core.domain.portfolio.dto.PortfolioResponse;
import ai.zevaro.core.domain.portfolio.dto.ProgramHealthSummary;
import ai.zevaro.core.domain.portfolio.dto.UpdatePortfolioRequest;
import ai.zevaro.core.domain.project.Project;
import ai.zevaro.core.domain.project.ProjectRepository;
import ai.zevaro.core.domain.project.ProjectStatus;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import ai.zevaro.core.security.UserPrincipal;
import ai.zevaro.core.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;
    private final PortfolioMapper portfolioMapper;
    private final SlugGenerator slugGenerator;
    private final AuditService auditService;

    @Transactional
    public PortfolioResponse create(UUID tenantId, CreatePortfolioRequest request, UserPrincipal currentUser) {
        Portfolio portfolio = portfolioMapper.toEntity(request, tenantId, currentUser.getUserId());

        // Generate unique slug
        String uniqueSlug = slugGenerator.generateUniqueSlug(
                request.name(),
                slug -> portfolioRepository.existsByTenantIdAndSlug(tenantId, slug)
        );
        portfolio.setSlug(uniqueSlug);

        portfolio = portfolioRepository.save(portfolio);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.CREATE)
                .entity("PORTFOLIO", portfolio.getId(), portfolio.getName())
                .description("Created portfolio: " + portfolio.getName()));

        String ownerName = resolveOwnerName(portfolio.getOwnerId(), tenantId);
        int programCount = countPrograms(portfolio.getId(), tenantId);
        return portfolioMapper.toResponse(portfolio, programCount, ownerName);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getById(UUID id, UUID tenantId) {
        Portfolio portfolio = portfolioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));

        String ownerName = resolveOwnerName(portfolio.getOwnerId(), tenantId);
        int programCount = countPrograms(portfolio.getId(), tenantId);
        return portfolioMapper.toResponse(portfolio, programCount, ownerName);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getBySlug(String slug, UUID tenantId) {
        Portfolio portfolio = portfolioRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "slug", slug));

        String ownerName = resolveOwnerName(portfolio.getOwnerId(), tenantId);
        int programCount = countPrograms(portfolio.getId(), tenantId);
        return portfolioMapper.toResponse(portfolio, programCount, ownerName);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> list(UUID tenantId, PortfolioStatus status) {
        List<Portfolio> portfolios;
        if (status != null) {
            portfolios = portfolioRepository.findByTenantIdAndStatus(tenantId, status);
        } else {
            portfolios = portfolioRepository.findByTenantId(tenantId);
        }

        return portfolios.stream()
                .map(p -> {
                    String ownerName = resolveOwnerName(p.getOwnerId(), tenantId);
                    int programCount = countPrograms(p.getId(), tenantId);
                    return portfolioMapper.toResponse(p, programCount, ownerName);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PortfolioResponse> listPaged(UUID tenantId, Pageable pageable) {
        Page<Portfolio> page = portfolioRepository.findByTenantId(tenantId, pageable);
        return page.map(p -> {
            String ownerName = resolveOwnerName(p.getOwnerId(), tenantId);
            int programCount = countPrograms(p.getId(), tenantId);
            return portfolioMapper.toResponse(p, programCount, ownerName);
        });
    }

    @Transactional
    public PortfolioResponse update(UUID id, UUID tenantId, UpdatePortfolioRequest request, UserPrincipal currentUser) {
        Portfolio portfolio = portfolioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));

        portfolioMapper.updateEntity(portfolio, request);
        portfolio = portfolioRepository.save(portfolio);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.UPDATE)
                .entity("PORTFOLIO", portfolio.getId(), portfolio.getName())
                .description("Updated portfolio: " + portfolio.getName()));

        String ownerName = resolveOwnerName(portfolio.getOwnerId(), tenantId);
        int programCount = countPrograms(portfolio.getId(), tenantId);
        return portfolioMapper.toResponse(portfolio, programCount, ownerName);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UserPrincipal currentUser) {
        Portfolio portfolio = portfolioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));

        portfolio.setStatus(PortfolioStatus.ARCHIVED);
        portfolioRepository.save(portfolio);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(currentUser)
                .action(AuditAction.DELETE)
                .entity("PORTFOLIO", portfolio.getId(), portfolio.getName())
                .description("Archived portfolio: " + portfolio.getName()));
    }

    @Transactional(readOnly = true)
    public List<Project> getPrograms(UUID portfolioId, UUID tenantId) {
        portfolioRepository.findByIdAndTenantId(portfolioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        return projectRepository.findByTenantIdAndPortfolioId(tenantId, portfolioId);
    }

    @Transactional(readOnly = true)
    public PortfolioDashboardResponse getDashboard(UUID portfolioId, UUID tenantId) {
        Portfolio portfolio = portfolioRepository.findByIdAndTenantId(portfolioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        List<Project> programs = projectRepository.findByTenantIdAndPortfolioId(tenantId, portfolioId);

        int totalPrograms = programs.size();
        int activePrograms = (int) programs.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
                .count();

        List<UUID> programIds = programs.stream().map(Project::getId).toList();

        // Aggregate decisions across all programs in this portfolio
        int totalPending = 0;
        int totalBreached = 0;
        double totalCycleTimeHours = 0;
        int cycleTimeCount = 0;

        List<ProgramHealthSummary> programSummaries = new ArrayList<>();

        for (Project program : programs) {
            int pending = (int) decisionRepository.countByTenantIdAndProjectIdAndStatus(
                    tenantId, program.getId(), DecisionStatus.NEEDS_INPUT);
            totalPending += pending;

            List<Decision> breachedDecisions = decisionRepository.findSlaBreachedForProject(tenantId, program.getId());
            int breached = breachedDecisions.size();
            totalBreached += breached;

            // Determine health indicator based on breached/pending ratio
            String healthIndicator;
            if (breached > 0) {
                healthIndicator = "RED";
            } else if (pending > 3) {
                healthIndicator = "YELLOW";
            } else {
                healthIndicator = "GREEN";
            }

            // Count workstreams (child projects under this program — placeholder 0)
            int workstreamCount = 0;

            programSummaries.add(new ProgramHealthSummary(
                    program.getId(),
                    program.getName(),
                    program.getStatus(),
                    healthIndicator,
                    pending,
                    breached,
                    workstreamCount
            ));
        }

        // Average cycle time across portfolio
        Double avgCycleTime = decisionRepository.getAverageDecisionTimeHours(
                tenantId, Instant.now().minus(Duration.ofDays(30)));
        double avgDecisionCycleTimeHours = avgCycleTime != null ? avgCycleTime : 0.0;

        return new PortfolioDashboardResponse(
                portfolio.getId(),
                portfolio.getName(),
                totalPrograms,
                activePrograms,
                totalPending,
                totalBreached,
                avgDecisionCycleTimeHours,
                programSummaries
        );
    }

    @Transactional(readOnly = true)
    public List<Decision> getDecisions(UUID portfolioId, UUID tenantId) {
        portfolioRepository.findByIdAndTenantId(portfolioId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        List<Project> programs = projectRepository.findByTenantIdAndPortfolioId(tenantId, portfolioId);
        List<UUID> programIds = programs.stream().map(Project::getId).toList();

        if (programIds.isEmpty()) {
            return List.of();
        }

        return decisionRepository.findByTenantIdAndProjectIdIn(tenantId, programIds);
    }

    private String resolveOwnerName(UUID ownerId, UUID tenantId) {
        if (ownerId == null) {
            return null;
        }
        return userRepository.findByIdAndTenantId(ownerId, tenantId)
                .map(User::getFullName)
                .orElse(null);
    }

    private int countPrograms(UUID portfolioId, UUID tenantId) {
        return projectRepository.findByTenantIdAndPortfolioId(tenantId, portfolioId).size();
    }
}
