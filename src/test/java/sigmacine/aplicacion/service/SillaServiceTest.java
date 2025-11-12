package sigmacine.aplicacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.dominio.repository.SillaRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SillaServiceTest {

    private SillaService sillaService;
    private SillaRepositoryFake repositorioFalso;

    @BeforeEach
    public void preparar() {
        repositorioFalso = new SillaRepositoryFake();
        sillaService = new SillaService(repositorioFalso);
    }

    @Test
    public void obtenerOcupados() {
        Set<String> asientos = new HashSet<>();
        asientos.add("A1");
        asientos.add("A2");
        asientos.add("B3");
        repositorioFalso.setOcupados(1L, asientos);
        
        Set<String> resultado = sillaService.obtenerAsientosOcupados(1L);
        
        assertEquals(3, resultado.size());
        assertTrue(resultado.contains("A1"));
        assertTrue(resultado.contains("A2"));
        assertTrue(resultado.contains("B3"));
    }

    @Test
    public void obtenerOcupadosVacio() {
        repositorioFalso.setOcupados(1L, new HashSet<>());
        
        Set<String> resultado = sillaService.obtenerAsientosOcupados(1L);
        
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void obtenerOcupadosConIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            sillaService.obtenerAsientosOcupados(null);
        });
    }

    @Test
    public void obtenerAccesibles() {
        Set<String> asientos = new HashSet<>();
        asientos.add("A1");
        asientos.add("H10");
        repositorioFalso.setAccesibles(2L, asientos);
        
        Set<String> resultado = sillaService.obtenerAsientosAccesibles(2L);
        
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains("A1"));
        assertTrue(resultado.contains("H10"));
    }

    @Test
    public void obtenerAccesiblesVacio() {
        repositorioFalso.setAccesibles(3L, new HashSet<>());
        
        Set<String> resultado = sillaService.obtenerAsientosAccesibles(3L);
        
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void obtenerAccesiblesConIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            sillaService.obtenerAsientosAccesibles(null);
        });
    }

    @Test
    public void llamadasMultiples() {
        Set<String> ocupados = new HashSet<>();
        ocupados.add("A1");
        Set<String> accesibles = new HashSet<>();
        accesibles.add("H1");
        
        repositorioFalso.setOcupados(1L, ocupados);
        repositorioFalso.setAccesibles(1L, accesibles);
        
        Set<String> resultadoOcupados = sillaService.obtenerAsientosOcupados(1L);
        Set<String> resultadoAccesibles = sillaService.obtenerAsientosAccesibles(1L);
        
        assertEquals(1, resultadoOcupados.size());
        assertEquals(1, resultadoAccesibles.size());
        assertFalse(resultadoOcupados.contains("H1"));
        assertFalse(resultadoAccesibles.contains("A1"));
    }

    @Test
    public void funcionesDiferentes() {
        Set<String> ocupados1 = new HashSet<>();
        ocupados1.add("A1");
        ocupados1.add("A2");
        
        Set<String> ocupados2 = new HashSet<>();
        ocupados2.add("C1");
        
        repositorioFalso.setOcupados(1L, ocupados1);
        repositorioFalso.setOcupados(2L, ocupados2);
        
        assertEquals(2, sillaService.obtenerAsientosOcupados(1L).size());
        assertEquals(1, sillaService.obtenerAsientosOcupados(2L).size());
    }

    // Repositorio falso para pruebas
    private static class SillaRepositoryFake implements SillaRepository {
        private Map<Long, Set<String>> ocupados = new HashMap<>();
        private Map<Long, Set<String>> accesibles = new HashMap<>();

        public void setOcupados(Long funcionId, Set<String> asientos) {
            ocupados.put(funcionId, asientos);
        }

        public void setAccesibles(Long funcionId, Set<String> asientos) {
            accesibles.put(funcionId, asientos);
        }

        @Override
        public Set<String> obtenerAsientosOcupadosPorFuncion(Long funcionId) {
            return ocupados.getOrDefault(funcionId, new HashSet<>());
        }

        @Override
        public Set<String> obtenerAsientosAccesiblesPorFuncion(Long funcionId) {
            return accesibles.getOrDefault(funcionId, new HashSet<>());
        }
    }
}
