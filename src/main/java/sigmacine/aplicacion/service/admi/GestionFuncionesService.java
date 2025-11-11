package sigmacine.aplicacion.service.admi;

import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.aplicacion.data.FuncionFormDTO;
import sigmacine.dominio.entity.Funcion;
import sigmacine.dominio.repository.admi.FuncionAdminRepository;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class GestionFuncionesService {

    private final FuncionAdminRepository repo;

    public GestionFuncionesService(FuncionAdminRepository repo) {
        if (repo == null) throw new IllegalArgumentException("repo nulo");
        this.repo = repo;
    }

    // ------- Comandos -------
    public Funcion crear(FuncionFormDTO dto) {
        validar(dto, false);
        Date fecha = Date.valueOf(LocalDate.parse(dto.fecha));
        Time hora = toTime(dto.hora);
        Time dur  = toTime(dto.duracion);

        if (repo.existeTraslape(fecha, dto.salaId, hora, dur)) {
            throw new RuntimeException("Ya existe una función que traslapa en esa sala/fecha/horario.");
        }
        Funcion f = new Funcion(0L, fecha, hora, dto.estado, dur, dto.estadoBool, dto.peliculaId, dto.salaId);
        return repo.crear(f);
    }

    public Funcion actualizar(FuncionFormDTO dto) {
        if (dto.id <= 0) throw new RuntimeException("Id requerido para actualizar");
        validar(dto, true);

        Date fecha = Date.valueOf(LocalDate.parse(dto.fecha));
        Time hora = toTime(dto.hora);
        Time dur  = toTime(dto.duracion);

        if (repo.existeTraslapeExceptoId(fecha, dto.salaId, hora, dur, dto.id)) {
            throw new RuntimeException("Traslape detectado al actualizar la función.");
        }
        Funcion f = new Funcion(dto.id, fecha, hora, dto.estado, dur, dto.estadoBool, dto.peliculaId, dto.salaId);
        return repo.actualizar(dto.id, f);
    }

    public boolean eliminar(long id) {
        if (id <= 0) throw new RuntimeException("Id inválido");
        return repo.eliminar(id);
    }

    // ------- Consultas -------
    public Funcion buscarPorId(long id) {
        if (id <= 0) throw new RuntimeException("Id inválido");
        Funcion f = repo.buscarPorId(id);
        if (f == null) throw new RuntimeException("Función no encontrada");
        return f;
    }

    public List<Funcion> listarPorFechaYSala(String fechaISO, long salaId) {
        if (salaId <= 0) throw new RuntimeException("Sala inválida");
        Date fecha = Date.valueOf(LocalDate.parse(fechaISO));
        return repo.listarPorFechaYSala(fecha, salaId);
    }

    public List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId) {
        if (peliculaId <= 0) throw new RuntimeException("Película inválida");
        return repo.listarPorPelicula(peliculaId);
    }

    public List<FuncionDisponibleDTO> listarTodas() {
        return repo.listarTodas();
    }

    public List<FuncionDisponibleDTO> buscar(String q) {
        if (q == null) q = "";
        return repo.buscar(q);
    }

    // ------- helpers -------
    private void validar(FuncionFormDTO dto, boolean isUpdate) {
        if (!isUpdate && dto.id != 0) throw new RuntimeException("El id de nueva función debe ser 0");
        if (dto.peliculaId <= 0) throw new RuntimeException("peliculaId requerido");
        if (dto.salaId <= 0) throw new RuntimeException("salaId requerido");
        if (dto.fecha == null || dto.fecha.trim().isEmpty()) throw new RuntimeException("fecha requerida (yyyy-MM-dd)");
        if (dto.hora == null || dto.hora.trim().isEmpty()) throw new RuntimeException("hora requerida (HH:mm)");
        if (dto.duracion == null || dto.duracion.trim().isEmpty()) throw new RuntimeException("duración requerida (HH:mm)");
        if (dto.estado == null || dto.estado.trim().isEmpty()) dto.estado = "Activa";

        try { LocalDate.parse(dto.fecha); } catch (Exception e) {
            throw new RuntimeException("fecha inválida: " + dto.fecha);
        }
        toTime(dto.hora);
        toTime(dto.duracion);
    }

    private Time toTime(String hhmm) {
        try {
            LocalTime t = LocalTime.parse(hhmm.length() >= 5 ? hhmm.substring(0, 5) : hhmm);
            return Time.valueOf(t);
        } catch (Exception e) {
            throw new RuntimeException("Hora/Duración inválida: " + hhmm);
        }
    }
}
