package ai.zevaro.core.security;

import ai.zevaro.core.domain.rbac.Permission;
import ai.zevaro.core.domain.rbac.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String email;
    private final String password;
    private final Role role;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID userId, UUID tenantId, String email, String password, Role role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.permissions = role != null ? role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet()) : Set.of();
        this.authorities = buildAuthorities();
    }

    public UserPrincipal(UUID userId, UUID tenantId, String email, String roleCode, Set<String> permissions) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.password = null;
        this.role = null;
        this.permissions = permissions != null ? permissions : Set.of();

        Set<GrantedAuthority> auths = new HashSet<>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
        this.permissions.forEach(p -> auths.add(new SimpleGrantedAuthority(p)));
        this.authorities = auths;
    }

    private Collection<? extends GrantedAuthority> buildAuthorities() {
        Set<GrantedAuthority> auths = new HashSet<>();
        if (role != null) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            role.getPermissions().forEach(p ->
                    auths.add(new SimpleGrantedAuthority(p.getCode())));
        }
        return auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getRoleCode() {
        return role != null ? role.getCode() : null;
    }
}
