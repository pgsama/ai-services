package pg.net.ai_services.domain.service;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;

public class ChatDomainService implements ChatInputPort {

    private final ChatOutputPort chatOutputPort;

    public ChatDomainService(ChatOutputPort chatOutputPort) {
        this.chatOutputPort = chatOutputPort;
    }

    @Override
    public String chat(String message) {
        return chatOutputPort.sendMessage(message);
    }
}
