package ai.zevaro.core.domain.decision.dto;

import java.util.List;
import java.util.Map;

public record DecisionOption(
        String id,
        String title,
        String description,
        List<String> pros,
        List<String> cons,
        Map<String, Object> metadata
) {}
