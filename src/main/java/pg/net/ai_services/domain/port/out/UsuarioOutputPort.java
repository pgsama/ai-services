package pg.net.ai_services.domain.port.out;

import java.util.List;
import java.util.Optional;

import pg.net.ai_services.domain.model.Usuario;

public interface UsuarioOutputPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
}
