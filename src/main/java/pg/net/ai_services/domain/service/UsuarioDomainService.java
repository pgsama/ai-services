package pg.net.ai_services.domain.service;

import java.util.List;
import java.util.Optional;

import pg.net.ai_services.domain.model.Usuario;
import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;
import pg.net.ai_services.utils.CryptoUtils;

public class UsuarioDomainService implements UsuarioInputPort {

    private final UsuarioOutputPort usuarioOutputPort;

    public UsuarioDomainService(UsuarioOutputPort usuarioOutputPort) {
        this.usuarioOutputPort = usuarioOutputPort;
    }

    @Override
    public Usuario create(String usuario, String password) {
        String encoded = CryptoUtils.encode(password);
        return usuarioOutputPort.save(new Usuario(null, usuario, encoded));
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioOutputPort.findById(id);
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioOutputPort.findAll();
    }
}
