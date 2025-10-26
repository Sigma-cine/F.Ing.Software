package sigmacine.dominio.repository;

import java.util.List;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;

public interface FuncionRepository {
    List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId);
}
