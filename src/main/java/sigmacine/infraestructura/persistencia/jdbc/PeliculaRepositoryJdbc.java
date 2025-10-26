package sigmacine.infraestructura.persistencia.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.PeliculaRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.Mapper.PeliculaMapper;



public class PeliculaRepositoryJdbc implements PeliculaRepository {
    private final DatabaseConfig db;

    public PeliculaRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }


    @Override
    public List<Pelicula> buscarPorTitulo(String q) {
        String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL,SINOPSIS,REPARTO " +
                    "FROM PELICULA WHERE UPPER(TITULO) LIKE UPPER(?)";
        try (Connection cn = db.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%"+q+"%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Pelicula> out = new ArrayList<>();
                while (rs.next()){
                    out.add(PeliculaMapper.map(rs));
                    }
                return out;
            }
        } catch (SQLException e) {
            // Si la tabla no existe (por ejemplo en un entorno sin inicializar), no propagamos la excepción
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("table") && msg.contains("not found")) {
                System.err.println("PeliculaRepositoryJdbc: tabla PELICULA no encontrada - devolviendo lista vacía");
                return new ArrayList<>();
            }
            throw new RuntimeException("Error en la buscando por título", e);
        }
    }

    @Override
    public List<Pelicula> buscarPorGenero(String genero){
    String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL,SINOPSIS,REPARTO " +
            "FROM PELICULA WHERE UPPER(GENERO) LIKE UPPER(?)";
        try (Connection cn = db.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, "%"+genero+"%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Pelicula> out = new ArrayList<>();
                while (rs.next()){ 
                    out.add(PeliculaMapper.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("table") && msg.contains("not found")) {
                System.err.println("PeliculaRepositoryJdbc: tabla PELICULA no encontrada (genero) - devolviendo lista vacía");
                return new ArrayList<>();
            }
            throw new RuntimeException("Error en la busqueda por genero", e);
        }

    }
    
    @Override
    public List<Pelicula> buscarTodas(){
    String sql = "SELECT ID,TITULO,GENERO,CLASIFICACION,DURACION,DIRECTOR,ESTADO,POSTER_URL,SINOPSIS,REPARTO " +
            "FROM PELICULA";
        try (Connection cn = db.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            List<Pelicula> out = new ArrayList<>();
            while (rs.next()){ 
                out.add(PeliculaMapper.map(rs));
            }
            return out;

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("table") && msg.contains("not found")) {
                System.err.println("PeliculaRepositoryJdbc: tabla PELICULA no encontrada (buscarTodas) - devolviendo lista vacía");
                return new ArrayList<>();
            }
            throw new RuntimeException("Error en el listado de las peliculas", e);
        }
    }
}
