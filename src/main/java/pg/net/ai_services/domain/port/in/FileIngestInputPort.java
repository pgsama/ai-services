package pg.net.ai_services.domain.port.in;

import java.util.List;

public interface FileIngestInputPort {
    List<String> ingestFile(String filename, byte[] content);
}
