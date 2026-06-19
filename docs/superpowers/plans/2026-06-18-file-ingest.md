# File Ingest (Excel/PDF) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `POST /api/ingest/file` endpoint that accepts a `.xlsx` or `.pdf` file, extracts text, and inserts it into the pgvector store (one document per Excel row, one document for the full PDF).

**Architecture:** Hexagonal. New `FileIngestInputPort` + `FileIngestDomainService` handle parsing and orchestration in the domain layer. `FileIngestRestAdapter` receives the multipart file and delegates. Parsing uses Apache POI (Excel) and PDFBox (PDF). `VectorStoreOutputPort.store` (already exists) persists each document.

**Tech Stack:** Java 21, Spring Boot 4.1.0, Apache POI 5.3.0 (`poi-ooxml`), Apache PDFBox 3.0.3, JUnit 5, Mockito

## Global Constraints

- Base package: `pg.net.ai_services`
- No comments in Java files
- No Spring annotations in domain classes
- Maven wrapper: `mvnw.cmd` on Windows / `./mvnw` on Unix
- `VectorStoreOutputPort.store(String texto, Map<String, String> metadata): String` already exists
- `GlobalExceptionHandler` exists at `infrastructure/web/GlobalExceptionHandler.java`

---

### Task 1: Add dependencies + `IllegalArgumentException` handler

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandler.java`
- Test: `src/test/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Produces: `GlobalExceptionHandler` returns `400` with `ApiErrorDto` message for `IllegalArgumentException`

- [ ] **Step 1: Write failing test**

Create `src/test/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandlerTest.java`:

```java
package pg.net.ai_services.infrastructure.web;

import org.junit.jupiter.api.Test;
import pg.net.ai_services.infrastructure.web.dto.ApiErrorDto;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentReturns400WithMessage() {
        ApiErrorDto result = handler.handleIllegalArgument(
                new IllegalArgumentException("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf."));

        assertThat(result.status()).isEqualTo(400);
        assertThat(result.message()).isEqualTo("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf.");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```
mvnw.cmd test -Dtest=GlobalExceptionHandlerTest
```

Expected: FAIL — `handleIllegalArgument` method does not exist.

- [ ] **Step 3: Add POI and PDFBox to `pom.xml`**

Inside the `<dependencies>` block of `pom.xml`, add:

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>
```

- [ ] **Step 4: Add `handleIllegalArgument` to `GlobalExceptionHandler`**

Replace full content of `src/main/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandler.java`:

```java
package pg.net.ai_services.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import pg.net.ai_services.infrastructure.web.dto.ApiErrorDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ApiErrorDto handleResponseStatus(ResponseStatusException ex) {
        return new ApiErrorDto(ex.getStatusCode().value(), ex.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDto handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return new ApiErrorDto(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorDto handleIllegalArgument(IllegalArgumentException ex) {
        return new ApiErrorDto(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorDto handleGeneral(Exception ex) {
        return new ApiErrorDto(500, "Internal error");
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```
mvnw.cmd test -Dtest=GlobalExceptionHandlerTest
```

Expected: PASS (1 test)

- [ ] **Step 6: Commit**

```
git add pom.xml
git add src/main/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandler.java
git add src/test/java/pg/net/ai_services/infrastructure/web/GlobalExceptionHandlerTest.java
git commit -m "feat: add POI/PDFBox deps and IllegalArgumentException handler"
```

---

### Task 2: Domain port + service (`FileIngestInputPort` + `FileIngestDomainService`)

**Files:**
- Create: `src/main/java/pg/net/ai_services/domain/port/in/FileIngestInputPort.java`
- Create: `src/main/java/pg/net/ai_services/domain/service/FileIngestDomainService.java`
- Test: `src/test/java/pg/net/ai_services/domain/service/FileIngestDomainServiceTest.java`

**Interfaces:**
- Consumes: `VectorStoreOutputPort.store(String texto, Map<String, String> metadata): String`
- Produces: `FileIngestInputPort.ingestFile(String filename, byte[] content): List<String>`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/pg/net/ai_services/domain/service/FileIngestDomainServiceTest.java`:

```java
package pg.net.ai_services.domain.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileIngestDomainServiceTest {

    private final VectorStoreOutputPort vectorStore = mock(VectorStoreOutputPort.class);
    private final FileIngestDomainService service = new FileIngestDomainService(vectorStore);

    @Test
    void excelIngestsOneDocumentPerRow() throws Exception {
        when(vectorStore.store(anyString(), anyMap())).thenReturn("id-1", "id-2");

        byte[] xlsx = buildExcel(new String[][]{
            {"Personal", "Juan Pérez", "Cargo", "Docente"},
            {"Personal", "María López", "Cargo", "Administrativo"}
        });

        List<String> ids = service.ingestFile("personal.xlsx", xlsx);

        assertThat(ids).containsExactly("id-1", "id-2");
        verify(vectorStore).store(
            eq("Personal: Juan Pérez. Cargo: Docente."),
            eq(Map.of("fuente", "personal.xlsx", "tipo", "excel", "fila", "1"))
        );
        verify(vectorStore).store(
            eq("Personal: María López. Cargo: Administrativo."),
            eq(Map.of("fuente", "personal.xlsx", "tipo", "excel", "fila", "2"))
        );
    }

    @Test
    void unsupportedExtensionThrowsIllegalArgument() {
        assertThatThrownBy(() -> service.ingestFile("report.csv", new byte[0]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf.");
    }

    private byte[] buildExcel(String[][] rows) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet();
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < rows[r].length; c++) {
                    row.createCell(c).setCellValue(rows[r][c]);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```
mvnw.cmd test -Dtest=FileIngestDomainServiceTest
```

Expected: FAIL — `FileIngestDomainService` does not exist.

- [ ] **Step 3: Create `FileIngestInputPort`**

Create `src/main/java/pg/net/ai_services/domain/port/in/FileIngestInputPort.java`:

```java
package pg.net.ai_services.domain.port.in;

import java.util.List;

public interface FileIngestInputPort {
    List<String> ingestFile(String filename, byte[] content);
}
```

- [ ] **Step 4: Create `FileIngestDomainService`**

Create `src/main/java/pg/net/ai_services/domain/service/FileIngestDomainService.java`:

```java
package pg.net.ai_services.domain.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pg.net.ai_services.domain.port.in.FileIngestInputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;

public class FileIngestDomainService implements FileIngestInputPort {

    private final VectorStoreOutputPort vectorStoreOutputPort;

    public FileIngestDomainService(VectorStoreOutputPort vectorStoreOutputPort) {
        this.vectorStoreOutputPort = vectorStoreOutputPort;
    }

    @Override
    public List<String> ingestFile(String filename, byte[] content) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx")) {
            return ingestExcel(filename, content);
        } else if (lower.endsWith(".pdf")) {
            return ingestPdf(filename, content);
        }
        throw new IllegalArgumentException("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf.");
    }

    private List<String> ingestExcel(String filename, byte[] content) {
        List<String> ids = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var sheet = workbook.getSheetAt(0);
            int rowIndex = 0;
            for (Row row : sheet) {
                rowIndex++;
                String texto = buildRowText(row);
                if (texto.isBlank()) continue;
                Map<String, String> metadata = Map.of(
                    "fuente", filename,
                    "tipo", "excel",
                    "fila", String.valueOf(rowIndex)
                );
                ids.add(vectorStoreOutputPort.store(texto, metadata));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel: " + e.getMessage());
        }
        return ids;
    }

    private String buildRowText(Row row) {
        StringBuilder sb = new StringBuilder();
        int lastCell = row.getLastCellNum();
        for (int i = 0; i + 1 < lastCell; i += 2) {
            Cell label = row.getCell(i);
            Cell value = row.getCell(i + 1);
            if (label == null || value == null) continue;
            String labelStr = label.toString().trim();
            String valueStr = value.toString().trim();
            if (!labelStr.isBlank() && !valueStr.isBlank()) {
                sb.append(labelStr).append(": ").append(valueStr).append(". ");
            }
        }
        return sb.toString().trim();
    }

    private List<String> ingestPdf(String filename, byte[] content) {
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(content))) {
            String texto = new PDFTextStripper().getText(doc).trim();
            Map<String, String> metadata = Map.of("fuente", filename, "tipo", "pdf");
            return List.of(vectorStoreOutputPort.store(texto, metadata));
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo PDF: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

```
mvnw.cmd test -Dtest=FileIngestDomainServiceTest
```

Expected: PASS (2 tests)

- [ ] **Step 6: Commit**

```
git add src/main/java/pg/net/ai_services/domain/port/in/FileIngestInputPort.java
git add src/main/java/pg/net/ai_services/domain/service/FileIngestDomainService.java
git add src/test/java/pg/net/ai_services/domain/service/FileIngestDomainServiceTest.java
git commit -m "feat: add FileIngestInputPort and FileIngestDomainService (Excel/PDF parsing)"
```

---

### Task 3: REST adapter + DTO + DomainConfig wiring

**Files:**
- Create: `src/main/java/pg/net/ai_services/infrastructure/web/dto/FileIngestResponseDto.java`
- Create: `src/main/java/pg/net/ai_services/infrastructure/web/FileIngestRestAdapter.java`
- Modify: `src/main/java/pg/net/ai_services/infrastructure/config/DomainConfig.java`

**Interfaces:**
- Consumes: `FileIngestInputPort.ingestFile(String filename, byte[] content): List<String>` (from Task 2)
- Produces: `POST /api/ingest/file` → `FileIngestResponseDto(List<String> ids)`

- [ ] **Step 1: Create `FileIngestResponseDto`**

Create `src/main/java/pg/net/ai_services/infrastructure/web/dto/FileIngestResponseDto.java`:

```java
package pg.net.ai_services.infrastructure.web.dto;

import java.util.List;

public record FileIngestResponseDto(List<String> ids) {
}
```

- [ ] **Step 2: Create `FileIngestRestAdapter`**

Create `src/main/java/pg/net/ai_services/infrastructure/web/FileIngestRestAdapter.java`:

```java
package pg.net.ai_services.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pg.net.ai_services.domain.port.in.FileIngestInputPort;
import pg.net.ai_services.infrastructure.web.dto.FileIngestResponseDto;

import java.io.IOException;
import java.util.List;

@Tag(name = "Ingest")
@RestController
@RequestMapping("/api/ingest")
public class FileIngestRestAdapter {

    private static final Logger log = LoggerFactory.getLogger(FileIngestRestAdapter.class);

    private final FileIngestInputPort fileIngestInputPort;

    public FileIngestRestAdapter(FileIngestInputPort fileIngestInputPort) {
        this.fileIngestInputPort = fileIngestInputPort;
    }

    @Operation(summary = "Parse Excel or PDF file and store contents in pgvector")
    @PostMapping("/file")
    @ResponseStatus(HttpStatus.CREATED)
    public FileIngestResponseDto ingestFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        log.info("POST /api/ingest/file filename={} size={}", filename, file.getSize());
        List<String> ids = fileIngestInputPort.ingestFile(filename, file.getBytes());
        log.info("file ingest completed ids_count={}", ids.size());
        return new FileIngestResponseDto(ids);
    }
}
```

- [ ] **Step 3: Update `DomainConfig`**

Replace full content of `src/main/java/pg/net/ai_services/infrastructure/config/DomainConfig.java`:

```java
package pg.net.ai_services.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pg.net.ai_services.domain.port.in.ChatInputPort;
import pg.net.ai_services.domain.port.in.FileIngestInputPort;
import pg.net.ai_services.domain.port.in.IngestInputPort;
import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.domain.port.out.ChatOutputPort;
import pg.net.ai_services.domain.port.out.EncryptionOutputPort;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;
import pg.net.ai_services.domain.port.out.VectorStoreOutputPort;
import pg.net.ai_services.domain.service.ChatDomainService;
import pg.net.ai_services.domain.service.FileIngestDomainService;
import pg.net.ai_services.domain.service.IngestDomainService;
import pg.net.ai_services.domain.service.UsuarioDomainService;

@Configuration
public class DomainConfig {

    @Bean
    ChatInputPort chatInputPort(ChatOutputPort chatOutputPort,
                                VectorStoreOutputPort vectorStoreOutputPort) {
        return new ChatDomainService(chatOutputPort, vectorStoreOutputPort);
    }

    @Bean
    UsuarioInputPort usuarioInputPort(UsuarioOutputPort usuarioOutputPort,
                                      EncryptionOutputPort encryptionOutputPort) {
        return new UsuarioDomainService(usuarioOutputPort, encryptionOutputPort);
    }

    @Bean
    IngestInputPort ingestInputPort(VectorStoreOutputPort vectorStoreOutputPort) {
        return new IngestDomainService(vectorStoreOutputPort);
    }

    @Bean
    FileIngestInputPort fileIngestInputPort(VectorStoreOutputPort vectorStoreOutputPort) {
        return new FileIngestDomainService(vectorStoreOutputPort);
    }
}
```

- [ ] **Step 4: Run full test suite**

```
mvnw.cmd test
```

Expected: PASS — all existing tests + 2 new domain tests + 1 handler test = 7 tests total.

- [ ] **Step 5: Commit**

```
git add src/main/java/pg/net/ai_services/infrastructure/web/dto/FileIngestResponseDto.java
git add src/main/java/pg/net/ai_services/infrastructure/web/FileIngestRestAdapter.java
git add src/main/java/pg/net/ai_services/infrastructure/config/DomainConfig.java
git commit -m "feat: add FileIngestRestAdapter and wire FileIngestDomainService"
```
