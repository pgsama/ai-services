package pg.net.ai_services.domain.port.in;

import java.util.Map;

public interface IngestInputPort {
    String ingest(String texto, Map<String, String> metadata);
}
