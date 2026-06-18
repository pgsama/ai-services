package pg.net.ai_services.domain.port.in;

import java.util.List;
import java.util.Optional;

import pg.net.ai_services.domain.model.Usuario;

public interface UsuarioInputPort {
    Usuario create(String usuario, String password);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
}
