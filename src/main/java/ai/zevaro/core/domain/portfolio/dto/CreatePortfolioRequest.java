package ai.zevaro.core.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePortfolioRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        UUID ownerId,
        List<String> tags
) {}
