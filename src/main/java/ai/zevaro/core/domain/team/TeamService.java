package ai.zevaro.core.domain.team;

import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.stakeholder.StakeholderRepository;
import ai.zevaro.core.domain.team.dto.AddTeamMemberRequest;
import ai.zevaro.core.domain.team.dto.CreateTeamRequest;
import ai.zevaro.core.domain.team.dto.TeamDetailResponse;
import ai.zevaro.core.domain.team.dto.TeamMemberResponse;
import ai.zevaro.core.domain.team.dto.TeamResponse;
import ai.zevaro.core.domain.team.dto.TeamWorkloadResponse;
import ai.zevaro.core.domain.team.dto.TeamWorkloadResponse.MemberWorkload;
import ai.zevaro.core.domain.team.dto.UpdateTeamMemberRequest;
import ai.zevaro.core.domain.team.dto.UpdateTeamRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
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
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final SlugGenerator slugGenerator;
    private final DecisionRepository decisionRepository;
    private final HypothesisRepository hypothesisRepository;
    private final StakeholderRepository stakeholderRepository;

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams(UUID tenantId) {
        return teamRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeamsPaged(UUID tenantId, Pageable pageable) {
        return teamRepository.findByTenantId(tenantId, pageable)
                .map(teamMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamById(UUID id, UUID tenantId) {
        Team team = teamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return teamMapper.toDetailResponse(team);
    }

    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamBySlug(String slug, UUID tenantId) {
        Team team = teamRepository.findBySlugAndTenantId(slug, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "slug", slug));
        return teamMapper.toDetailResponse(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsForUser(UUID userId, UUID tenantId) {
        return teamRepository.findByMemberUserId(userId, tenantId).stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Transactional
    public TeamResponse createTeam(UUID tenantId, CreateTeamRequest request, UUID createdById) {
        String slug = slugGenerator.generateUniqueSlug(
                request.name(),
                s -> teamRepository.existsBySlugAndTenantId(s, tenantId)
        );

        Team team = new Team();
        team.setTenantId(tenantId);
        team.setName(request.name());
        team.setSlug(slug);
        team.setDescription(request.description());
        team.setIconUrl(request.iconUrl());
        team.setColor(request.color());
        team.setActive(true);

        if (request.leadId() != null) {
            User lead = userRepository.findByIdAndTenantId(request.leadId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.leadId()));
            team.setLead(lead);
        }

        team = teamRepository.save(team);
        return teamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(UUID id, UUID tenantId, UpdateTeamRequest request) {
        Team team = teamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        if (request.name() != null) {
            team.setName(request.name());
            String currentSlug = team.getSlug();
            String newSlug = slugGenerator.generateUniqueSlug(
                    request.name(),
                    s -> !s.equals(currentSlug) && teamRepository.existsBySlugAndTenantId(s, tenantId)
            );
            team.setSlug(newSlug);
        }
        if (request.description() != null) {
            team.setDescription(request.description());
        }
        if (request.iconUrl() != null) {
            team.setIconUrl(request.iconUrl());
        }
        if (request.color() != null) {
            team.setColor(request.color());
        }
        if (request.leadId() != null) {
            User lead = userRepository.findByIdAndTenantId(request.leadId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.leadId()));
            team.setLead(lead);
        }
        if (request.active() != null) {
            team.setActive(request.active());
        }

        team = teamRepository.save(team);
        return teamMapper.toResponse(team);
    }

    @Transactional
    public void deleteTeam(UUID id, UUID tenantId) {
        Team team = teamRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        team.setActive(false);
        teamRepository.save(team);
    }

    @Transactional
    public TeamMemberResponse addMember(UUID teamId, UUID tenantId, AddTeamMemberRequest request, UUID addedById) {
        Team team = teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        User user = userRepository.findByIdAndTenantId(request.userId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.userId())) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        TeamMemberRole role = request.role() != null ? request.role() : TeamMemberRole.MEMBER;

        TeamMember member = new TeamMember(team, user, role);
        member.setAddedById(addedById);
        member = teamMemberRepository.save(member);

        return teamMapper.toMemberResponse(member);
    }

    @Transactional
    public TeamMemberResponse updateMember(UUID teamId, UUID userId, UUID tenantId, UpdateTeamMemberRequest request) {
        teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "userId", userId));

        member.setTeamRole(request.role());
        member = teamMemberRepository.save(member);

        return teamMapper.toMemberResponse(member);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, UUID tenantId) {
        teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new ResourceNotFoundException("TeamMember", "userId", userId);
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getMembers(UUID teamId, UUID tenantId) {
        teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(teamMapper::toMemberResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamWorkloadResponse getTeamWorkload(UUID teamId, UUID tenantId) {
        Team team = teamRepository.findByIdAndTenantId(teamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);

        // Count total stakeholders (not directly associated with team, so 0)
        // Stakeholders are associated with Projects, not Teams
        long totalStakeholders = 0;

        // Calculate pending decisions across team
        long pendingDecisionsAcrossTeam = members.stream()
                .mapToLong(m -> decisionRepository.countPendingForAssignee(tenantId, m.getUser().getId()))
                .sum();

        // Build member workload list
        List<MemberWorkload> memberWorkloads = new ArrayList<>();
        for (TeamMember member : members) {
            UUID userId = member.getUser().getId();

            // Count pending decisions
            long decisionsPending = decisionRepository.countPendingForAssignee(tenantId, userId);

            // Calculate average response time from resolved decisions
            List<ai.zevaro.core.domain.decision.Decision> recentResolved =
                decisionRepository.findRecentResolvedForAssignee(tenantId, userId);
            double avgResponseTimeHours = 0;
            if (!recentResolved.isEmpty()) {
                avgResponseTimeHours = recentResolved.stream()
                        .mapToDouble(d -> {
                            if (d.getCreatedAt() != null && d.getDecidedAt() != null) {
                                return Duration.between(d.getCreatedAt(), d.getDecidedAt()).toMinutes() / 60.0;
                            }
                            return 0;
                        })
                        .average()
                        .orElse(0);
            }

            // Count hypotheses owned
            long hypothesesOwned = hypothesisRepository.countOwnedByUser(tenantId, userId);

            memberWorkloads.add(new MemberWorkload(
                userId,
                member.getUser().getFullName(),
                member.getUser().getEmail(),
                member.getUser().getAvatarUrl(),
                member.getTeamRole().toString(),
                (int) decisionsPending,
                avgResponseTimeHours,
                (int) hypothesesOwned,
                member.getUser().isActive()
            ));
        }

        return new TeamWorkloadResponse(
            teamId,
            team.getName(),
            members.size(),
            (int) totalStakeholders,
            (int) pendingDecisionsAcrossTeam,
            memberWorkloads
        );
    }
}
