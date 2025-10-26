package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import sigmacine.dominio.entity.Pelicula;
import static org.junit.jupiter.api.Assertions.*;

public class BuscarPeliculaServiceTest {

    @Test
    public void buscarPorTitulo_caseInsensitive_and_matches() {
        List<Pelicula> list = new ArrayList<>();
        Pelicula p1 = new Pelicula(1, "Star Wars", "SciFi", "PG-13", 120, "Lucas", "Activo");
        Pelicula p2 = new Pelicula(2, "La guerra", "Accion", "PG", 100, "Otro", "Activo");
        list.add(p1); list.add(p2);

        BuscarPeliculaService svc = new BuscarPeliculaService(list);
        List<Pelicula> res = svc.buscarPorTitulo("star");
        assertEquals(1, res.size());
        assertEquals("Star Wars", res.get(0).getTitulo());
    }

    @Test
    public void buscarPorGenero_matches_ignoreCase() {
        List<Pelicula> list = new ArrayList<>();
        Pelicula p1 = new Pelicula(1, "A", "Drama", "G", 90, "D", "Activo");
        Pelicula p2 = new Pelicula(2, "B", "drama", "G", 80, "E", "Activo");
        Pelicula p3 = new Pelicula(3, "C", "Accion", "G", 80, "F", "Activo");
        list.add(p1); list.add(p2); list.add(p3);

        BuscarPeliculaService svc = new BuscarPeliculaService(list);
        List<Pelicula> res = svc.buscarPorGenero("DRAMA");
        assertEquals(2, res.size());
    }

    @Test
    public void buscarTodas_returns_copy() {
        List<Pelicula> list = new ArrayList<>();
        list.add(new Pelicula(1, "X", "G", "C", 1, "D", "Activo"));
        BuscarPeliculaService svc = new BuscarPeliculaService(list);
        List<Pelicula> copy = svc.buscarTodas();
        assertEquals(1, copy.size());
        copy.clear();
        assertEquals(1, list.size());
    }
}
