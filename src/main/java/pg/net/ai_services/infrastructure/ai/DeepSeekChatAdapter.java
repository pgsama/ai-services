package pg.net.ai_services.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import pg.net.ai_services.domain.port.out.ChatOutputPort;

@Component
public class DeepSeekChatAdapter implements ChatOutputPort {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekChatAdapter.class);

    private final ChatClient chatClient;

    public DeepSeekChatAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String sendMessage(String message) {
        log.info("deepseek request message_length={}", message.length());
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        log.info("deepseek response received");
        return response;
    }
}
