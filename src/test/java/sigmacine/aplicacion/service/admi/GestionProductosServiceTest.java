package sigmacine.aplicacion.service.admi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.dominio.repository.admi.ProductoAdminRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GestionProductosServiceTest {

    private GestionProductosService servicio;
    private ProductoRepositoryFake repositorio;

    @BeforeEach
    public void preparar() {
        repositorio = new ProductoRepositoryFake();
        servicio = new GestionProductosService(repositorio);
    }

    @Test
    public void listarTodos() {
        ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Desc1", "url1", null, "Menu", 
                                          new BigDecimal("5.50"), true, "Activo");
        ProductoDTO p2 = new ProductoDTO(2L, "Refresco", "Desc2", "url2", null, "Menu",
                                          new BigDecimal("3.00"), true, "Activo");
        
        repositorio.agregar(p1);
        repositorio.agregar(p2);
        
        List<ProductoDTO> resultado = servicio.listarTodas();
        
        assertEquals(2, resultado.size());
        assertEquals("Palomitas", resultado.get(0).getNombreProducto());
        assertEquals("Refresco", resultado.get(1).getNombreProducto());
    }

    @Test
    public void listarVacio() {
        List<ProductoDTO> resultado = servicio.listarTodas();
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void constructorConRepoNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new GestionProductosService(null);
        });
    }

    @Test
    public void listarUnProducto() {
        ProductoDTO p = new ProductoDTO(1L, "Combo", "Combo Grande", "url", "Dulce", "Combo",
                                         new BigDecimal("8.00"), true, "Activo");
        repositorio.agregar(p);
        
        List<ProductoDTO> resultado = servicio.listarTodas();
        
        assertEquals(1, resultado.size());
        assertEquals("Combo", resultado.get(0).getNombreProducto());
        assertEquals(new BigDecimal("8.00"), resultado.get(0).getPrecioLista());
    }

    @Test
    public void listarMultiplesProductos() {
        for (int i = 1; i <= 5; i++) {
            ProductoDTO p = new ProductoDTO((long)i, "Producto" + i, "Desc" + i, "url" + i, 
                                             null, "Menu", new BigDecimal(i), true, "Activo");
            repositorio.agregar(p);
        }
        
        List<ProductoDTO> resultado = servicio.listarTodas();
        
        assertEquals(5, resultado.size());
        assertEquals("Producto1", resultado.get(0).getNombreProducto());
        assertEquals("Producto5", resultado.get(4).getNombreProducto());
    }

    // Repositorio falso para pruebas
    private static class ProductoRepositoryFake implements ProductoAdminRepository {
        private final List<ProductoDTO> productos = new ArrayList<>();

        public void agregar(ProductoDTO p) {
            productos.add(p);
        }

        @Override
        public List<ProductoDTO> listarTodas() {
            return new ArrayList<>(productos);
        }
    }
}
