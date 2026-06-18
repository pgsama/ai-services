package pg.net.ai_services.domain.port.out;

import java.util.Map;

public interface VectorStoreOutputPort {
    String store(String texto, Map<String, String> metadata);
}
