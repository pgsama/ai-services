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
    public FileIngestResponseDto ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("contexto") String contexto) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        log.info("POST /api/ingest/file filename={} size={} contexto={}", filename, file.getSize(), contexto);
        List<String> ids = fileIngestInputPort.ingestFile(filename, file.getBytes(), contexto);
        log.info("file ingest completed ids_count={}", ids.size());
        return new FileIngestResponseDto(ids);
    }
}
