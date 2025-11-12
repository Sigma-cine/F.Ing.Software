package sigmacine.aplicacion.facade;

import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.dominio.repository.PeliculaRepository;
import sigmacine.dominio.repository.admi.PeliculaAdminRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.admi.PeliculaAdminRepositoryJdbc;

public final class AdminServiciosFactory {

    private AdminServiciosFactory() {}

    public static GestionPeliculasService crearGestionPeliculasService(DatabaseConfig dbConfig) {
        PeliculaRepository repoLectura = new PeliculaRepositoryJdbc(dbConfig);
        PeliculaAdminRepository repoAdmin = new PeliculaAdminRepositoryJdbc(dbConfig);
        return new GestionPeliculasService(repoLectura, repoAdmin);
    }
}
