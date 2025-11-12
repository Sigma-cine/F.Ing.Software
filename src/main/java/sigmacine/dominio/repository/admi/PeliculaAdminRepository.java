package sigmacine.dominio.repository.admi;

import sigmacine.dominio.entity.Pelicula;

public interface PeliculaAdminRepository {
    Pelicula crearPelicula(Pelicula peliculaNueva);
    Pelicula actualizarPelicula(int idPelicula, Pelicula datosActualizados);
    boolean eliminarPelicula(int idPelicula); // false si hay funciones futuras
}
