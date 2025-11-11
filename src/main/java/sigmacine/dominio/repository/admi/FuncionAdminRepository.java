package sigmacine.dominio.repository.admi;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import sigmacine.dominio.entity.Funcion;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;

public interface FuncionAdminRepository {
    Funcion crear(Funcion f);
    Funcion actualizar(long id, Funcion f);
    boolean eliminar(long id);

    Funcion buscarPorId(long id);
    List<Funcion> listarPorFechaYSala(Date fecha, long salaId);
    boolean existeTraslape(Date fecha, long salaId, Time horaInicio, Time duracion);

    boolean existeTraslapeExceptoId(Date fecha, long salaId, Time horaInicio, Time duracion, long excluirId);

    List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId);
    List<FuncionDisponibleDTO> listarTodas();
    List<FuncionDisponibleDTO> buscar(String q);
}
