package ai.zevaro.core.domain.experiment;

import ai.zevaro.core.domain.experiment.dto.CreateExperimentRequest;
import ai.zevaro.core.domain.experiment.dto.ExperimentResponse;
import ai.zevaro.core.domain.experiment.dto.RecordResultsRequest;
import ai.zevaro.core.domain.experiment.dto.UpdateExperimentRequest;
import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final HypothesisRepository hypothesisRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ExperimentMapper experimentMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ExperimentResponse> listExperiments(UUID tenantId, UUID projectId, ExperimentStatus status) {
        List<Experiment> experiments;

        if (projectId != null) {
            if (status != null) {
                experiments = experimentRepository.findByTenantIdAndProgramIdAndStatus(tenantId, projectId, status);
            } else {
                experiments = experimentRepository.findByTenantIdAndProgramId(tenantId, projectId);
            }
        } else if (status != null) {
            experiments = experimentRepository.findByTenantIdAndStatus(tenantId, status);
        } else {
            experiments = experimentRepository.findByTenantId(tenantId);
        }

        return experiments.stream()
                .map(experimentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ExperimentResponse> listExperimentsPaged(UUID tenantId, UUID projectId, ExperimentStatus status, Pageable pageable) {
        Page<Experiment> experiments;

        if (projectId != null) {
            if (status != null) {
                experiments = experimentRepository.findByTenantIdAndStatus(tenantId, status, pageable);
            } else {
                experiments = experimentRepository.findByTenantIdAndProgramId(tenantId, projectId, pageable);
            }
        } else if (status != null) {
            experiments = experimentRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else {
            experiments = experimentRepository.findByTenantId(tenantId, pageable);
        }

        return experiments.map(experimentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ExperimentResponse getExperiment(UUID id, UUID tenantId) {
        Experiment experiment = experimentRepository.findByIdAndTenantIdWithDetails(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));
        return experimentMapper.toResponse(experiment);
    }

    @Transactional(readOnly = true)
    public List<ExperimentResponse> getExperimentsByHypothesis(UUID hypothesisId, UUID tenantId) {
        return experimentRepository.findByHypothesisId(hypothesisId).stream()
                .filter(e -> e.getTenantId().equals(tenantId))
                .map(experimentMapper::toResponse)
                .toList();
    }

    @Transactional
    public ExperimentResponse createExperiment(UUID tenantId, CreateExperimentRequest request, UUID createdById) {
        Experiment experiment = experimentMapper.toEntity(request, tenantId, createdById);

        if (request.projectId() != null) {
            Program program = programRepository.findByIdAndTenantId(request.projectId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.projectId()));
            experiment.setProgram(program);
        }

        if (request.hypothesisId() != null) {
            Hypothesis hypothesis = hypothesisRepository.findById(request.hypothesisId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", request.hypothesisId()));
            experiment.setHypothesis(hypothesis);
        }

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            experiment.setOwner(owner);
        }

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse updateExperiment(UUID id, UUID tenantId, UpdateExperimentRequest request) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        experimentMapper.updateEntity(experiment, request);

        if (request.projectId() != null) {
            Program program = programRepository.findByIdAndTenantId(request.projectId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.projectId()));
            experiment.setProgram(program);
        }

        if (request.hypothesisId() != null) {
            Hypothesis hypothesis = hypothesisRepository.findById(request.hypothesisId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", request.hypothesisId()));
            experiment.setHypothesis(hypothesis);
        }

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            experiment.setOwner(owner);
        }

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse startExperiment(UUID id, UUID tenantId) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        if (experiment.getStatus() != ExperimentStatus.DRAFT) {
            throw new IllegalStateException("Can only start experiments in DRAFT status");
        }

        if (experiment.getDurationDays() == null || experiment.getDurationDays() <= 0) {
            throw new IllegalArgumentException("Duration days must be set before starting experiment");
        }

        experiment.setStatus(ExperimentStatus.RUNNING);
        experiment.setStartDate(Instant.now());
        experiment.setEndDate(Instant.now().plusSeconds((long) experiment.getDurationDays() * 86400));

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse concludeExperiment(UUID id, UUID tenantId, String conclusion) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        if (experiment.getStatus() != ExperimentStatus.RUNNING) {
            throw new IllegalStateException("Can only conclude experiments in RUNNING status");
        }

        experiment.setStatus(ExperimentStatus.CONCLUDED);
        experiment.setEndDate(Instant.now());
        experiment.setConclusion(conclusion);

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse cancelExperiment(UUID id, UUID tenantId) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        experiment.setStatus(ExperimentStatus.CANCELLED);
        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse extendExperiment(UUID id, UUID tenantId, Integer additionalDays) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        if (additionalDays == null || additionalDays <= 0) {
            throw new IllegalArgumentException("Additional days must be greater than 0");
        }

        if (experiment.getEndDate() == null) {
            throw new IllegalStateException("Experiment end date must be set before extending");
        }

        experiment.setEndDate(experiment.getEndDate().plusSeconds((long) additionalDays * 86400));
        experiment.setDurationDays(experiment.getDurationDays() + additionalDays);

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public ExperimentResponse recordResults(UUID id, UUID tenantId, RecordResultsRequest request) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));

        if (request.controlValue() != null) {
            experiment.setControlValue(request.controlValue());
        }
        if (request.variantValue() != null) {
            experiment.setVariantValue(request.variantValue());
        }
        if (request.confidenceLevel() != null) {
            experiment.setConfidenceLevel(request.confidenceLevel());
        }
        if (request.currentSampleSize() != null) {
            experiment.setCurrentSampleSize(request.currentSampleSize());
        }

        if (request.additionalResults() != null) {
            try {
                experiment.setResults(objectMapper.writeValueAsString(request.additionalResults()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid results format", e);
            }
        }

        experiment = experimentRepository.save(experiment);
        return experimentMapper.toResponse(experiment);
    }

    @Transactional
    public void deleteExperiment(UUID id, UUID tenantId) {
        Experiment experiment = experimentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment", "id", id));
        experimentRepository.delete(experiment);
    }
}
