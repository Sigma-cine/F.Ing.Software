package sigmacine.aplicacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Producto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarritoServiceTest {

    private CarritoService carrito;

    @BeforeEach
    public void setup() {
        carrito = CarritoService.getInstance();
        carrito.clear();
    }

    @Test
    public void agregarEliminarProducto() {
        CompraProductoDTO it = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.50"));
        carrito.addItem(it);
        assertEquals(1, carrito.getItems().size());
        assertEquals(new BigDecimal("11.00"), carrito.getTotal());

        carrito.removeItem(it);
        assertEquals(0, carrito.getItems().size());
    }

    @Test
    public void agregarBoleto() {
        Boleto b = new Boleto(10L, "PeliculaX", "Sala1", "19:00", "A1", 1500);
        carrito.addBoleto(b);
        assertEquals(1, carrito.getItems().size());
        assertEquals(1, carrito.getBoletos().size());
        assertThrows(IllegalArgumentException.class, () -> carrito.addBoleto(null));
    }

    @Test
    public void agregarProducto() {
        Producto p = new Producto(2L, "Gaseosa", 450);
        carrito.addProducto(p);
        assertEquals(1, carrito.getItems().size());
        assertEquals(1, carrito.getProductos().size());
        assertThrows(IllegalArgumentException.class, () -> carrito.addProducto(null));
    }

    @Test
    public void limpiarCarrito() {
        carrito.addItem(new CompraProductoDTO(1L, "X", 1, new BigDecimal("1.00")));
        carrito.clear();
        assertEquals(0, carrito.getItems().size());
        assertTrue(carrito.getBoletos().isEmpty());
        assertTrue(carrito.getProductos().isEmpty());
    }
}
