package ai.zevaro.core.domain.search;

import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.document.Document;
import ai.zevaro.core.domain.document.DocumentRepository;
import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.requirement.Requirement;
import ai.zevaro.core.domain.requirement.RequirementRepository;
import ai.zevaro.core.domain.search.dto.SearchResult;
import ai.zevaro.core.domain.specification.Specification;
import ai.zevaro.core.domain.specification.SpecificationRepository;
import ai.zevaro.core.domain.ticket.Ticket;
import ai.zevaro.core.domain.ticket.TicketRepository;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProgramRepository programRepository;
    private final WorkstreamRepository workstreamRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final SpecificationRepository specificationRepository;
    private final RequirementRepository requirementRepository;
    private final TicketRepository ticketRepository;
    private final DecisionRepository decisionRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<SearchResult> search(UUID tenantId, String query, String entityType,
                                      UUID programId, String status, int maxResults) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int perTypeLimit = Math.min(maxResults, 50);
        Pageable pageable = PageRequest.of(0, perTypeLimit);
        List<SearchResult> results = new ArrayList<>();

        boolean searchAll = entityType == null || entityType.isBlank();

        if (searchAll || "PROGRAM".equalsIgnoreCase(entityType)) {
            programRepository.search(tenantId, query, pageable).stream()
                    .filter(p -> matchesStatus(p.getStatus().name(), status))
                    .map(p -> toResult("PROGRAM", p.getId(), p.getName(), p.getDescription(),
                            p.getStatus().name(), p.getId(), p.getName()))
                    .forEach(results::add);
        }

        if (searchAll || "WORKSTREAM".equalsIgnoreCase(entityType)) {
            workstreamRepository.search(tenantId, query, pageable).stream()
                    .filter(w -> matchesProgramId(w.getProgramId(), programId))
                    .filter(w -> matchesStatus(w.getStatus().name(), status))
                    .map(w -> toResult("WORKSTREAM", w.getId(), w.getName(), w.getDescription(),
                            w.getStatus().name(), w.getProgramId(), null))
                    .forEach(results::add);
        }

        if (searchAll || "OUTCOME".equalsIgnoreCase(entityType)) {
            outcomeRepository.search(tenantId, query, pageable).stream()
                    .filter(o -> matchesProgramId(o.getProgram() != null ? o.getProgram().getId() : null, programId))
                    .filter(o -> matchesStatus(o.getStatus().name(), status))
                    .map(o -> toResult("OUTCOME", o.getId(), o.getTitle(), o.getDescription(),
                            o.getStatus().name(),
                            o.getProgram() != null ? o.getProgram().getId() : null,
                            o.getProgram() != null ? o.getProgram().getName() : null))
                    .forEach(results::add);
        }

        if (searchAll || "HYPOTHESIS".equalsIgnoreCase(entityType)) {
            hypothesisRepository.search(tenantId, query, pageable).stream()
                    .filter(h -> matchesProgramId(h.getProgram() != null ? h.getProgram().getId() : null, programId))
                    .filter(h -> matchesStatus(h.getStatus().name(), status))
                    .map(h -> toResult("HYPOTHESIS", h.getId(), h.getTitle(), h.getBelief(),
                            h.getStatus().name(),
                            h.getProgram() != null ? h.getProgram().getId() : null,
                            h.getProgram() != null ? h.getProgram().getName() : null))
                    .forEach(results::add);
        }

        if (searchAll || "SPECIFICATION".equalsIgnoreCase(entityType)) {
            specificationRepository.search(tenantId, query, pageable).stream()
                    .filter(s -> matchesProgramId(s.getProgramId(), programId))
                    .filter(s -> matchesStatus(s.getStatus().name(), status))
                    .map(s -> toResult("SPECIFICATION", s.getId(), s.getName(), s.getDescription(),
                            s.getStatus().name(), s.getProgramId(), null))
                    .forEach(results::add);
        }

        if (searchAll || "REQUIREMENT".equalsIgnoreCase(entityType)) {
            requirementRepository.search(tenantId, query, pageable).stream()
                    .filter(r -> matchesProgramId(r.getProgramId(), programId))
                    .filter(r -> matchesStatus(r.getStatus().name(), status))
                    .map(r -> toResult("REQUIREMENT", r.getId(), r.getTitle(), r.getDescription(),
                            r.getStatus().name(), r.getProgramId(), null))
                    .forEach(results::add);
        }

        if (searchAll || "TICKET".equalsIgnoreCase(entityType)) {
            ticketRepository.search(tenantId, query, pageable).stream()
                    .filter(t -> matchesProgramId(t.getProgramId(), programId))
                    .filter(t -> matchesStatus(t.getStatus().name(), status))
                    .map(t -> toResult("TICKET", t.getId(), t.getTitle(), t.getDescription(),
                            t.getStatus().name(), t.getProgramId(), null))
                    .forEach(results::add);
        }

        if (searchAll || "DECISION".equalsIgnoreCase(entityType)) {
            decisionRepository.search(tenantId, query, pageable).stream()
                    .filter(d -> matchesProgramId(d.getProgram() != null ? d.getProgram().getId() : null, programId))
                    .filter(d -> matchesStatus(d.getStatus().name(), status))
                    .map(d -> toResult("DECISION", d.getId(), d.getTitle(), d.getDescription(),
                            d.getStatus().name(),
                            d.getProgram() != null ? d.getProgram().getId() : null,
                            d.getProgram() != null ? d.getProgram().getName() : null))
                    .forEach(results::add);
        }

        if (searchAll || "DOCUMENT".equalsIgnoreCase(entityType)) {
            documentRepository.search(tenantId, query, pageable).stream()
                    .filter(d -> matchesStatus(d.getStatus().name(), status))
                    .map(d -> toResult("DOCUMENT", d.getId(), d.getTitle(), d.getBody(),
                            d.getStatus().name(), null, null))
                    .forEach(results::add);
        }

        // Sort: exact title matches first, then alphabetically
        String queryLower = query.toLowerCase();
        results.sort((a, b) -> {
            boolean aExact = a.title() != null && a.title().toLowerCase().equals(queryLower);
            boolean bExact = b.title() != null && b.title().toLowerCase().equals(queryLower);
            if (aExact && !bExact) return -1;
            if (!aExact && bExact) return 1;
            boolean aStartsWith = a.title() != null && a.title().toLowerCase().startsWith(queryLower);
            boolean bStartsWith = b.title() != null && b.title().toLowerCase().startsWith(queryLower);
            if (aStartsWith && !bStartsWith) return -1;
            if (!aStartsWith && bStartsWith) return 1;
            return 0;
        });

        return results.stream().limit(maxResults).toList();
    }

    private SearchResult toResult(String entityType, UUID entityId, String title, String description,
                                   String status, UUID programId, String programName) {
        String truncatedDesc = truncate(description, 200);
        return new SearchResult(entityType, entityId, title, truncatedDesc, status, programId, programName);
    }

    private boolean matchesProgramId(UUID entityProgramId, UUID filterProgramId) {
        if (filterProgramId == null) return true;
        return filterProgramId.equals(entityProgramId);
    }

    private boolean matchesStatus(String entityStatus, String filterStatus) {
        if (filterStatus == null || filterStatus.isBlank()) return true;
        return filterStatus.equalsIgnoreCase(entityStatus);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
