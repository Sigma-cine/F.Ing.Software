package sigmacine.dominio.repository;

import sigmacine.dominio.entity.PeliculaTrailer;
import java.util.List;

public interface PeliculaTrailerRepository {
    List<PeliculaTrailer> obtenerTrailersPorPelicula(long peliculaId);
}