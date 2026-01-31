package ai.zevaro.core.domain.user;

import ai.zevaro.core.domain.rbac.RoleMapper;
import ai.zevaro.core.domain.user.dto.UserResponse;
import ai.zevaro.core.domain.user.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getTitle(),
                user.getDepartment(),
                user.getAvatarUrl(),
                roleMapper.toResponse(user.getRole()),
                user.getManager() != null ? toSummary(user.getManager()) : null,
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    public UserSummary toSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(
                user.getId(),
                user.getFullName(),
                user.getTitle(),
                user.getAvatarUrl()
        );
    }
}
