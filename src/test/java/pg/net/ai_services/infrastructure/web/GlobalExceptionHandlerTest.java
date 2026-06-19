package pg.net.ai_services.infrastructure.web;

import org.junit.jupiter.api.Test;
import pg.net.ai_services.infrastructure.web.dto.ApiErrorDto;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentReturns400WithMessage() {
        ApiErrorDto result = handler.handleIllegalArgument(
                new IllegalArgumentException("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf."));

        assertThat(result.status()).isEqualTo(400);
        assertThat(result.message()).isEqualTo("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf.");
    }
}
