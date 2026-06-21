package pg.net.ai_services.infrastructure.vectorstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import pg.net.ai_services.domain.model.SearchResult;

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
    void searchReturnsSearchResults() {
        Document doc1 = new Document("texto uno", Map.of("contexto", "manual"));
        Document doc2 = new Document("texto dos", Map.of("contexto", "reglamento"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));

        List<SearchResult> result = adapter.search("consulta", 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).texto()).isEqualTo("texto uno");
        assertThat(result.get(0).contexto()).isEqualTo("manual");
        assertThat(result.get(1).texto()).isEqualTo("texto dos");
        assertThat(result.get(1).contexto()).isEqualTo("reglamento");
    }

    @Test
    void searchReturnsEmptyListWhenNoResults() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        List<SearchResult> result = adapter.search("consulta", 5);

        assertThat(result).isEmpty();
    }
}
