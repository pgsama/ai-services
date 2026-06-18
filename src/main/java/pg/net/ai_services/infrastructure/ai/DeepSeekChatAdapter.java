package pg.net.ai_services.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import pg.net.ai_services.domain.port.out.ChatOutputPort;

@Component
public class DeepSeekChatAdapter implements ChatOutputPort {

    private final ChatClient chatClient;

    public DeepSeekChatAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
