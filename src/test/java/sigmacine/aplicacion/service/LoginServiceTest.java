package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.valueobject.PasswordHash;
import sigmacine.dominio.valueobject.Email;

import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;

public class LoginServiceTest {

    static class StubRepo implements UsuarioRepository {
        private Usuario user;
        public StubRepo(Usuario user) { this.user = user; }
        @Override public void guardar(Usuario usuario) { }
        @Override public int crearCliente(Email email, PasswordHash passwordHash, String nombre) { return 0; }
        @Override public Usuario buscarPorEmail(Email email) { return user; }
        @Override public Usuario buscarPorId(int id) { return null; }
        @Override public java.util.List<sigmacine.aplicacion.data.HistorialCompraDTO> verHistorial(String emailPlano) { return null; }
        @Override public java.util.List<sigmacine.dominio.entity.Boleto> obtenerBoletosPorCompra(Long compraId) { return null; }
        @Override public java.util.List<sigmacine.aplicacion.data.CompraProductoDTO> obtenerProductosPorCompra(Long compraId) { return null; }
    }

    @Test
    public void autenticar() {
        String plain = "secret";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        Usuario u = Usuario.crearCliente(7, new Email("user@example.com"), ph, "User", java.time.LocalDate.now());

        LoginService svc = new LoginService(new StubRepo(u));
        var dto = svc.autenticar("user@example.com", plain);
        assertNotNull(dto);
        assertEquals(7, dto.getId());

        var dto2 = svc.autenticar("user@example.com", "bad");
        assertNull(dto2);

        var dto3 = svc.autenticar("not-an-email", plain);
        assertNull(dto3);
    }

    @Test
    public void autenticarConEmailNuloDevuelveNull() {
        LoginService svc = new LoginService(new StubRepo(null));
        var dto = svc.autenticar(null, "clave");
        assertNull(dto);
    }

    @Test
    public void autenticarUsuarioNoExistenteDevuelveNull() {
        LoginService svc = new LoginService(new StubRepo(null));
        var dto = svc.autenticar("noexiste@example.com", "clave");
        assertNull(dto);
    }

    @Test
    public void autenticarConFechaRegistroNula() {
        String plain = "secret";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        // Usuario Admin no tiene fecha de registro
        Usuario admin = Usuario.crearAdmin(1, new Email("admin@example.com"), ph, "Admin");
        
        LoginService svc = new LoginService(new StubRepo(admin));
        var dto = svc.autenticar("admin@example.com", plain);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertNull(dto.getFechaRegistro());
    }

    @Test
    public void autenticarConPasswordVacia() {
        String plain = "secret";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        Usuario u = Usuario.crearCliente(1, new Email("user@test.com"), ph, "User", java.time.LocalDate.now());
        
        LoginService svc = new LoginService(new StubRepo(u));
        var dto = svc.autenticar("user@test.com", "");
        assertNull(dto);
    }

    @Test
    public void autenticarConPasswordNula() {
        String plain = "secret";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        Usuario u = Usuario.crearCliente(1, new Email("user@test.com"), ph, "User", java.time.LocalDate.now());
        
        LoginService svc = new LoginService(new StubRepo(u));
        var dto = svc.autenticar("user@test.com", null);
        assertNull(dto);
    }

    @Test
    public void autenticarRetornaDatosCompletos() {
        String plain = "password123";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        java.time.LocalDate fecha = java.time.LocalDate.of(2024, 1, 15);
        Usuario u = Usuario.crearCliente(42, new Email("test@example.com"), ph, "Test User", fecha);
        
        LoginService svc = new LoginService(new StubRepo(u));
        var dto = svc.autenticar("test@example.com", plain);
        
        assertNotNull(dto);
        assertEquals(42, dto.getId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test User", dto.getNombre());
        assertEquals("CLIENTE", dto.getRol());
        assertEquals("2024-01-15", dto.getFechaRegistro());
    }

    @Test
    public void autenticarAdminRetornaRolCorrecto() {
        String plain = "admin123";
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt(4));
        PasswordHash ph = new PasswordHash(hash);
        Usuario admin = Usuario.crearAdmin(1, new Email("admin@system.com"), ph, "Administrator");
        
        LoginService svc = new LoginService(new StubRepo(admin));
        var dto = svc.autenticar("admin@system.com", plain);
        
        assertNotNull(dto);
        assertEquals("ADMIN", dto.getRol());
    }

    @Test
    public void autenticarConEmailVacio() {
        LoginService svc = new LoginService(new StubRepo(null));
        var dto = svc.autenticar("", "password");
        assertNull(dto);
    }

    @Test
    public void autenticarConEspaciosEnEmail() {
        LoginService svc = new LoginService(new StubRepo(null));
        var dto = svc.autenticar("  invalid email  ", "password");
        assertNull(dto);
    }
}

