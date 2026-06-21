package pg.net.ai_services.domain.service;

import java.util.List;

import pg.net.ai_services.domain.model.SearchResult;
import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

public class ChatDomainService implements ChatInputPort {

    private static final String NOT_FOUND_MESSAGE = "No he encontrado nada en mi base de datos sobre ese tema.";
    private static final int TOP_K = 5;

    private final ChatOutputPort chatOutputPort;
    private final VectorStoreOutputPort vectorStoreOutputPort;

    public ChatDomainService(ChatOutputPort chatOutputPort, VectorStoreOutputPort vectorStoreOutputPort) {
        this.chatOutputPort = chatOutputPort;
        this.vectorStoreOutputPort = vectorStoreOutputPort;
    }

    @Override
    public String chat(String message) {
        List<SearchResult> results = vectorStoreOutputPort.search(message, TOP_K);
        if (results.isEmpty()) {
            return NOT_FOUND_MESSAGE;
        }
        return chatOutputPort.sendMessage(buildPrompt(message, results));
    }

    private String buildPrompt(String message, List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente que responde basándose exclusivamente en la información proporcionada.\n");
        sb.append("Si la información no es suficiente para responder con precisión, indícalo.\n\n");
        sb.append("Información disponible:\n");
        for (SearchResult result : results) {
            if (!result.contexto().isBlank()) {
                sb.append("[Fuente: ").append(result.contexto()).append("]\n");
            }
            sb.append(result.texto()).append("\n\n");
        }
        sb.append("Pregunta: ").append(message);
        return sb.toString();
    }
}
