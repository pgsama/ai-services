# RAG Chat Design

## Overview

Enrich chat responses with context retrieved from the pgvector store before sending to DeepSeek. If no relevant documents are found, return a fixed "not found" message instead of calling the model.

## Architecture

Approach B: RAG logic lives in `ChatDomainService`. No new services. Two output ports injected into the domain service.

## Changes

### 1. `VectorStoreOutputPort` — add search method

```java
List<String> search(String query, int topK);
```

### 2. `PgVectorStoreAdapter` — implement search

Uses Spring AI `VectorStore.similaritySearch(SearchRequest.query(query).withTopK(topK))`. Returns `List<String>` of document text content.

### 3. `ChatDomainService` — RAG orchestration

Constructor gains `VectorStoreOutputPort` as second dependency.

Flow:
1. Call `vectorStoreOutputPort.search(message, 3)`
2. If result is empty → return `"No he encontrado nada en mi base de datos sobre ese tema."`
3. If results present → build enriched prompt:

```
Usa el siguiente contexto para responder la pregunta.

Contexto:
{fragmento1}
{fragmento2}
...

Pregunta: {message}
```

4. Pass enriched prompt to `chatOutputPort.sendMessage(...)`

### 4. `DomainConfig` — wire new dependency

```java
@Bean
ChatInputPort chatInputPort(ChatOutputPort chatOutputPort,
                             VectorStoreOutputPort vectorStoreOutputPort) {
    return new ChatDomainService(chatOutputPort, vectorStoreOutputPort);
}
```

## Files Changed

| File | Change |
|------|--------|
| `domain/port/out/VectorStoreOutputPort.java` | Add `search` method |
| `infrastructure/vectorstore/PgVectorStoreAdapter.java` | Implement `search` |
| `domain/service/ChatDomainService.java` | Add RAG logic |
| `infrastructure/config/DomainConfig.java` | Wire `VectorStoreOutputPort` into chat bean |

## Behavior

| Scenario | Result |
|----------|--------|
| 0 results from vector store | `"No he encontrado nada en mi base de datos sobre ese tema."` |
| 1–3 results | Enriched prompt sent to DeepSeek; model completes the answer |

## Out of Scope

- Similarity threshold filtering
- Streaming responses
- Chat history / conversation memory
