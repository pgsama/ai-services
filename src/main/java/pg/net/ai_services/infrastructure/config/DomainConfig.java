package pg.net.ai_services.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;
import pg.net.ai_services.domain.service.ChatDomainService;
import pg.net.ai_services.domain.service.UsuarioDomainService;

@Configuration
public class DomainConfig {

    @Bean
    ChatInputPort chatInputPort(ChatOutputPort chatOutputPort) {
        return new ChatDomainService(chatOutputPort);
    }

    @Bean
    UsuarioInputPort usuarioInputPort(UsuarioOutputPort usuarioOutputPort) {
        return new UsuarioDomainService(usuarioOutputPort);
    }
}
