package sigmacine.aplicacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Producto;

import java.math.BigDecimal;

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
    public void limpiarCarrito() {
        carrito.addItem(new CompraProductoDTO(1L, "X", Integer.valueOf(1), new BigDecimal("1.00")));
        carrito.clear();
        assertEquals(0, carrito.getItems().size());
        assertTrue(carrito.getBoletos().isEmpty());
        assertTrue(carrito.getProductos().isEmpty());
    }

    @Test
    public void listenerRecibeEventoAlAgregarYEliminar() {
        final int[] contador = {0};
        carrito.addListener(change -> contador[0] += 1);
        carrito.addItem(new CompraProductoDTO(1L, "X", Integer.valueOf(1), new BigDecimal("1.00")));
        assertTrue(contador[0] > 0);
        contador[0] = 0;
        carrito.removeItem(carrito.getItems().get(0));
        assertTrue(contador[0] > 0);
    }

    @Test
    public void agregarItemNulo() {
        int sizeBefore = carrito.getItems().size();
        carrito.addItem(null);
        assertEquals(sizeBefore, carrito.getItems().size());
    }

    @Test
    public void removerItemNulo() {
        carrito.addItem(new CompraProductoDTO(1L, "X", Integer.valueOf(1), new BigDecimal("1.00")));
        int sizeBefore = carrito.getItems().size();
        carrito.removeItem(null);
        assertEquals(sizeBefore, carrito.getItems().size());
    }

    @Test
    public void agregarBoletoSinHorario() {
        Boleto b = new Boleto(10L, "Pelicula", "Sala1", null, "A1", 1500);
        carrito.addBoleto(b);
        assertEquals(1, carrito.getItems().size());
        CompraProductoDTO item = carrito.getItems().get(0);
        assertTrue(item.getNombre().contains("Pelicula"));
    }

    @Test
    public void agregarBoletoConPeliculaNula() {
        Boleto b = new Boleto(10L, null, "Sala1", "19:00", "A1", 1500);
        carrito.addBoleto(b);
        assertEquals(1, carrito.getItems().size());
    }

    @Test
    public void getTotalConCarritoVacio() {
        assertEquals(BigDecimal.ZERO.setScale(2), carrito.getTotal());
    }

    @Test
    public void agregarProducto() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Palomitas");
        p.setPrecio(500f); // 5.00 en centavos
        
        carrito.addProducto(p);
        
        assertEquals(1, carrito.getItems().size());
        assertEquals(1, carrito.getProductos().size());
        assertEquals(new BigDecimal("5.00"), carrito.getTotal());
    }

    @Test
    public void agregarProductoNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            carrito.addProducto(null);
        });
    }

    @Test
    public void agregarVariosProductos() {
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("Palomitas");
        p1.setPrecio(500f);
        
        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setNombre("Refresco");
        p2.setPrecio(300f);
        
        carrito.addProducto(p1);
        carrito.addProducto(p2);
        
        assertEquals(2, carrito.getItems().size());
        assertEquals(2, carrito.getProductos().size());
        assertEquals(new BigDecimal("8.00"), carrito.getTotal());
    }

    @Test
    public void agregarProductoYBoleto() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Combo");
        p.setPrecio(800f);
        
        Boleto b = new Boleto(10L, "Matrix", "Sala2", "20:00", "B5", 1200);
        
        carrito.addProducto(p);
        carrito.addBoleto(b);
        
        assertEquals(2, carrito.getItems().size());
        assertEquals(1, carrito.getProductos().size());
        assertEquals(1, carrito.getBoletos().size());
        assertEquals(new BigDecimal("20.00"), carrito.getTotal());
    }

    @Test
    public void addItemConsolidatedConItemsIguales() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(1L, "Palomitas", 3, new BigDecimal("5.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        assertEquals(1, carrito.getItems().size());
        assertEquals(5, carrito.getItems().get(0).getCantidad());
        assertEquals(new BigDecimal("25.00"), carrito.getTotal());
    }

    @Test
    public void addItemConsolidatedConItemsDiferentes() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, "Refresco", 1, new BigDecimal("3.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        assertEquals(2, carrito.getItems().size());
        assertEquals(new BigDecimal("13.00"), carrito.getTotal());
    }

    @Test
    public void addItemConsolidatedConItemNulo() {
        int sizeBefore = carrito.getItems().size();
        carrito.addItemConsolidated(null);
        assertEquals(sizeBefore, carrito.getItems().size());
    }

    @Test
    public void addItemConsolidatedConMismoPrecioDistintoNombre() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(1L, "Nachos", 1, new BigDecimal("5.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void addItemConsolidatedConMismoNombreDistintoPrecio() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 2, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(1L, "Palomitas", 1, new BigDecimal("6.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void getTotalConMultiplesItems() {
        carrito.addItem(new CompraProductoDTO(1L, "Item1", 2, new BigDecimal("5.50")));
        carrito.addItem(new CompraProductoDTO(2L, "Item2", 3, new BigDecimal("3.00")));
        carrito.addItem(new CompraProductoDTO(3L, "Item3", 1, new BigDecimal("10.00")));
        
        assertEquals(new BigDecimal("30.00"), carrito.getTotal());
    }

    @Test
    public void getInstanceDevuelveMismaInstancia() {
        CarritoService instance1 = CarritoService.getInstance();
        CarritoService instance2 = CarritoService.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    public void getBoletosDevuelveListaInmutable() {
        Boleto b = new Boleto(10L, "Pelicula", "Sala1", "19:00", "A1", 1500);
        carrito.addBoleto(b);
        
        assertThrows(UnsupportedOperationException.class, () -> {
            carrito.getBoletos().clear();
        });
    }

    @Test
    public void getProductosDevuelveListaInmutable() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Palomitas");
        p.setPrecio(500f);
        carrito.addProducto(p);
        
        assertThrows(UnsupportedOperationException.class, () -> {
            carrito.getProductos().clear();
        });
    }

    @Test
    public void conversionDePrecioDesdeBoletoCentavosABigDecimal() {
        Boleto b = new Boleto(10L, "Pelicula", "Sala1", "19:00", "A1", 1550); // 15.50
        carrito.addBoleto(b);
        
        assertEquals(new BigDecimal("15.50"), carrito.getTotal());
    }

    @Test
    public void conversionDePrecioDesdeProductoCentavosABigDecimal() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Combo");
        p.setPrecio(1250f); // 12.50
        carrito.addProducto(p);
        
        assertEquals(new BigDecimal("12.50"), carrito.getTotal());
    }

    @Test
    public void limpiarCarritoReseteBoletos() {
        Boleto b = new Boleto(10L, "Pelicula", "Sala1", "19:00", "A1", 1500);
        carrito.addBoleto(b);
        
        carrito.clear();
        
        assertEquals(0, carrito.getBoletos().size());
        assertEquals(0, carrito.getItems().size());
    }

    @Test
    public void limpiarCarritoReseteProductos() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Palomitas");
        p.setPrecio(500f);
        carrito.addProducto(p);
        
        carrito.clear();
        
        assertEquals(0, carrito.getProductos().size());
        assertEquals(0, carrito.getItems().size());
    }

    @Test
    public void getItemsNoDevuelveNull() {
        assertNotNull(carrito.getItems());
    }

    @Test
    public void getBoletosNoDevuelveNull() {
        assertNotNull(carrito.getBoletos());
    }

    @Test
    public void getProductosNoDevuelveNull() {
        assertNotNull(carrito.getProductos());
    }

    @Test
    public void areItemsEqualConAmbosNombresNulos() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, null, 1, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, null, 1, new BigDecimal("5.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si ambos nombres son nulos y precios iguales, deben consolidarse
        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    public void areItemsEqualConPrimerNombreNulo() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, null, 1, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, "Palomitas", 1, new BigDecimal("5.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si uno es nulo y el otro no, no deben consolidarse
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void areItemsEqualConSegundoNombreNulo() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 1, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, null, 1, new BigDecimal("5.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si uno es nulo y el otro no, no deben consolidarse
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void areItemsEqualAmbosNombresNulosYPreciosDiferentes() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, null, 1, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, null, 1, new BigDecimal("6.00"));
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Nombres nulos pero precios diferentes, no deben consolidarse
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void areItemsEqualConPreciosNulos() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 1, null);
        CompraProductoDTO item2 = new CompraProductoDTO(2L, "Palomitas", 1, null);
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si ambos precios son nulos y nombres iguales, deben consolidarse
        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    public void areItemsEqualConUnPrecioNulo() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, "Palomitas", 1, new BigDecimal("5.00"));
        CompraProductoDTO item2 = new CompraProductoDTO(2L, "Palomitas", 1, null);
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si uno tiene precio nulo y el otro no, no deben consolidarse
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    public void areItemsEqualTodoNulo() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, null, 1, null);
        CompraProductoDTO item2 = new CompraProductoDTO(2L, null, 1, null);
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Si todo es nulo (nombre y precio), deben consolidarse
        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    public void addItemConsolidatedUsaConstructorCompleto() {
        CompraProductoDTO item1 = new CompraProductoDTO(1L, 10L, "Combo", 1, new BigDecimal("15.00"), "A1");
        CompraProductoDTO item2 = new CompraProductoDTO(1L, 10L, "Combo", 2, new BigDecimal("15.00"), "A1");
        
        carrito.addItemConsolidated(item1);
        carrito.addItemConsolidated(item2);
        
        // Deben consolidarse usando el constructor de 6 parámetros
        assertEquals(1, carrito.getItems().size());
        assertEquals(3, carrito.getItems().get(0).getCantidad());
        assertEquals(1L, carrito.getItems().get(0).getProductoId());
        assertEquals(10L, carrito.getItems().get(0).getFuncionId());
    }

    @Test
    public void safeConStringNulo() {
        Boleto b = new Boleto(10L, null, "Sala1", "19:00", "A1", 1500);
        carrito.addBoleto(b);
        
        // El nombre debe contener "Boleto: " aunque la película sea nula
        CompraProductoDTO item = carrito.getItems().get(0);
        assertTrue(item.getNombre().startsWith("Boleto: "));
    }

    @Test
    public void addBoletoDividePrecioPorCien() {
        Boleto b = new Boleto(10L, "Matrix", "Sala1", "19:00", "A1", 2550);
        carrito.addBoleto(b);
        
        // 2550 centavos = 25.50
        assertEquals(new BigDecimal("25.50"), carrito.getTotal());
    }

    @Test
    public void addProductoDividePrecioPorCien() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Combo Grande");
        p.setPrecio(3750f); // 37.50
        
        carrito.addProducto(p);
        
        assertEquals(new BigDecimal("37.50"), carrito.getTotal());
    }
}

