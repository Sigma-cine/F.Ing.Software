package sigmacine.aplicacion.service.admi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.PeliculaDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.PeliculaRepository;
import sigmacine.dominio.repository.admi.PeliculaAdminRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GestionPeliculasServiceTest {
        @Test
        public void convertirAEntidadConDuracionNullNoLanzaExcepcion() {
            PeliculaDTO dto = crearDTO(1, "Titulo", "Genero", "PG", 120);
            dto.setDuracionMinutos(null);
            // No debe lanzar excepción, simplemente no setea duración
            assertDoesNotThrow(() -> {
                // Acceso por reflexión para probar método privado
                try {
                    var method = GestionPeliculasService.class.getDeclaredMethod("convertirA_Entidad", PeliculaDTO.class);
                    method.setAccessible(true);
                    Pelicula entidad = (Pelicula) method.invoke(servicio, dto);
                    assertNotNull(entidad);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Test
        public void convertirAEntidadConDuracionNoNullSeteaDuracion() {
            PeliculaDTO dto = crearDTO(2, "Titulo", "Genero", "PG", 150);
            // No debe lanzar excepción y debe setear duración
            assertDoesNotThrow(() -> {
                try {
                    var method = GestionPeliculasService.class.getDeclaredMethod("convertirA_Entidad", PeliculaDTO.class);
                    method.setAccessible(true);
                    Pelicula entidad = (Pelicula) method.invoke(servicio, dto);
                    assertNotNull(entidad);
                    assertEquals(150, entidad.getDuracion());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    @Test
    public void actualizarPeliculaDTONuloLanzaExcepcion() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarPeliculaExistente(creada.getIdPelicula(), null));
    }

    @Test
    public void actualizarPeliculaTituloSoloEspaciosLanzaExcepcion() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "   ", "Drama", "PG", 90);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios));
    }

    @Test
    public void actualizarPeliculaClasificacionSoloEspaciosLanzaExcepcion() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "Original", "Drama", "   ", 90);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios));
    }

    @Test
    public void actualizarPeliculaDuracionNulaLanzaExcepcion() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "Original", "Drama", "PG", 90);
        cambios.setDuracionMinutos(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios));
    }

    @Test
    public void actualizarPeliculaDuracionNegativaLanzaExcepcion() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "Original", "Drama", "PG", -10);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios));
    }

    @Test
    public void actualizarPeliculaEstadoNuloAsignaActiva() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "Original", "Drama", "PG", 90);
        cambios.setEstadoPelicula(null);
        PeliculaDTO actualizada = servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios);
        assertEquals("ACTIVA", actualizada.getEstadoPelicula());
    }

    @Test
    public void actualizarPeliculaEstadoSoloEspaciosAsignaActiva() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        PeliculaDTO cambios = crearDTO(creada.getIdPelicula(), "Original", "Drama", "PG", 90);
        cambios.setEstadoPelicula("   ");
        PeliculaDTO actualizada = servicio.actualizarPeliculaExistente(creada.getIdPelicula(), cambios);
        assertEquals("ACTIVA", actualizada.getEstadoPelicula());
    }

    @Test
    public void eliminarPeliculaIdInexistenteDevuelveFalse() {
        boolean eliminado = servicio.eliminarPeliculaPorId(9999);
        assertFalse(eliminado);
    }

    private GestionPeliculasService servicio;
    private PeliculaRepositoryFake consultaRepo;
    private PeliculaAdminRepositoryFake adminRepo;

    @BeforeEach
    public void preparar() {
        consultaRepo = new PeliculaRepositoryFake();
        adminRepo = new PeliculaAdminRepositoryFake();
        servicio = new GestionPeliculasService(consultaRepo, adminRepo);
    }

    @Test
    public void obtenerTodas() {
        Pelicula p1 = crearPelicula(1, "Película 1", "Acción", "PG-13", 120);
        Pelicula p2 = crearPelicula(2, "Película 2", "Drama", "R", 90);
        consultaRepo.agregar(p1);
        consultaRepo.agregar(p2);
        
        List<PeliculaDTO> resultado = servicio.obtenerTodasLasPeliculas();
        
        assertEquals(2, resultado.size());
        assertEquals("Película 1", resultado.get(0).getTituloPelicula());
        assertEquals("Película 2", resultado.get(1).getTituloPelicula());
    }

    @Test
    public void buscarPorTitulo() {
        Pelicula p1 = crearPelicula(1, "Star Wars", "SciFi", "PG", 120);
        Pelicula p2 = crearPelicula(2, "Avatar", "SciFi", "PG-13", 150);
        consultaRepo.agregar(p1);
        consultaRepo.agregar(p2);
        
        List<PeliculaDTO> resultado = servicio.buscarPeliculasPorTitulo("Star");
        
        assertEquals(1, resultado.size());
        assertEquals("Star Wars", resultado.get(0).getTituloPelicula());
    }

    @Test
    public void buscarPorGenero() {
        Pelicula p1 = crearPelicula(1, "A", "Drama", "PG", 90);
        Pelicula p2 = crearPelicula(2, "B", "Drama", "R", 100);
        Pelicula p3 = crearPelicula(3, "C", "Acción", "PG-13", 110);
        consultaRepo.agregar(p1);
        consultaRepo.agregar(p2);
        consultaRepo.agregar(p3);
        
        List<PeliculaDTO> resultado = servicio.buscarPeliculasPorGenero("Drama");
        
        assertEquals(2, resultado.size());
    }

    @Test
    public void crearPelicula() {
        PeliculaDTO dto = crearDTO(null, "Nueva Película", "Comedia", "PG", 95);
        
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        
        assertNotNull(creada);
        assertNotNull(creada.getIdPelicula());
        assertEquals("Nueva Película", creada.getTituloPelicula());
        assertEquals("Comedia", creada.getGeneroPelicula());
    }

    @Test
    public void actualizarPelicula() {
        PeliculaDTO dto = crearDTO(null, "Original", "Drama", "PG", 90);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        
        PeliculaDTO actualizada = crearDTO(creada.getIdPelicula(), "Actualizada", "Acción", "R", 120);
        PeliculaDTO resultado = servicio.actualizarPeliculaExistente(creada.getIdPelicula(), actualizada);
        
        assertNotNull(resultado);
        assertEquals("Actualizada", resultado.getTituloPelicula());
        assertEquals("Acción", resultado.getGeneroPelicula());
    }

    @Test
    public void eliminarPelicula() {
        PeliculaDTO dto = crearDTO(null, "A Eliminar", "Horror", "R", 100);
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        
        boolean eliminado = servicio.eliminarPeliculaPorId(creada.getIdPelicula());
        
        assertTrue(eliminado);
    }

    @Test
    public void validacionTituloVacio() {
        PeliculaDTO dto = crearDTO(null, "", "Drama", "PG", 90);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionTituloNulo() {
        PeliculaDTO dto = crearDTO(null, null, "Drama", "PG", 90);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionClasificacionNula() {
        PeliculaDTO dto = crearDTO(null, "Título", "Drama", null, 90);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionDuracionCero() {
        PeliculaDTO dto = crearDTO(null, "Título", "Drama", "PG", 0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionDuracionNegativa() {
        PeliculaDTO dto = crearDTO(null, "Título", "Drama", "PG", -10);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionDTONulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(null);
        });
    }

    @Test
    public void constructorConRepoNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new GestionPeliculasService(null, adminRepo);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GestionPeliculasService(consultaRepo, null);
        });
    }

    @Test
    public void validacionDuracionNula() {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setTituloPelicula("Título");
        dto.setClasificacionPelicula("PG");
        dto.setDuracionMinutos(null);
        dto.setEstadoPelicula("ACTIVA");
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionClasificacionVacia() {
        PeliculaDTO dto = crearDTO(null, "Título", "Drama", "", 120);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionTituloConEspacios() {
        PeliculaDTO dto = crearDTO(null, "   ", "Drama", "PG", 120);
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearNuevaPelicula(dto);
        });
    }

    @Test
    public void validacionEstadoNuloAsignaActiva() {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setTituloPelicula("Película Test");
        dto.setClasificacionPelicula("PG");
        dto.setDuracionMinutos(120);
        dto.setEstadoPelicula(null);
        
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        
        assertEquals("ACTIVA", creada.getEstadoPelicula());
    }

    @Test
    public void validacionEstadoVacioAsignaActiva() {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setTituloPelicula("Película Test 2");
        dto.setClasificacionPelicula("PG");
        dto.setDuracionMinutos(120);
        dto.setEstadoPelicula("  ");
        
        PeliculaDTO creada = servicio.crearNuevaPelicula(dto);
        
        assertEquals("ACTIVA", creada.getEstadoPelicula());
    }

    private Pelicula crearPelicula(int id, String titulo, String genero, String clasificacion, int duracion) {
        Pelicula p = new Pelicula(id, titulo, genero, clasificacion, duracion, "Director", "Activo");
        return p;
    }

    private PeliculaDTO crearDTO(Integer id, String titulo, String genero, String clasificacion, int duracion) {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setIdPelicula(id);
        dto.setTituloPelicula(titulo);
        dto.setGeneroPelicula(genero);
        dto.setClasificacionPelicula(clasificacion);
        dto.setDuracionMinutos(duracion);
        dto.setEstadoPelicula("ACTIVA");
        return dto;
    }

    // Repositorios feiks para pruebas
    private static class PeliculaRepositoryFake implements PeliculaRepository {
        private final List<Pelicula> peliculas = new ArrayList<>();

        public void agregar(Pelicula p) {
            peliculas.add(p);
        }

        @Override
        public List<Pelicula> buscarTodas() {
            return new ArrayList<>(peliculas);
        }

        @Override
        public List<Pelicula> buscarPorTitulo(String texto) {
            List<Pelicula> resultado = new ArrayList<>();
            for (Pelicula p : peliculas) {
                if (p.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                    resultado.add(p);
                }
            }
            return resultado;
        }

        @Override
        public List<Pelicula> buscarPorGenero(String genero) {
            List<Pelicula> resultado = new ArrayList<>();
            for (Pelicula p : peliculas) {
                if (p.getGenero().equalsIgnoreCase(genero)) {
                    resultado.add(p);
                }
            }
            return resultado;
        }
    }

    private static class PeliculaAdminRepositoryFake implements PeliculaAdminRepository {
        private final Map<Integer, Pelicula> peliculas = new HashMap<>();
        private int siguienteId = 1;

        @Override
        public Pelicula crearPelicula(Pelicula p) {
            p.setId(siguienteId++);
            peliculas.put(p.getId(), p);
            return p;
        }

        @Override
        public Pelicula actualizarPelicula(int id, Pelicula p) {
            p.setId(id);
            peliculas.put(id, p);
            return p;
        }

        @Override
        public boolean eliminarPelicula(int id) {
            return peliculas.remove(id) != null;
        }
    }
}
