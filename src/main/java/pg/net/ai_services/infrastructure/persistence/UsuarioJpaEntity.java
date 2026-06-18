package pg.net.ai_services.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "password", nullable = false)
    private String password;

    public UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(Long id, String usuario, String password) {
        this.id = id;
        this.usuario = usuario;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
}
