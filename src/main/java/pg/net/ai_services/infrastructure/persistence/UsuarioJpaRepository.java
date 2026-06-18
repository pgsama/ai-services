package pg.net.ai_services.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
}
