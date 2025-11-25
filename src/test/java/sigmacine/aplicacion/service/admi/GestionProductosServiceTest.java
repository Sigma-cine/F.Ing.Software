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
            @Test
            public void crearProductoNombreNullLanzaExcepcion() {
                ProductoDTO p = new ProductoDTO(13L, null, "desc", "url", null, "Menu", new BigDecimal("2.00"), true, "Activo");
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(p));
            }

            @Test
            public void actualizarProductoNombreNullLanzaExcepcion() {
                ProductoDTO cambios = new ProductoDTO(24L, null, "desc", "url", null, "Menu", new BigDecimal("1.00"), true, "Activo");
                repositorio.agregar(cambios);
                assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(24L, cambios));
            }
            @Test
            public void listarTodasSinProductosDevuelveListaVacia() {
                List<ProductoDTO> resultado = servicio.listarTodas();
                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
            }

            @Test
            public void buscarProductosSinCoincidenciasDevuelveVacia() {
                ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
                repositorio.agregar(p1);
                List<ProductoDTO> resultado = servicio.buscarProductos("ZZZ");
                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
            }

            @Test
            public void buscarProductosNuloDevuelveTodos() {
                ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
                repositorio.agregar(p1);
                List<ProductoDTO> resultado = servicio.buscarProductos(null);
                assertEquals(1, resultado.size());
            }

            @Test
            public void buscarProductosRepoDevuelveVacia() {
                // Simula que el repo devuelve vacía aunque haya productos
                ProductoRepositoryFake repoVacio = new ProductoRepositoryFake() {
                    @Override
                    public List<ProductoDTO> buscarPorCriterio(String q) {
                        return new ArrayList<>();
                    }
                };
                GestionProductosService servicioVacio = new GestionProductosService(repoVacio);
                ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
                repoVacio.agregar(p1);
                List<ProductoDTO> resultado = servicioVacio.buscarProductos("Palomitas");
                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
            }
        @Test
        public void crearProductoNombreSoloEspaciosLanzaExcepcion() {
            ProductoDTO p = new ProductoDTO(12L, "   ", "desc", "url", null, "Menu", new BigDecimal("2.00"), true, "Activo");
            assertThrows(IllegalArgumentException.class, () -> servicio.crear(p));
        }

        @Test
        public void actualizarProductoNombreSoloEspaciosLanzaExcepcion() {
            ProductoDTO cambios = new ProductoDTO(22L, "   ", "desc", "url", null, "Menu", new BigDecimal("1.00"), true, "Activo");
            repositorio.agregar(cambios);
            assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(22L, cambios));
        }

        @Test
        public void actualizarProductoDtoNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(23L, null));
        }

        @Test
        public void actualizarProductoIdNegativoLanzaExcepcion() {
            ProductoDTO cambios = new ProductoDTO(-1L, "Valido", "desc", "url", null, "Menu", new BigDecimal("1.00"), true, "Activo");
            assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(-1L, cambios));
        }

        @Test
        public void eliminarProductoIdNegativoLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> servicio.eliminar(-5L));
        }

        @Test
        public void buscarProductosSoloEspaciosDevuelveTodos() {
            ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
            repositorio.agregar(p1);
            assertEquals(1, servicio.buscarProductos("   ").size());
        }
    @Test
    public void buscarProductosCoincidencia() {
        ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
        ProductoDTO p2 = new ProductoDTO(2L, "Refresco", "Bebida fría", "url2", null, "Menu", new BigDecimal("3.00"), true, "Activo");
        repositorio.agregar(p1);
        repositorio.agregar(p2);
        List<ProductoDTO> resultado = servicio.buscarProductos("Refresco");
        assertEquals(1, resultado.size());
        assertEquals("Refresco", resultado.get(0).getNombreProducto());
    }

    @Test
    public void buscarProductosVacioONuloDevuelveTodos() {
        ProductoDTO p1 = new ProductoDTO(1L, "Palomitas", "Saladas", "url1", null, "Menu", new BigDecimal("5.50"), true, "Activo");
        repositorio.agregar(p1);
        assertEquals(1, servicio.buscarProductos("").size());
        assertEquals(1, servicio.buscarProductos(null).size());
    }

    @Test
    public void crearProductoExitoso() {
        ProductoDTO p = new ProductoDTO(10L, "Nachos", "Con queso", "url", null, "Menu", new BigDecimal("7.00"), true, "Activo");
        ProductoDTO creado = servicio.crear(p);
        assertEquals("Nachos", creado.getNombreProducto());
        assertEquals(new BigDecimal("7.00"), creado.getPrecioLista());
    }

    @Test
    public void crearProductoNombreVacioLanzaExcepcion() {
        ProductoDTO p = new ProductoDTO(11L, "", "desc", "url", null, "Menu", new BigDecimal("2.00"), true, "Activo");
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(p));
    }

    @Test
    public void crearProductoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(null));
    }

    @Test
    public void actualizarProductoExitoso() {
        ProductoDTO original = new ProductoDTO(20L, "Chocolates", "desc", "url", null, "Menu", new BigDecimal("4.00"), true, "Activo");
        repositorio.agregar(original);
        ProductoDTO cambios = new ProductoDTO(20L, "Chocolates Premium", "desc2", "url2", null, "Menu", new BigDecimal("6.00"), true, "Activo");
        assertDoesNotThrow(() -> servicio.actualizar(20L, cambios));
        List<ProductoDTO> lista = servicio.listarTodas();
        assertEquals("Chocolates Premium", lista.get(0).getNombreProducto());
        assertEquals(new BigDecimal("6.00"), lista.get(0).getPrecioLista());
    }

    @Test
    public void actualizarProductoIdInvalidoLanzaExcepcion() {
        ProductoDTO cambios = new ProductoDTO(0L, "Nuevo", "desc", "url", null, "Menu", new BigDecimal("1.00"), true, "Activo");
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(0L, cambios));
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(null, cambios));
    }

    @Test
    public void actualizarProductoNombreVacioLanzaExcepcion() {
        ProductoDTO cambios = new ProductoDTO(21L, "", "desc", "url", null, "Menu", new BigDecimal("1.00"), true, "Activo");
        repositorio.agregar(cambios);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(21L, cambios));
    }

    @Test
    public void eliminarProductoExitoso() {
        ProductoDTO p = new ProductoDTO(30L, "Galletas", "desc", "url", null, "Menu", new BigDecimal("2.50"), true, "Activo");
        repositorio.agregar(p);
        assertDoesNotThrow(() -> servicio.eliminar(30L));
        assertTrue(servicio.listarTodas().isEmpty());
    }

    @Test
    public void eliminarProductoIdInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminar(0L));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminar(null));
    }

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

        @Override
        public ProductoDTO guardarNuevo(ProductoDTO dto) {
            productos.add(dto);
            return dto;
        }

        @Override
        public List<ProductoDTO> buscarPorCriterio(String q) {
            List<ProductoDTO> resultado = new ArrayList<>();
            for (ProductoDTO p : productos) {
                if ((p.getNombreProducto() != null && p.getNombreProducto().contains(q)) ||
                    (p.getDescripcionProducto() != null && p.getDescripcionProducto().contains(q))) {
                    resultado.add(p);
                }
            }
            return resultado;
        }

        @Override
        public void actualizar(Long id, ProductoDTO cambios) {
            for (int i = 0; i < productos.size(); i++) {
                if (productos.get(i).getProductoId().equals(id)) {
                    productos.set(i, cambios);
                    break;
                }
            }
        }

        @Override
        public void eliminar(Long id) {
            productos.removeIf(p -> p.getProductoId().equals(id));
        }
    }
}
