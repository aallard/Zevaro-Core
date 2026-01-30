package ai.zevaro.core.domain.team;

import ai.zevaro.core.domain.team.dto.TeamDetailResponse;
import ai.zevaro.core.domain.team.dto.TeamMemberResponse;
import ai.zevaro.core.domain.team.dto.TeamResponse;
import ai.zevaro.core.domain.team.dto.TeamSummary;
import ai.zevaro.core.domain.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamMapper {

    private final UserMapper userMapper;

    public TeamResponse toResponse(Team team) {
        if (team == null) {
            return null;
        }
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getSlug(),
                team.getDescription(),
                team.getIconUrl(),
                team.getColor(),
                team.getLead() != null ? userMapper.toSummary(team.getLead()) : null,
                team.getMembers() != null ? team.getMembers().size() : 0,
                team.isActive(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    public TeamDetailResponse toDetailResponse(Team team) {
        if (team == null) {
            return null;
        }
        List<TeamMemberResponse> memberResponses = team.getMembers() != null
                ? team.getMembers().stream()
                        .map(this::toMemberResponse)
                        .toList()
                : List.of();

        return new TeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getSlug(),
                team.getDescription(),
                team.getIconUrl(),
                team.getColor(),
                team.getLead() != null ? userMapper.toSummary(team.getLead()) : null,
                memberResponses,
                team.isActive(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    public TeamSummary toSummary(Team team) {
        if (team == null) {
            return null;
        }
        return new TeamSummary(
                team.getId(),
                team.getName(),
                team.getSlug(),
                team.getColor()
        );
    }

    public TeamMemberResponse toMemberResponse(TeamMember member) {
        if (member == null) {
            return null;
        }
        return new TeamMemberResponse(
                member.getId(),
                userMapper.toSummary(member.getUser()),
                member.getTeamRole(),
                member.getJoinedAt()
        );
    }
}
