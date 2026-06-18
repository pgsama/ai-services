package pg.net.ai_services.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record IngestRequestDto(@NotBlank String texto, Map<String, String> metadata) {
}
