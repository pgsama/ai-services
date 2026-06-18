package pg.net.ai_services.infrastructure.persistence;

import pg.net.ai_services.domain.model.Usuario;

final class UsuarioMapper {

    private UsuarioMapper() {
    }

    static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(entity.getId(), entity.getUsuario(), entity.getPassword());
    }

    static UsuarioJpaEntity toEntity(Usuario domain) {
        return new UsuarioJpaEntity(domain.getId(), domain.getUsuario(), domain.getPassword());
    }
}
