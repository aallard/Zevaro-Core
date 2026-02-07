package ai.zevaro.core.domain.experiment.dto;

import java.math.BigDecimal;
import java.util.Map;

public record RecordResultsRequest(
        BigDecimal controlValue,
        BigDecimal variantValue,
        BigDecimal confidenceLevel,
        Integer currentSampleSize,
        Map<String, Object> additionalResults
) {}
