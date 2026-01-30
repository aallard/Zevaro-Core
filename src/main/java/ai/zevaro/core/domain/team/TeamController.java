package ai.zevaro.core.domain.team;

import ai.zevaro.core.domain.team.dto.AddTeamMemberRequest;
import ai.zevaro.core.domain.team.dto.CreateTeamRequest;
import ai.zevaro.core.domain.team.dto.TeamDetailResponse;
import ai.zevaro.core.domain.team.dto.TeamMemberResponse;
import ai.zevaro.core.domain.team.dto.TeamResponse;
import ai.zevaro.core.domain.team.dto.UpdateTeamMemberRequest;
import ai.zevaro.core.domain.team.dto.UpdateTeamRequest;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:read')")
    public ResponseEntity<List<TeamResponse>> getTeams(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.getTeams(user.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:read')")
    public ResponseEntity<TeamDetailResponse> getTeam(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.getTeamById(id, user.getTenantId()));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:read')")
    public ResponseEntity<TeamDetailResponse> getTeamBySlug(
            @PathVariable String slug,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.getTeamBySlug(slug, user.getTenantId()));
    }

    @GetMapping("/my-teams")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamResponse>> getMyTeams(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.getTeamsForUser(user.getUserId(), user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:create')")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @CurrentUser UserPrincipal user) {
        TeamResponse team = teamService.createTeam(user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:update')")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.updateTeam(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:delete')")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        teamService.deleteTeam(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:read')")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.getMembers(id, user.getTenantId()));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:manage_members')")
    public ResponseEntity<TeamMemberResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddTeamMemberRequest request,
            @CurrentUser UserPrincipal user) {
        TeamMemberResponse member = teamService.addMember(id, user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @PutMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:manage_members')")
    public ResponseEntity<TeamMemberResponse> updateMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateTeamMemberRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(teamService.updateMember(id, userId, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('team:manage_members')")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @CurrentUser UserPrincipal user) {
        teamService.removeMember(id, userId, user.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
