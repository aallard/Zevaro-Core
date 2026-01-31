package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateKeyResultRequest;
import ai.zevaro.core.domain.outcome.dto.KeyResultResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateKeyResultRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class KeyResultMapper {

    public KeyResult toEntity(CreateKeyResultRequest request, Outcome outcome) {
        return KeyResult.builder()
                .outcome(outcome)
                .title(request.title())
                .description(request.description())
                .targetValue(request.targetValue())
                .currentValue(request.currentValue() != null ? request.currentValue() : BigDecimal.ZERO)
                .unit(request.unit())
                .build();
    }

    public void updateEntity(KeyResult keyResult, UpdateKeyResultRequest request) {
        if (request.title() != null) {
            keyResult.setTitle(request.title());
        }
        if (request.description() != null) {
            keyResult.setDescription(request.description());
        }
        if (request.targetValue() != null) {
            keyResult.setTargetValue(request.targetValue());
        }
        if (request.currentValue() != null) {
            keyResult.setCurrentValue(request.currentValue());
        }
        if (request.unit() != null) {
            keyResult.setUnit(request.unit());
        }
    }

    public KeyResultResponse toResponse(KeyResult keyResult) {
        if (keyResult == null) {
            return null;
        }
        return new KeyResultResponse(
                keyResult.getId(),
                keyResult.getOutcome() != null ? keyResult.getOutcome().getId() : null,
                keyResult.getTitle(),
                keyResult.getDescription(),
                keyResult.getTargetValue(),
                keyResult.getCurrentValue(),
                keyResult.getUnit(),
                keyResult.getProgressPercent(),
                keyResult.getCreatedAt(),
                keyResult.getUpdatedAt()
        );
    }
}
