package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.dominio.repository.FuncionRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FuncionRepositoryJdbc implements FuncionRepository {

    private final DatabaseConfig db;

    public FuncionRepositoryJdbc(DatabaseConfig db) { this.db = db; }

    @Override
    public List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId) {
        String sql = "SELECT f.ID AS FUNCION_ID, f.FECHA, f.HORA, se.CIUDAD, se.NOMBRE AS SEDE, sa.NUMERO_SALA, sa.TIPO " +
                "FROM FUNCION f " +
                "JOIN SALA sa ON sa.ID = f.SALA_ID " +
                "JOIN SEDE se ON se.ID = sa.SEDE_ID " +
                "WHERE f.PELICULA_ID = ? " +
                "ORDER BY se.CIUDAD, se.NOMBRE, f.FECHA, f.HORA";
        try (Connection cn = db.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, peliculaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<FuncionDisponibleDTO> out = new ArrayList<>();
                while (rs.next()) {
                    long id = rs.getLong("FUNCION_ID");
                    LocalDate fecha = rs.getDate("FECHA").toLocalDate();
                    LocalTime hora = rs.getTime("HORA").toLocalTime();
                    String ciudad = rs.getString("CIUDAD");
                    String sede = rs.getString("SEDE");
                    int numSala = rs.getInt("NUMERO_SALA");
                    String tipo = rs.getString("TIPO");
                    out.add(new FuncionDisponibleDTO(id, peliculaId, fecha, hora, ciudad, sede, numSala, tipo));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando funciones por película", e);
        }
    }
}
