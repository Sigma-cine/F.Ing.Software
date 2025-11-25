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

    @Test
    public void confirmarCompraBoletoValido() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO boleto = new CompraProductoDTO(null, 10L, "Boleto Función", 1, new BigDecimal("5.00"));
        
        Long res = svc.confirmarCompraProductos(1, List.of(boleto), "efectivo");
        
        assertEquals(9999L, res);
        assertEquals(new BigDecimal("5.00"), repo.lastTotal);
    }

    @Test
    public void confirmarCompraConMultiplesProductos() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod1 = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.00"));
        CompraProductoDTO prod2 = new CompraProductoDTO(2L, "Refresco", 3, new BigDecimal("3.00"));
        
        Long res = svc.confirmarCompraProductos(5, List.of(prod1, prod2), "tarjeta");
        
        assertEquals(9999L, res);
        assertEquals(new BigDecimal("19.00"), repo.lastTotal);
        assertEquals(2, repo.lastItems.size());
    }

    @Test
    public void confirmarCompraConBoletoYProducto() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO boleto = new CompraProductoDTO(null, 5L, "Boleto", 1, new BigDecimal("10.00"));
        CompraProductoDTO prod = new CompraProductoDTO(3L, "Combo", 1, new BigDecimal("8.00"));
        
        Long res = svc.confirmarCompraProductos(10, List.of(boleto, prod), "efectivo");
        
        assertEquals(9999L, res);
        assertEquals(new BigDecimal("18.00"), repo.lastTotal);
    }

    @Test
    public void confirmarCompraCalculaTotalCorrectamente() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod = new CompraProductoDTO(1L, "Producto", 5, new BigDecimal("2.50"));
        
        svc.confirmarCompraProductos(1, List.of(prod), "tarjeta");
        
        assertEquals(new BigDecimal("12.50"), repo.lastTotal);
    }

    @Test
    public void confirmarCompraGuardaMetodoPago() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod = new CompraProductoDTO(1L, "X", 1, new BigDecimal("1.00"));
        
        svc.confirmarCompraProductos(1, List.of(prod), "sigma_card");
        
        assertEquals("sigma_card", repo.lastMetodo);
    }

    @Test
    public void confirmarCompraGuardaFechaActual() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod = new CompraProductoDTO(1L, "X", 1, new BigDecimal("1.00"));
        
        svc.confirmarCompraProductos(1, List.of(prod), "efectivo");
        
        assertEquals(java.time.LocalDate.now(), repo.lastFecha);
    }

    @Test
    public void confirmarCompraConPreciosDecimales() {
        StubRepo repo = new StubRepo();
        CompraService svc = new CompraService(repo);
        CompraProductoDTO prod1 = new CompraProductoDTO(1L, "X", 3, new BigDecimal("1.99"));
        CompraProductoDTO prod2 = new CompraProductoDTO(2L, "Y", 2, new BigDecimal("2.49"));
        
        svc.confirmarCompraProductos(1, List.of(prod1, prod2), "tarjeta");
        
        assertEquals(new BigDecimal("10.95"), repo.lastTotal);
    }
}

