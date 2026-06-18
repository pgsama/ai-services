package pg.net.ai_services.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pg.net.ai_services.domain.model.Usuario;
import pg.net.ai_services.domain.port.out.UsuarioOutputPort;

@Component
public class UsuarioPersistenceAdapter implements UsuarioOutputPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        return UsuarioMapper.toDomain(jpaRepository.save(UsuarioMapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(UsuarioMapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream().map(UsuarioMapper::toDomain).toList();
    }
}
