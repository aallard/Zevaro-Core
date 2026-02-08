package ai.zevaro.core.domain.template.dto;

import java.util.List;
import java.util.UUID;

public record ApplyTemplateResponse(
        UUID programId,
        String programName,
        int workstreamsCreated,
        List<String> workstreamNames
) {}
