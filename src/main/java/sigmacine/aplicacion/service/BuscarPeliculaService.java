package sigmacine.aplicacion.service;

import sigmacine.dominio.entity.Pelicula;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuscarPeliculaService {
    private final List<Pelicula> peliculas; 

    public BuscarPeliculaService(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }

    public List<Pelicula> buscarPorTitulo(String titulo) {
        String texto = titulo.toLowerCase();
        return peliculas.stream()
            .filter(p -> p.getTitulo() != null && p.getTitulo().toLowerCase().contains(texto))
            .collect(Collectors.toList());
    }

    public List<Pelicula> buscarPorGenero(String genero) {
        return peliculas.stream()
            .filter(p -> p.getGenero().equalsIgnoreCase(genero))
            .collect(Collectors.toList());
    }

    public List<Pelicula> buscarTodas() {
        return new ArrayList<>(peliculas);
    }
}