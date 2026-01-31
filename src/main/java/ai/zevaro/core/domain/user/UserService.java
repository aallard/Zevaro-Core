package ai.zevaro.core.domain.user;

import ai.zevaro.core.domain.rbac.Role;
import ai.zevaro.core.domain.rbac.RoleRepository;
import ai.zevaro.core.domain.user.dto.UpdateUserRequest;
import ai.zevaro.core.domain.user.dto.UserResponse;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(UUID tenantId) {
        return userRepository.findByTenantId(tenantId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartment(UUID tenantId, String department) {
        return userRepository.findByTenantIdAndDepartment(tenantId, department).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getDirectReports(UUID managerId) {
        return userRepository.findByManagerId(managerId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(UUID id, UUID tenantId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.title() != null) {
            user.setTitle(request.title());
        }
        if (request.department() != null) {
            user.setDepartment(request.department());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.roleId()));
            user.setRole(role);
        }
        if (request.managerId() != null) {
            User manager = userRepository.findByIdAndTenantId(request.managerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.managerId()));
            user.setManager(manager);
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse assignRole(UUID userId, UUID roleId, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.setRole(role);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deactivateUser(UUID id, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        userRepository.save(user);
    }
}
