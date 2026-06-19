# File Ingest (Excel/PDF → Vector Store) Design

## Overview

New endpoint `POST /api/ingest/file` accepts a multipart `.xlsx` or `.pdf` file, parses it, and inserts the extracted text into the pgvector store. Excel: one document per row. PDF: one document for the full file. Unsupported extensions are rejected before any processing.

## Endpoint

`POST /api/ingest/file` — `multipart/form-data`, field name `file`.

**Validation (before processing):**
- Extension must be `.xlsx` or `.pdf`
- If not: return `400` with body `"Formato no soportado. Solo se aceptan archivos .xlsx y .pdf."`

**Success response:**
```json
{ "ids": ["uuid1", "uuid2", ...] }
```

## Domain Port

```java
// domain/port/in/FileIngestInputPort.java
List<String> ingestFile(String filename, byte[] content);
```

Returns list of vector store IDs created (one per Excel row, one for PDF).

## Domain Service: `FileIngestDomainService`

Implements `FileIngestInputPort`. Detects file type by `filename` extension.

### Excel (`.xlsx`) — Apache POI `XSSFWorkbook`

- Read first sheet only.
- **Format:** column A = field name, column B = value (per row).
- Each data row builds text: `"FieldA: ValueB. FieldC: ValueD. ..."` by reading column pairs across the row.
- Actually: the Excel has pairs across columns — column 0 = label, column 1 = value, column 2 = label, column 3 = value, etc. Build text by joining all `label: value` pairs found in the row.
- Calls `VectorStoreOutputPort.store(texto, metadata)` per row.
- Metadata: `{ "fuente": "<filename>", "tipo": "excel", "fila": "<row number>" }`
- Skips completely empty rows.

### PDF (`.pdf`) — Apache PDFBox `PDDocument`

- Extracts all text via `PDFTextStripper`.
- Calls `VectorStoreOutputPort.store(texto, metadata)` once.
- Metadata: `{ "fuente": "<filename>", "tipo": "pdf" }`

### Unsupported extension

Throws `IllegalArgumentException("Formato no soportado. Solo se aceptan archivos .xlsx y .pdf.")` — caught by existing `GlobalExceptionHandler` → `400`.

## Infrastructure

### `FileIngestRestAdapter`

- `@PostMapping("/api/ingest/file")`
- Parameter: `@RequestParam("file") MultipartFile file`
- Calls `fileIngestInputPort.ingestFile(file.getOriginalFilename(), file.getBytes())`
- Returns `FileIngestResponseDto` with list of IDs

### `FileIngestResponseDto`

```java
record FileIngestResponseDto(List<String> ids) {}
```

### `DomainConfig` addition

```java
@Bean
FileIngestInputPort fileIngestInputPort(VectorStoreOutputPort vectorStoreOutputPort) {
    return new FileIngestDomainService(vectorStoreOutputPort);
}
```

## Dependencies (pom.xml)

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

## Files

| Action | File |
|--------|------|
| Create | `domain/port/in/FileIngestInputPort.java` |
| Create | `domain/service/FileIngestDomainService.java` |
| Create | `infrastructure/web/FileIngestRestAdapter.java` |
| Create | `infrastructure/web/dto/FileIngestResponseDto.java` |
| Modify | `infrastructure/config/DomainConfig.java` |
| Modify | `pom.xml` |

## Out of Scope

- `.xls` (legacy Excel format)
- Multi-sheet Excel processing
- PDF with scanned images (OCR)
- File size limits
- Duplicate detection
