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
    void excelIngestsOneDocumentPerDataRow() throws Exception {
        when(vectorStore.store(anyString(), anyMap())).thenReturn("id-1", "id-2");

        byte[] xlsx = buildExcel(new String[][]{
            {"numero_documento", "nombre", "cargo"},
            {"75308851", "Juan Pérez", "Docente"},
            {"87654321", "María López", "Administrativo"}
        });

        List<String> ids = service.ingestFile("personal.xlsx", xlsx);

        assertThat(ids).containsExactly("id-1", "id-2");
        verify(vectorStore).store(
            eq("numero_documento: 75308851. nombre: Juan Pérez. cargo: Docente."),
            eq(Map.of("fuente", "personal.xlsx", "tipo", "excel", "fila", "2", "numero_documento", "75308851"))
        );
        verify(vectorStore).store(
            eq("numero_documento: 87654321. nombre: María López. cargo: Administrativo."),
            eq(Map.of("fuente", "personal.xlsx", "tipo", "excel", "fila", "3", "numero_documento", "87654321"))
        );
    }

    @Test
    void excelSkipsBlankDataRows() throws Exception {
        byte[] xlsx = buildExcel(new String[][]{
            {"numero_documento", "nombre"},
            {"75308851", "Juan Pérez"},
            {"", ""},
            {"87654321", "María López"}
        });
        when(vectorStore.store(anyString(), anyMap())).thenReturn("id-1", "id-2");

        List<String> ids = service.ingestFile("personal.xlsx", xlsx);

        assertThat(ids).containsExactly("id-1", "id-2");
        verify(vectorStore, times(2)).store(anyString(), anyMap());
    }

    @Test
    void pdfIngestsFullTextAsOneDocument() throws Exception {
        org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument();
        org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
        doc.addPage(page);
        try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                 new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
            cs.newLineAtOffset(100, 700);
            cs.showText("Reglamento interno de la empresa.");
            cs.endText();
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        byte[] pdfBytes = out.toByteArray();

        when(vectorStore.store(anyString(), anyMap())).thenReturn("pdf-id-1");

        List<String> ids = service.ingestFile("reglamento.pdf", pdfBytes);

        assertThat(ids).containsExactly("pdf-id-1");
        verify(vectorStore).store(
            org.mockito.ArgumentMatchers.contains("Reglamento interno"),
            eq(Map.of("fuente", "reglamento.pdf", "tipo", "pdf"))
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
