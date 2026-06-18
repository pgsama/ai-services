package pg.net.ai_services.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(@NotBlank String message) {
}
