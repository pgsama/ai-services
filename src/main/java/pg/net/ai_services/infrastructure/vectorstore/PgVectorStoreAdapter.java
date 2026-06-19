package pg.net.ai_services.infrastructure.vectorstore;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

@Component
public class PgVectorStoreAdapter implements VectorStoreOutputPort {

    private static final Logger log = LoggerFactory.getLogger(PgVectorStoreAdapter.class);

    private final VectorStore vectorStore;

    public PgVectorStoreAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public String store(String texto, Map<String, String> metadata) {
        Document document = new Document(texto, Map.copyOf(metadata));
        log.info("storing vector document id={}", document.getId());
        vectorStore.add(List.of(document));
        log.info("vector stored id={}", document.getId());
        return document.getId();
    }

    @Override
    public List<String> search(String query, int topK) {
        log.info("vector search query_length={} topK={}", query.length(), topK);
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build()
        );
        log.info("vector search results={}", docs.size());
        return docs.stream().map(Document::getText).toList();
    }
}
