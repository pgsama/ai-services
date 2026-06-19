package pg.net.ai_services.domain.service;

import org.junit.jupiter.api.Test;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatDomainServiceTest {

    private final ChatOutputPort chatOutputPort = mock(ChatOutputPort.class);
    private final VectorStoreOutputPort vectorStoreOutputPort = mock(VectorStoreOutputPort.class);
    private final ChatDomainService service = new ChatDomainService(chatOutputPort, vectorStoreOutputPort);

    @Test
    void returnsNotFoundMessageWhenVectorStoreEmpty() {
        when(vectorStoreOutputPort.search(anyString(), anyInt())).thenReturn(List.of());

        String result = service.chat("¿qué es Java?");

        assertThat(result).isEqualTo("No he encontrado nada en mi base de datos sobre ese tema.");
        verify(chatOutputPort, never()).sendMessage(anyString());
    }

    @Test
    void sendsEnrichedPromptWhenContextFound() {
        when(vectorStoreOutputPort.search("¿qué es Java?", 3))
                .thenReturn(List.of("Java es un lenguaje.", "Fue creado en 1995."));
        when(chatOutputPort.sendMessage(anyString())).thenReturn("Respuesta del modelo");

        String result = service.chat("¿qué es Java?");

        assertThat(result).isEqualTo("Respuesta del modelo");
        verify(chatOutputPort).sendMessage(contains("Java es un lenguaje."));
        verify(chatOutputPort).sendMessage(contains("Fue creado en 1995."));
        verify(chatOutputPort).sendMessage(contains("¿qué es Java?"));
    }
}
