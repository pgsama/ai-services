package pg.net.ai_services.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.in.IngestInputPort;
import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.EncryptionOutputPort;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;
import pg.net.ai_services.domain.service.ChatDomainService;
import pg.net.ai_services.domain.service.IngestDomainService;
import pg.net.ai_services.domain.service.UsuarioDomainService;

@Configuration
public class DomainConfig {

    @Bean
    ChatInputPort chatInputPort(ChatOutputPort chatOutputPort) {
        return new ChatDomainService(chatOutputPort);
    }

    @Bean
    UsuarioInputPort usuarioInputPort(UsuarioOutputPort usuarioOutputPort,
                                      EncryptionOutputPort encryptionOutputPort) {
        return new UsuarioDomainService(usuarioOutputPort, encryptionOutputPort);
    }

    @Bean
    IngestInputPort ingestInputPort(VectorStoreOutputPort vectorStoreOutputPort) {
        return new IngestDomainService(vectorStoreOutputPort);
    }
}
