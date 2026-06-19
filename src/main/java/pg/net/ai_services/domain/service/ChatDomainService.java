package pg.net.ai_services.domain.service;

import java.util.List;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

public class ChatDomainService implements ChatInputPort {

    private static final String NOT_FOUND_MESSAGE = "No he encontrado nada en mi base de datos sobre ese tema.";
    private static final int TOP_K = 3;

    private final ChatOutputPort chatOutputPort;
    private final VectorStoreOutputPort vectorStoreOutputPort;

    public ChatDomainService(ChatOutputPort chatOutputPort, VectorStoreOutputPort vectorStoreOutputPort) {
        this.chatOutputPort = chatOutputPort;
        this.vectorStoreOutputPort = vectorStoreOutputPort;
    }

    @Override
    public String chat(String message) {
        List<String> context = vectorStoreOutputPort.search(message, TOP_K);
        if (context.isEmpty()) {
            return NOT_FOUND_MESSAGE;
        }
        return chatOutputPort.sendMessage(buildPrompt(message, context));
    }

    private String buildPrompt(String message, List<String> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Usa el siguiente contexto para responder la pregunta.\n\nContexto:\n");
        for (String fragment : context) {
            sb.append(fragment).append("\n");
        }
        sb.append("\nPregunta: ").append(message);
        return sb.toString();
    }
}
