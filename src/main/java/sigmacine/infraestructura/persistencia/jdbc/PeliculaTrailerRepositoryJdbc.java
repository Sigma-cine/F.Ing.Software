package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.dominio.entity.PeliculaTrailer;
import sigmacine.dominio.repository.PeliculaTrailerRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeliculaTrailerRepositoryJdbc implements PeliculaTrailerRepository {
    
    private final DatabaseConfig databaseConfig;

    public PeliculaTrailerRepositoryJdbc(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public List<PeliculaTrailer> obtenerTrailersPorPelicula(long peliculaId) {
        List<PeliculaTrailer> trailers = new ArrayList<>();
        String sql = "SELECT ID, PELICULA_ID, URL FROM PELICULA_TRAILER WHERE PELICULA_ID = ?";
        
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, peliculaId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PeliculaTrailer trailer = new PeliculaTrailer();
                    trailer.setId(resultSet.getLong("ID"));
                    trailer.setPeliculaId(resultSet.getLong("PELICULA_ID"));
                    trailer.setUrl(resultSet.getString("URL"));
                    trailers.add(trailer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trailers;
    }
}