package pg.net.ai_services.domain.service;

import java.util.List;
import java.util.Optional;

import pg.net.ai_services.domain.model.Usuario;
import pg.net.ai_services.domain.port.in.UsuarioInputPort;
import pg.net.ai_services.domain.port.out.EncryptionOutputPort;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;

public class UsuarioDomainService implements UsuarioInputPort {

    private final UsuarioOutputPort usuarioOutputPort;
    private final EncryptionOutputPort encryptionOutputPort;

    public UsuarioDomainService(UsuarioOutputPort usuarioOutputPort,
                                EncryptionOutputPort encryptionOutputPort) {
        this.usuarioOutputPort = usuarioOutputPort;
        this.encryptionOutputPort = encryptionOutputPort;
    }

    @Override
    public Usuario create(String usuario, String password) {
        String encoded = encryptionOutputPort.encode(password);
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
