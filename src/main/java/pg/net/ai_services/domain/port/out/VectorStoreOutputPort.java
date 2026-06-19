package pg.net.ai_services.domain.port.out;

import java.util.List;
import java.util.Map;

public interface VectorStoreOutputPort {
    String store(String texto, Map<String, String> metadata);
    List<String> search(String query, int topK);
}
