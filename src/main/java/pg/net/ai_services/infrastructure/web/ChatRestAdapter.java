package pg.net.ai_services.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.infrastructure.web.dto.ChatRequestDto;
import pg.net.ai_services.infrastructure.web.dto.ChatResponseDto;

@Tag(name = "Chat")
@RestController
@RequestMapping("/api/chat")
public class ChatRestAdapter {

    private final ChatInputPort chatInputPort;

    public ChatRestAdapter(ChatInputPort chatInputPort) {
        this.chatInputPort = chatInputPort;
    }

    @Operation(summary = "Send a message to DeepSeek and get a reply")
    @PostMapping
    public ChatResponseDto chat(@RequestBody ChatRequestDto request) {
        return new ChatResponseDto(chatInputPort.chat(request.message()));
    }
}
