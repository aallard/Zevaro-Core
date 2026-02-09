package ai.zevaro.core.domain.auth;

import ai.zevaro.core.domain.auth.dto.AuthResponse;
import ai.zevaro.core.domain.auth.dto.LoginRequest;
import ai.zevaro.core.domain.auth.dto.RefreshTokenRequest;
import ai.zevaro.core.domain.auth.dto.RegisterRequest;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.RateLimiterService;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryConsumeLogin(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryConsumeGeneral(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.tryConsumeGeneral(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser UserPrincipal currentUser) {
        authService.logout(currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
