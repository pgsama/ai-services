package pg.net.ai_services.infrastructure.vectorstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PgVectorStoreAdapterTest {

    private final VectorStore vectorStore = mock(VectorStore.class);
    private final PgVectorStoreAdapter adapter = new PgVectorStoreAdapter(vectorStore);

    @Test
    void searchReturnsDocumentTexts() {
        Document doc1 = new Document("texto uno", Map.of());
        Document doc2 = new Document("texto dos", Map.of());
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));

        List<String> result = adapter.search("consulta", 3);

        assertThat(result).containsExactly("texto uno", "texto dos");
    }

    @Test
    void searchReturnsEmptyListWhenNoResults() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        List<String> result = adapter.search("consulta", 3);

        assertThat(result).isEmpty();
    }
}
