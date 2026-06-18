package pg.net.ai_services.domain.service;

import java.util.Map;

import pg.net.ai_services.domain.port.in.IngestInputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

public class IngestDomainService implements IngestInputPort {

    private final VectorStoreOutputPort vectorStoreOutputPort;

    public IngestDomainService(VectorStoreOutputPort vectorStoreOutputPort) {
        this.vectorStoreOutputPort = vectorStoreOutputPort;
    }

    @Override
    public String ingest(String texto, Map<String, String> metadata) {
        return vectorStoreOutputPort.store(texto, metadata);
    }
}
