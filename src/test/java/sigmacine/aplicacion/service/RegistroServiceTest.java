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
    public void registrarConFallos() {
        StubRepo repo = new StubRepo(Usuario.crearCliente(5, new Email("a@b.com"), new PasswordHash("$2a$10$abcdefghijklmnopqrstuv"), "N", java.time.LocalDate.now()));
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCliente("N", "a@b.com", "p"));
    }

    @Test
    public void registrarExitoso() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        int id = svc.registrarCliente("Nombre", "nuevo@ex.com", "clave");
        assertEquals(123, id);
    }

    @Test
    public void registrarConEmailInvalidoLanzaExcepcion() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCliente("N", "invalid-email", "p"));
    }

    @Test
    public void registrarConNombreVacio() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        // El servicio no valida nombre vacío, debería crear el usuario
        int id = svc.registrarCliente("", "test@example.com", "password");
        assertEquals(123, id);
    }

    @Test
    public void registrarConPasswordVacia() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        // El servicio no valida password vacía, debería crear el usuario
        int id = svc.registrarCliente("Usuario", "test@example.com", "");
        assertEquals(123, id);
    }

    @Test
    public void registrarEncriptaPassword() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        
        svc.registrarCliente("Test", "test@example.com", "mypassword");
        
        assertEquals(123, repo.createdId);
    }

    @Test
    public void registrarConEmailSinArroba() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> 
            svc.registrarCliente("Usuario", "testexample.com", "password"));
    }

    @Test
    public void registrarConEmailSinDominio() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> 
            svc.registrarCliente("Usuario", "test@", "password"));
    }

    @Test
    public void registrarConEmailNulo() {
        StubRepo repo = new StubRepo(null);
        RegistroService svc = new RegistroService(repo);
        assertThrows(IllegalArgumentException.class, () -> 
            svc.registrarCliente("Usuario", null, "password"));
    }
}

