package pg.net.ai_services.domain.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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
        DataFormatter formatter = new DataFormatter();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var sheet = workbook.getSheetAt(0);
            List<String> headers = new ArrayList<>();
            boolean headersRead = false;
            for (Row row : sheet) {
                if (!headersRead) {
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
                        headers.add(cell != null ? formatter.formatCellValue(cell).trim() : "");
                    }
                    headersRead = true;
                    continue;
                }
                String texto = buildRowText(row, headers, formatter);
                if (texto.isBlank()) continue;
                Map<String, String> metadata = buildMetadata(filename, row, headers, formatter);
                ids.add(vectorStoreOutputPort.store(texto, metadata));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel: " + e.getMessage());
        }
        return ids;
    }

    private String buildRowText(Row row, List<String> headers, DataFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            String header = headers.get(i);
            String value = cell != null ? formatter.formatCellValue(cell).trim() : "";
            if (!header.isBlank() && !value.isBlank()) {
                sb.append(header).append(": ").append(value).append(". ");
            }
        }
        return sb.toString().trim();
    }

    private Map<String, String> buildMetadata(String filename, Row row, List<String> headers, DataFormatter formatter) {
        Map<String, String> metadata = new java.util.LinkedHashMap<>();
        metadata.put("fuente", filename);
        metadata.put("tipo", "excel");
        metadata.put("fila", String.valueOf(row.getRowNum() + 1));
        if (!headers.isEmpty()) {
            String firstHeader = headers.get(0);
            Cell firstCell = row.getCell(0);
            String firstValue = firstCell != null ? formatter.formatCellValue(firstCell).trim() : "";
            if (!firstHeader.isBlank() && !firstValue.isBlank()) {
                metadata.put(firstHeader, firstValue);
            }
        }
        return Map.copyOf(metadata);
    }

    private List<String> ingestPdf(String filename, byte[] content) {
        try (PDDocument doc = Loader.loadPDF(content)) {
            String texto = new PDFTextStripper().getText(doc).trim();
            if (texto.isBlank()) {
                return List.of();
            }
            Map<String, String> metadata = Map.of("fuente", filename, "tipo", "pdf");
            return List.of(vectorStoreOutputPort.store(texto, metadata));
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo PDF: " + e.getMessage());
        }
    }
}
