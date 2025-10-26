package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

import static org.junit.jupiter.api.Assertions.*;

public class RegistroServiceTest {

    static class StubRepo implements UsuarioRepository {
        private final Usuario existente;
        private int createdId = 0;
        public StubRepo(Usuario existente) { this.existente = existente; }
        @Override public void guardar(Usuario usuario) {}
        @Override public int crearCliente(Email email, PasswordHash passwordHash, String nombre) { this.createdId = 123; return 123; }
        @Override public Usuario buscarPorEmail(Email email) { return existente; }
        @Override public Usuario buscarPorId(int id) { return null; }
        @Override public java.util.List<sigmacine.aplicacion.data.HistorialCompraDTO> verHistorial(String emailPlano) { return null; }
        @Override public java.util.List<sigmacine.dominio.entity.Boleto> obtenerBoletosPorCompra(Long compraId) { return null; }
        @Override public java.util.List<sigmacine.aplicacion.data.CompraProductoDTO> obtenerProductosPorCompra(Long compraId) { return null; }
    }

    @Test
    public void registrar_throws_when_email_exists() {
        StubRepo repo = new StubRepo(Usuario.crearCliente(5, new Email("a@b.com"), new PasswordHash("$2a$10$abcdefghijklmnopqrstuv"), "N", java.time.LocalDate.now()));
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCliente("N", "a@b.com", "p"));
    }

    @Test
    public void registrar_success_calls_repo() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        int id = svc.registrarCliente("Nombre", "nuevo@ex.com", "clave");
        assertEquals(123, id);
    }
}
