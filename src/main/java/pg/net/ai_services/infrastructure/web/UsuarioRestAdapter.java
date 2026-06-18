package pg.net.ai_services.infrastructure.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.infrastructure.web.dto.UsuarioRequestDto;
import pg.net.ai_services.infrastructure.web.dto.UsuarioResponseDto;

@Tag(name = "Usuario")
@RestController
@RequestMapping("/api/usuario")
public class UsuarioRestAdapter {

    private static final Logger log = LoggerFactory.getLogger(UsuarioRestAdapter.class);

    private final UsuarioInputPort usuarioInputPort;

    public UsuarioRestAdapter(UsuarioInputPort usuarioInputPort) {
        this.usuarioInputPort = usuarioInputPort;
    }

    @Operation(summary = "Create a new usuario with encoded password")
    @PostMapping
    public UsuarioResponseDto create(@Valid @RequestBody UsuarioRequestDto request) {
        log.info("POST /api/usuario usuario={}", request.usuario());
        var created = usuarioInputPort.create(request.usuario(), request.password());
        log.info("usuario created id={}", created.getId());
        return new UsuarioResponseDto(created.getId(), created.getUsuario());
    }

    @Operation(summary = "Find usuario by id")
    @GetMapping("/{id}")
    public UsuarioResponseDto findById(@PathVariable Long id) {
        log.info("GET /api/usuario/{}", id);
        return usuarioInputPort.findById(id)
                .map(u -> new UsuarioResponseDto(u.getId(), u.getUsuario()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario not found"));
    }

    @Operation(summary = "List all usuarios")
    @GetMapping
    public List<UsuarioResponseDto> findAll() {
        log.info("GET /api/usuario");
        return usuarioInputPort.findAll().stream()
                .map(u -> new UsuarioResponseDto(u.getId(), u.getUsuario()))
                .toList();
    }
}
