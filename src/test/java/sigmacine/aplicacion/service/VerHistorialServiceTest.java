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
    public void verHistorial() {
    HistorialCompraDTO h = new HistorialCompraDTO(1L, LocalDate.now(), BigDecimal.ZERO, "Bogota", LocalDate.now(), LocalTime.NOON, 1, 0);
    List<HistorialCompraDTO> list = List.of(h);
        VerHistorialService svc = new VerHistorialService(new StubRepo(list));
        var res = svc.verHistorial("u@x.com");
        assertEquals(1, res.size());
    }

    @Test
    public void verHistorialVacio() {
        VerHistorialService svc = new VerHistorialService(new StubRepo(List.of()));
        var res = svc.verHistorial("usuario@test.com");
        assertEquals(0, res.size());
    }

    @Test
    public void verHistorialConMultiplesCompras() {
        HistorialCompraDTO h1 = new HistorialCompraDTO(1L, LocalDate.of(2024, 1, 15), 
            new BigDecimal("25.50"), "Bogota", LocalDate.of(2024, 1, 20), LocalTime.of(19, 30), 2, 1);
        HistorialCompraDTO h2 = new HistorialCompraDTO(2L, LocalDate.of(2024, 2, 10), 
            new BigDecimal("15.00"), "Medellin", LocalDate.of(2024, 2, 12), LocalTime.of(20, 0), 1, 0);
        HistorialCompraDTO h3 = new HistorialCompraDTO(3L, LocalDate.of(2024, 3, 5), 
            new BigDecimal("30.00"), "Cali", LocalDate.of(2024, 3, 8), LocalTime.of(18, 0), 3, 2);
        
        List<HistorialCompraDTO> list = List.of(h1, h2, h3);
        VerHistorialService svc = new VerHistorialService(new StubRepo(list));
        var res = svc.verHistorial("cliente@example.com");
        
        assertEquals(3, res.size());
        assertEquals(1L, res.get(0).getCompraId());
        assertEquals(2L, res.get(1).getCompraId());
        assertEquals(3L, res.get(2).getCompraId());
    }

    @Test
    public void verHistorialConEmailNulo() {
        VerHistorialService svc = new VerHistorialService(new StubRepo(List.of()));
        var res = svc.verHistorial(null);
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void verHistorialConEmailVacio() {
        VerHistorialService svc = new VerHistorialService(new StubRepo(List.of()));
        var res = svc.verHistorial("");
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void verHistorialConDatosCompletos() {
        HistorialCompraDTO h = new HistorialCompraDTO(
            100L, 
            LocalDate.of(2024, 6, 15), 
            new BigDecimal("45.75"), 
            "Barranquilla", 
            LocalDate.of(2024, 6, 18), 
            LocalTime.of(21, 15), 
            4, 
            3
        );
        
        VerHistorialService svc = new VerHistorialService(new StubRepo(List.of(h)));
        var res = svc.verHistorial("test@example.com");
        
        assertEquals(1, res.size());
        assertEquals(100L, res.get(0).getCompraId());
        assertEquals(new BigDecimal("45.75"), res.get(0).getTotal());
        assertEquals("Barranquilla", res.get(0).getSedeCiudad());
        assertEquals(4, res.get(0).getCantBoletos());
        assertEquals(3, res.get(0).getCantProductos());
    }
}

