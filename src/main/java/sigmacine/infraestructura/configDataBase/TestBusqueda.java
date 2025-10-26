package sigmacine.infraestructura.configDataBase;

import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.dominio.entity.Pelicula;

import java.util.List;

public class TestBusqueda {
    public static void main(String[] args) {
        String termino = (args != null && args.length > 0) ? args[0] : "Dune";
        System.out.println("TestBusqueda: buscando por término: '" + termino + "'");
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> resultados = repo.buscarPorTitulo(termino);
            System.out.println("Resultados encontrados: " + (resultados == null ? 0 : resultados.size()));
            if (resultados != null) {
                for (Pelicula p : resultados) {
                    System.out.println(" - " + p.getTitulo() + " (id=" + p.getId() + ")");
                }
            }
        } catch (Exception ex) {
            System.err.println("Error en TestBusqueda: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
