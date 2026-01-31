package ai.zevaro.core.domain.auth;

import ai.zevaro.core.domain.auth.dto.AuthResponse;
import ai.zevaro.core.domain.auth.dto.LoginRequest;
import ai.zevaro.core.domain.auth.dto.RefreshTokenRequest;
import ai.zevaro.core.domain.auth.dto.RegisterRequest;
import ai.zevaro.core.domain.rbac.Role;
import ai.zevaro.core.domain.rbac.RoleRepository;
import ai.zevaro.core.domain.tenant.Tenant;
import ai.zevaro.core.domain.tenant.TenantRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import ai.zevaro.core.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        userRepository.updateLastLogin(user.getId(), Instant.now());

        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        return buildAuthResponse(user, refreshToken);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String slug = generateSlug(request.tenantName());
        if (tenantRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Organization name already taken");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.tenantName());
        tenant.setSlug(slug);
        tenant = tenantRepository.save(tenant);

        Role superAdminRole = roleRepository.findByCodeAndTenantIdIsNull("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found. Run data loaders first."));

        User user = new User();
        user.setTenantId(tenant.getId());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(superAdminRole);
        user.setActive(true);

        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(passwordEncoder.encode(refreshToken));

        user = userRepository.save(user);

        return buildAuthResponse(user, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Claims claims = jwtTokenProvider.validateToken(request.refreshToken());
        if (claims == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Invalid token type");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRefreshToken() == null || !passwordEncoder.matches(request.refreshToken(), user.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(passwordEncoder.encode(newRefreshToken));
        userRepository.save(user);

        return buildAuthResponse(user, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String rawRefreshToken) {
        String accessToken = jwtTokenProvider.generateToken(user);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                jwtTokenProvider.getJwtExpiration(),
                new AuthResponse.UserInfo(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().getCode()
                )
        );
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
