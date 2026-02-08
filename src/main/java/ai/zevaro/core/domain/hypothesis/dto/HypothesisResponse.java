package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.ConfidenceLevel;
import ai.zevaro.core.domain.hypothesis.HypothesisPriority;
import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import ai.zevaro.core.domain.hypothesis.TShirtSize;
import ai.zevaro.core.domain.outcome.dto.OutcomeSummary;
import ai.zevaro.core.domain.program.dto.ProgramSummary;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record HypothesisResponse(
        UUID id,
        OutcomeSummary outcome,
        ProgramSummary program,
        String title,
        String belief,
        String expectedResult,
        String measurementCriteria,
        HypothesisStatus status,
        HypothesisPriority priority,
        TShirtSize effort,
        TShirtSize impact,
        ConfidenceLevel confidence,
        UserSummary owner,
        Map<String, Object> experimentConfig,
        Map<String, Object> experimentResults,
        String blockedReason,
        String conclusionNotes,
        Map<String, String> externalRefs,
        List<String> tags,
        Instant startedAt,
        Instant deployedAt,
        Instant measuringStartedAt,
        Instant concludedAt,
        UserSummary concludedBy,
        Instant createdAt,
        Instant updatedAt
) {}
