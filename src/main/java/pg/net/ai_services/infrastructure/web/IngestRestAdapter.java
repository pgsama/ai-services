package pg.net.ai_services.infrastructure.web;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pg.net.ai_services.domain.port.in.IngestInputPort;
import pg.net.ai_services.infrastructure.web.dto.IngestRequestDto;
import pg.net.ai_services.infrastructure.web.dto.IngestResponseDto;

@Tag(name = "Ingest")
@RestController
@RequestMapping("/api/ingest")
public class IngestRestAdapter {

    private static final Logger log = LoggerFactory.getLogger(IngestRestAdapter.class);

    private final IngestInputPort ingestInputPort;

    public IngestRestAdapter(IngestInputPort ingestInputPort) {
        this.ingestInputPort = ingestInputPort;
    }

    @Operation(summary = "Embed and store text in pgvector")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestResponseDto ingest(@Valid @RequestBody IngestRequestDto request) {
        log.info("POST /api/ingest texto_length={}", request.texto().length());
        Map<String, String> metadata = request.metadata() != null ? request.metadata() : Map.of();
        String id = ingestInputPort.ingest(request.texto(), metadata);
        log.info("ingest completed id={}", id);
        return new IngestResponseDto(id);
    }
}
