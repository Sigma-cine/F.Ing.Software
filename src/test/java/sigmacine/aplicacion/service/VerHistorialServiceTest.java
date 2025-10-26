package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.aplicacion.data.HistorialCompraDTO;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class VerHistorialServiceTest {

    static class StubRepo implements UsuarioRepository {
        private final List<HistorialCompraDTO> data;
        public StubRepo(List<HistorialCompraDTO> data) { this.data = data; }
        @Override public void guardar(sigmacine.dominio.entity.Usuario usuario) {}
        @Override public int crearCliente(sigmacine.dominio.valueobject.Email email, sigmacine.dominio.valueobject.PasswordHash passwordHash, String nombre) { return 0; }
        @Override public sigmacine.dominio.entity.Usuario buscarPorEmail(sigmacine.dominio.valueobject.Email email) { return null; }
        @Override public sigmacine.dominio.entity.Usuario buscarPorId(int id) { return null; }
        @Override public List<HistorialCompraDTO> verHistorial(String emailPlano) { return data; }
        @Override public java.util.List<sigmacine.dominio.entity.Boleto> obtenerBoletosPorCompra(Long compraId) { return null; }
        @Override public java.util.List<sigmacine.aplicacion.data.CompraProductoDTO> obtenerProductosPorCompra(Long compraId) { return null; }
    }

    @Test
    public void verHistorial_delegates_to_repo() {
    HistorialCompraDTO h = new HistorialCompraDTO(1L, LocalDate.now(), BigDecimal.ZERO, "Bogota", LocalDate.now(), LocalTime.NOON, 1, 0);
    List<HistorialCompraDTO> list = List.of(h);
        VerHistorialService svc = new VerHistorialService(new StubRepo(list));
        var res = svc.verHistorial("u@x.com");
        assertEquals(1, res.size());
    }
}
