package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.repository.CompraRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompraServiceTest {

    static class StubRepo implements CompraRepository {
        public Long lastCliente; public List<CompraProductoDTO> lastItems; public String lastMetodo; public java.time.LocalDate lastFecha; public java.math.BigDecimal lastTotal;
        @Override public Long guardarCompraProductos(int clienteId, java.time.LocalDate fecha, List<CompraProductoDTO> items, String metodoPago, BigDecimal total) {
            this.lastCliente = (long) clienteId; this.lastItems = items; this.lastMetodo = metodoPago; this.lastFecha = fecha; this.lastTotal = total;
            return 9999L;
        }
    }

    @Test
    public void validarArgumentos() {
        CompraService svc = new CompraService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> svc.confirmarCompraProductos(1, null, "m"));
        assertThrows(IllegalArgumentException.class, () -> svc.confirmarCompraProductos(1, List.of(), "m"));
        CompraProductoDTO badCantidad = new CompraProductoDTO(1L, "X", 0, new BigDecimal("1.00"));
        assertThrows(IllegalArgumentException.class, () -> svc.confirmarCompraProductos(1, List.of(badCantidad), "m"));
        CompraProductoDTO missingIds = new CompraProductoDTO(null, null, "BoletoSinFuncion", 1, new BigDecimal("1.00"));
        assertThrows(IllegalArgumentException.class, () -> svc.confirmarCompraProductos(1, List.of(missingIds), "m"));
    }

    @Test
    public void confirmarCompraProductos() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod = new CompraProductoDTO(5L, "Refri", 2, new BigDecimal("3.00"));
        Long res = svc.confirmarCompraProductos(42, List.of(prod), "tarjeta");
        assertEquals(9999L, res);
        assertEquals(42L, repo.lastCliente.longValue());
        assertEquals(1, repo.lastItems.size());
    }

    @Test
    public void confirmarCompraConCantidadNegativaLanzaExcepcion() {
        CompraService svc = new CompraService(new StubRepo());
        CompraProductoDTO malo = new CompraProductoDTO(1L, "X", -2, new BigDecimal("1.00"));
        assertThrows(IllegalArgumentException.class, () -> svc.confirmarCompraProductos(1, List.of(malo), "m"));
    }
}
