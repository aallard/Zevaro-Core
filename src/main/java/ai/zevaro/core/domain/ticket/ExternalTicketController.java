package ai.zevaro.core.domain.ticket;

import ai.zevaro.core.domain.ticket.dto.CreateTicketRequest;
import ai.zevaro.core.domain.ticket.dto.ExternalTicketRequest;
import ai.zevaro.core.domain.ticket.dto.TicketResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/external/tickets")
@Tag(name = "External Tickets", description = "External ticket ingestion")
@RequiredArgsConstructor
public class ExternalTicketController {

    private final TicketService ticketService;

    @Value("${zevaro.external.api-key:default-dev-key-change-in-production}")
    private String configuredApiKey;

    @Value("${zevaro.external.system-user-id:00000000-0000-0000-0000-000000000000}")
    private String systemUserId;

    @PostMapping
    public ResponseEntity<?> createTicket(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody ExternalTicketRequest request) {

        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid or missing API key\"}");
        }

        UUID reportedById = UUID.fromString(systemUserId);
        TicketSource source = request.source() != null ? request.source() : TicketSource.API;

        CreateTicketRequest ticketRequest = new CreateTicketRequest(
                request.title(),
                request.description(),
                request.type(),
                request.severity(),
                null,
                request.environment(),
                null, null, null,
                source,
                request.externalRef(),
                null
        );

        TicketResponse response = ticketService.create(
                request.workstreamId(), ticketRequest, request.tenantId(), reportedById);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
