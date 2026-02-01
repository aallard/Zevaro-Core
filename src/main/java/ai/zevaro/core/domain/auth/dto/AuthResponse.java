package ai.zevaro.core.domain.auth.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String email,
            String firstName,
            String lastName,
            String tenantId,
            List<String> roles,
            List<String> permissions
    ) {}
}
