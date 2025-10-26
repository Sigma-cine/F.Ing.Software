package sigmacine.dominio.repository;

import java.util.List;
import sigmacine.dominio.entity.Pelicula;

public interface PeliculaRepository {
    List<Pelicula> buscarPorTitulo(String titulo);
    List<Pelicula> buscarPorGenero(String genero);
    List<Pelicula> buscarTodas();
}
