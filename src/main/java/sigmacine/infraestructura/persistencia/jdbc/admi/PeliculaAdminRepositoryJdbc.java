package sigmacine.infraestructura.persistencia.jdbc.admi;

import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.admi.PeliculaAdminRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.sql.*;

public class PeliculaAdminRepositoryJdbc implements PeliculaAdminRepository {

    private final DatabaseConfig databaseConfig;

    public PeliculaAdminRepositoryJdbc(DatabaseConfig databaseConfig) {
        if (databaseConfig == null) throw new IllegalArgumentException("DatabaseConfig nulo");
        this.databaseConfig = databaseConfig;
    }

    @Override
    public Pelicula crearPelicula(Pelicula peliculaNueva) {
        long nuevoId = obtenerSiguienteIdPelicula();
        peliculaNueva.setId((int) nuevoId);

        final String sqlInsert =
            "INSERT INTO PELICULA (ID, TITULO, GENERO, CLASIFICACION, DURACION, DIRECTOR, REPARTO, TRAILER, SINOPSIS, ESTADO, POSTER_URL, ESTADO_BOOL) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection cnn = databaseConfig.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sqlInsert)) {

            ps.setLong(1, nuevoId);
            ps.setString(2, peliculaNueva.getTitulo());
            ps.setString(3, peliculaNueva.getGenero());
            ps.setString(4, peliculaNueva.getClasificacion());
            ps.setObject(5, peliculaNueva.getDuracion(), Types.INTEGER);
            ps.setString(6, peliculaNueva.getDirector());
            ps.setString(7, peliculaNueva.getReparto());
            ps.setString(8, peliculaNueva.getTrailer());
            ps.setString(9, peliculaNueva.getSinopsis());
            ps.setString(10, peliculaNueva.getEstado());
            ps.setString(11, peliculaNueva.getPosterUrl());
            ps.setObject(12, peliculaNueva.getEstado() != null && peliculaNueva.getEstado().equalsIgnoreCase("ACTIVA"), Types.BOOLEAN);

            ps.executeUpdate();
            return peliculaNueva;
        } catch (SQLException e) {
            throw new RuntimeException("Error creando película", e);
        }
    }

    @Override
    public Pelicula actualizarPelicula(int idPelicula, Pelicula datosActualizados) {
        final String sqlUpdate =
            "UPDATE PELICULA SET TITULO=?, GENERO=?, CLASIFICACION=?, DURACION=?, DIRECTOR=?, REPARTO=?, TRAILER=?, SINOPSIS=?, ESTADO=?, POSTER_URL=?, ESTADO_BOOL=? " +
            "WHERE ID=?";
        try (Connection cnn = databaseConfig.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sqlUpdate)) {

            ps.setString(1, datosActualizados.getTitulo());
            ps.setString(2, datosActualizados.getGenero());
            ps.setString(3, datosActualizados.getClasificacion());
            ps.setObject(4, datosActualizados.getDuracion(), Types.INTEGER);
            ps.setString(5, datosActualizados.getDirector());
            ps.setString(6, datosActualizados.getReparto());
            ps.setString(7, datosActualizados.getTrailer());
            ps.setString(8, datosActualizados.getSinopsis());
            ps.setString(9, datosActualizados.getEstado());
            ps.setString(10, datosActualizados.getPosterUrl());
            ps.setObject(11, datosActualizados.getEstado() != null && datosActualizados.getEstado().equalsIgnoreCase("ACTIVA"), Types.BOOLEAN);
            ps.setLong(12, idPelicula);

            ps.executeUpdate();
            datosActualizados.setId(idPelicula);
            return datosActualizados;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando película", e);
        }
    }

    @Override
public boolean eliminarPelicula(int idPelicula) {
    // No permitir borrar si hay funciones (pasadas o futuras)
    if (existenFuncionesParaPelicula(idPelicula)) {
        return false;
    }

    final String sqlDelete = "DELETE FROM PELICULA WHERE ID=?";
    try (Connection cnn = databaseConfig.getConnection();
         PreparedStatement ps = cnn.prepareStatement(sqlDelete)) {

        ps.setLong(1, idPelicula);
        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        // Cualquier violación de integridad (FK) -> devolver false, no romper la UI
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return false;
        }
        throw new RuntimeException("Error eliminando película", e);
    }
}

    // Helpers
    private long obtenerSiguienteIdPelicula() {
        final String sql = "SELECT COALESCE(MAX(ID), 0) + 1 AS NEXT_ID FROM PELICULA";
        try (Connection cnn = databaseConfig.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("NEXT_ID");
            return 1L;
        } catch (SQLException e) {
            return 1L;
        }
    }

  private boolean existenFuncionesParaPelicula(int idPelicula) {
    final String sql =
        "SELECT 1 FROM FUNCION WHERE PELICULA_ID=? FETCH FIRST 1 ROWS ONLY";
    try (Connection cnn = databaseConfig.getConnection();
         PreparedStatement ps = cnn.prepareStatement(sql)) {
        ps.setLong(1, idPelicula);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        return true;
    }
}
}