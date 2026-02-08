package ai.zevaro.core.domain.portfolio;

import ai.zevaro.core.domain.portfolio.dto.CreatePortfolioRequest;
import ai.zevaro.core.domain.portfolio.dto.PortfolioResponse;
import ai.zevaro.core.domain.portfolio.dto.UpdatePortfolioRequest;
import ai.zevaro.core.util.SlugGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioMapper {

    private final ObjectMapper objectMapper;
    private final SlugGenerator slugGenerator;

    public Portfolio toEntity(CreatePortfolioRequest request, UUID tenantId, UUID createdById) {
        Portfolio portfolio = new Portfolio();
        portfolio.setTenantId(tenantId);
        portfolio.setName(request.name());
        portfolio.setSlug(slugGenerator.generateSlug(request.name()));
        portfolio.setDescription(request.description());
        portfolio.setStatus(PortfolioStatus.ACTIVE);
        portfolio.setOwnerId(request.ownerId());
        portfolio.setTags(listToJson(request.tags()));
        portfolio.setCreatedById(createdById);
        return portfolio;
    }

    public PortfolioResponse toResponse(Portfolio portfolio, int programCount, String ownerName) {
        if (portfolio == null) {
            return null;
        }

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getSlug(),
                portfolio.getDescription(),
                portfolio.getStatus(),
                portfolio.getOwnerId(),
                ownerName,
                parseJsonToList(portfolio.getTags()),
                programCount,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    public void updateEntity(Portfolio portfolio, UpdatePortfolioRequest request) {
        if (request.name() != null) {
            portfolio.setName(request.name());
        }
        if (request.description() != null) {
            portfolio.setDescription(request.description());
        }
        if (request.status() != null) {
            portfolio.setStatus(request.status());
        }
        if (request.ownerId() != null) {
            portfolio.setOwnerId(request.ownerId());
        }
        if (request.tags() != null) {
            portfolio.setTags(listToJson(request.tags()));
        }
    }

    private String listToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert list to JSON", e);
            return null;
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to list: {}", json, e);
            return null;
        }
    }
}
