package pg.net.ai_services.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDto(
        @NotBlank String usuario,
        @NotBlank @Size(min = 6) String password
) {
}
