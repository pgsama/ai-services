package pg.net.ai_services.domain.port.out;

import java.util.List;
import java.util.Map;

import pg.net.ai_services.domain.model.SearchResult;

public interface VectorStoreOutputPort {
    String store(String texto, Map<String, String> metadata);
    List<SearchResult> search(String query, int topK);
}
