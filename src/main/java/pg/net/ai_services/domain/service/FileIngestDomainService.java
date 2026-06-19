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
        try (PDDocument doc = Loader.loadPDF(content)) {
            String texto = new PDFTextStripper().getText(doc).trim();
            Map<String, String> metadata = Map.of("fuente", filename, "tipo", "pdf");
            return List.of(vectorStoreOutputPort.store(texto, metadata));
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo PDF: " + e.getMessage());
        }
    }
}
