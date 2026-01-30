package ai.zevaro.core.domain.auth;

import ai.zevaro.core.domain.auth.dto.AuthResponse;
import ai.zevaro.core.domain.auth.dto.LoginRequest;
import ai.zevaro.core.domain.auth.dto.RefreshTokenRequest;
import ai.zevaro.core.domain.auth.dto.RegisterRequest;
import ai.zevaro.core.domain.tenant.Tenant;
import ai.zevaro.core.domain.tenant.TenantRepository;
import ai.zevaro.core.domain.user.Role;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.tenantName());
        tenant = tenantRepository.save(tenant);

        User user = new User();
        user.setTenantId(tenant.getId());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(Role.OWNER);

        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);

        user = userRepository.save(user);

        return buildAuthResponse(user);
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

        if (!request.refreshToken().equals(user.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = user.getRefreshToken();
        if (refreshToken == null) {
            refreshToken = jwtTokenProvider.generateRefreshToken(user);
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getJwtExpiration(),
                new AuthResponse.UserInfo(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole().name()
                )
        );
    }
}
