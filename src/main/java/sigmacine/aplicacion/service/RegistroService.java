package sigmacine.aplicacion.service;

import org.mindrot.jbcrypt.BCrypt;

import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public class RegistroService {
    private final UsuarioRepository repo;

    public RegistroService(UsuarioRepository repo) { this.repo = repo; }

    public int registrarCliente(String nombre, String emailPlano, String contrasenaPlano) {
        Email email = new Email(emailPlano);

        Usuario existente = repo.buscarPorEmail(email);
        if (existente != null) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        String hash = BCrypt.hashpw(contrasenaPlano, BCrypt.gensalt(10));
        PasswordHash ph = new PasswordHash(hash);

        return repo.crearCliente(email, ph, nombre);
    }
}
