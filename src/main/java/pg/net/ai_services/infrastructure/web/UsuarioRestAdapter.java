package pg.net.ai_services.infrastructure.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final UsuarioInputPort usuarioInputPort;

    public UsuarioRestAdapter(UsuarioInputPort usuarioInputPort) {
        this.usuarioInputPort = usuarioInputPort;
    }

    @Operation(summary = "Create a new usuario with encoded password")
    @PostMapping
    public UsuarioResponseDto create(@RequestBody UsuarioRequestDto request) {
        var created = usuarioInputPort.create(request.usuario(), request.password());
        return new UsuarioResponseDto(created.getId(), created.getUsuario());
    }

    @Operation(summary = "Find usuario by id")
    @GetMapping("/{id}")
    public UsuarioResponseDto findById(@PathVariable Long id) {
        return usuarioInputPort.findById(id)
                .map(u -> new UsuarioResponseDto(u.getId(), u.getUsuario()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "List all usuarios")
    @GetMapping
    public List<UsuarioResponseDto> findAll() {
        return usuarioInputPort.findAll().stream()
                .map(u -> new UsuarioResponseDto(u.getId(), u.getUsuario()))
                .toList();
    }
}
