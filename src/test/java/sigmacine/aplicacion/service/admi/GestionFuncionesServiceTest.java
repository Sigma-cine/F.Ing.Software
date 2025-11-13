package sigmacine.aplicacion.service.admi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.aplicacion.data.FuncionFormDTO;
import sigmacine.dominio.entity.Funcion;
import sigmacine.dominio.repository.admi.FuncionAdminRepository;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GestionFuncionesServiceTest {

    private GestionFuncionesService servicio;
    private FuncionRepositoryFake repositorio;

    @BeforeEach
    public void preparar() {
        repositorio = new FuncionRepositoryFake();
        servicio = new GestionFuncionesService(repositorio);
    }

    @Test
    public void crearFuncionValida() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:30", 1L, 1L);
        
        Funcion resultado = servicio.crear(dto);
        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getPeliculaId());
        assertEquals(1L, resultado.getSalaId());
    }

    @Test
    public void crearConTraslape() {
        FuncionFormDTO dto1 = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        servicio.crear(dto1);
        
        repositorio.setTraslape(true);
        FuncionFormDTO dto2 = crearDTO(0, "2025-12-01", "19:00", "02:00", 2L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto2);
        });
    }

    @Test
    public void actualizarFuncion() {
        FuncionFormDTO dtoCrear = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        Funcion creada = servicio.crear(dtoCrear);
        
        FuncionFormDTO dtoActualizar = crearDTO(creada.getId(), "2025-12-01", "20:00", "02:00", 1L, 1L);
        Funcion actualizada = servicio.actualizar(dtoActualizar);
        
        assertNotNull(actualizada);
        assertEquals(creada.getId(), actualizada.getId());
    }

    @Test
    public void eliminarFuncion() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        Funcion creada = servicio.crear(dto);
        
        boolean eliminado = servicio.eliminar(creada.getId());
        
        assertTrue(eliminado);
    }

    @Test
    public void buscarPorId() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        Funcion creada = servicio.crear(dto);
        
        Funcion encontrada = servicio.buscarPorId(creada.getId());
        
        assertNotNull(encontrada);
        assertEquals(creada.getId(), encontrada.getId());
    }

    @Test
    public void buscarPorIdInvalido() {
        assertThrows(RuntimeException.class, () -> {
            servicio.buscarPorId(0);
        });
    }

    @Test
    public void listarPorFechaYSala() {
        FuncionFormDTO dto1 = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        FuncionFormDTO dto2 = crearDTO(0, "2025-12-01", "21:00", "02:00", 2L, 1L);
        servicio.crear(dto1);
        servicio.crear(dto2);
        
        List<Funcion> resultado = servicio.listarPorFechaYSala("2025-12-01", 1L);
        
        assertEquals(2, resultado.size());
    }

    @Test
    public void validacionFechaNula() {
        FuncionFormDTO dto = crearDTO(0, null, "18:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionHoraNula() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", null, "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionPeliculaInvalida() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 0L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionSalaInvalida() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 0L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void constructorConRepoNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new GestionFuncionesService(null);
        });
    }

    @Test
    public void listarPorPelicula() {
        // Crear funciones con la misma película
        servicio.crear(crearDTO(0, "2024-12-20", "18:00", "02:00", 10L, 1L));
        servicio.crear(crearDTO(0, "2024-12-21", "20:00", "02:00", 10L, 2L));
        servicio.crear(crearDTO(0, "2024-12-22", "19:00", "02:00", 20L, 1L));
        
        List<FuncionDisponibleDTO> resultado = servicio.listarPorPelicula(10L);
        
        assertEquals(2, resultado.size());
    }

    @Test
    public void listarTodasFunciones() {
        servicio.crear(crearDTO(0, "2024-12-20", "18:00", "02:00", 10L, 1L));
        servicio.crear(crearDTO(0, "2024-12-21", "20:00", "02:00", 20L, 2L));
        
        List<FuncionDisponibleDTO> resultado = servicio.listarTodas();
        
        assertEquals(2, resultado.size());
    }

    @Test
    public void buscarFunciones() {
        servicio.crear(crearDTO(0, "2024-12-20", "18:00", "02:00", 10L, 1L));
        servicio.crear(crearDTO(0, "2024-12-21", "20:00", "02:00", 20L, 2L));
        
        List<FuncionDisponibleDTO> resultado = servicio.buscar("test");
        
        assertNotNull(resultado);
    }

    @Test
    public void buscarConQueryNulo() {
        servicio.crear(crearDTO(0, "2024-12-20", "18:00", "02:00", 10L, 1L));
        
        List<FuncionDisponibleDTO> resultado = servicio.buscar(null);
        
        assertNotNull(resultado);
    }

    @Test
    public void actualizarConIdInvalido() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.actualizar(dto);
        });
    }

    @Test
    public void actualizarConTraslape() {
        FuncionFormDTO dto1 = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        Funcion creada = servicio.crear(dto1);
        
        repositorio.setTraslape(true);
        FuncionFormDTO dtoActualizar = crearDTO(creada.getId(), "2025-12-01", "20:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.actualizar(dtoActualizar);
        });
    }

    @Test
    public void eliminarConIdInvalido() {
        assertThrows(RuntimeException.class, () -> {
            servicio.eliminar(0);
        });
    }

    @Test
    public void buscarPorIdNoEncontrado() {
        assertThrows(RuntimeException.class, () -> {
            servicio.buscarPorId(999);
        });
    }

    @Test
    public void listarPorFechaYSalaInvalida() {
        assertThrows(RuntimeException.class, () -> {
            servicio.listarPorFechaYSala("2025-12-01", 0);
        });
    }

    @Test
    public void listarPorPeliculaInvalida() {
        assertThrows(RuntimeException.class, () -> {
            servicio.listarPorPelicula(0);
        });
    }

    @Test
    public void validacionFechaInvalida() {
        FuncionFormDTO dto = crearDTO(0, "fecha-invalida", "18:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionHoraInvalida() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "hora-invalida", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionDuracionInvalida() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "duracion-invalida", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionIdNoDebeSerCeroAlCrear() {
        FuncionFormDTO dto = crearDTO(5, "2025-12-01", "18:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionDuracionNula() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", null, 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionFechaVacia() {
        FuncionFormDTO dto = crearDTO(0, "", "18:00", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionHoraVacia() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionDuracionVacia() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void validacionEstadoNuloSeAsignaActiva() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        dto.estado = null;
        
        Funcion resultado = servicio.crear(dto);
        
        assertNotNull(resultado);
    }

    @Test
    public void validacionEstadoVacioSeAsignaActiva() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "18:00", "02:00", 1L, 1L);
        dto.estado = "";
        
        Funcion resultado = servicio.crear(dto);
        
        assertNotNull(resultado);
    }

    @Test
    public void toTimeConFormatoInvalido() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "9:30", "02:00", 1L, 1L);
        
        assertThrows(RuntimeException.class, () -> {
            servicio.crear(dto);
        });
    }

    @Test
    public void toTimeConFormatoLargo() {
        FuncionFormDTO dto = crearDTO(0, "2025-12-01", "09:30:00", "02:00", 1L, 1L);
        
        Funcion resultado = servicio.crear(dto);
        
        assertNotNull(resultado);
    }

    private FuncionFormDTO crearDTO(long id, String fecha, String hora, String duracion, long peliculaId, long salaId) {
        FuncionFormDTO dto = new FuncionFormDTO();
        dto.id = id;
        dto.fecha = fecha;
        dto.hora = hora;
        dto.duracion = duracion;
        dto.estado = "Activa";
        dto.estadoBool = true;
        dto.peliculaId = peliculaId;
        dto.salaId = salaId;
        return dto;
    }

    // Repositorio feik para pruebas
    private static class FuncionRepositoryFake implements FuncionAdminRepository {
        private final Map<Long, Funcion> funciones = new HashMap<>();
        private long siguienteId = 1;
        private boolean traslape = false;

        public void setTraslape(boolean valor) {
            this.traslape = valor;
        }

        @Override
        public Funcion crear(Funcion f) {
            Funcion nueva = new Funcion(siguienteId++, f.getFecha(), f.getHora(), 
                                        f.getEstado(), f.getDuracion(), f.getEstadoBool(), 
                                        f.getPeliculaId(), f.getSalaId());
            funciones.put(nueva.getId(), nueva);
            return nueva;
        }

        @Override
        public Funcion actualizar(long id, Funcion f) {
            funciones.put(id, f);
            return f;
        }

        @Override
        public boolean eliminar(long id) {
            return funciones.remove(id) != null;
        }

        @Override
        public Funcion buscarPorId(long id) {
            return funciones.get(id);
        }

        @Override
        public List<Funcion> listarPorFechaYSala(Date fecha, long salaId) {
            List<Funcion> resultado = new ArrayList<>();
            for (Funcion f : funciones.values()) {
                if (f.getFecha().equals(fecha) && f.getSalaId() == salaId) {
                    resultado.add(f);
                }
            }
            return resultado;
        }

        @Override
        public List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId) {
            List<FuncionDisponibleDTO> resultado = new ArrayList<>();
            for (Funcion f : funciones.values()) {
                if (f.getPeliculaId() == peliculaId) {
                    resultado.add(convertirADTO(f));
                }
            }
            return resultado;
        }

        @Override
        public List<FuncionDisponibleDTO> listarTodas() {
            List<FuncionDisponibleDTO> resultado = new ArrayList<>();
            for (Funcion f : funciones.values()) {
                resultado.add(convertirADTO(f));
            }
            return resultado;
        }

        @Override
        public List<FuncionDisponibleDTO> buscar(String q) {
            List<FuncionDisponibleDTO> resultado = new ArrayList<>();
            for (Funcion f : funciones.values()) {
                resultado.add(convertirADTO(f));
            }
            return resultado;
        }

        private FuncionDisponibleDTO convertirADTO(Funcion f) {
            return new FuncionDisponibleDTO(
                f.getId(),
                f.getPeliculaId(),
                f.getFecha().toLocalDate(),
                f.getHora().toLocalTime(),
                "Bogotá",
                "Sede Centro",
                (int) f.getSalaId(),
                "Estándar",
                f.getSalaId()
            );
        }

        @Override
        public boolean existeTraslape(Date fecha, long salaId, Time hora, Time duracion) {
            return traslape;
        }

        @Override
        public boolean existeTraslapeExceptoId(Date fecha, long salaId, Time hora, Time duracion, long idExcluir) {
            return traslape;
        }
    }
}
