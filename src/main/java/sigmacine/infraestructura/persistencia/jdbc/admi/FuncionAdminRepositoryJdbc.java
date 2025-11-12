package sigmacine.infraestructura.persistencia.jdbc.admi;

import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.dominio.entity.Funcion;
import sigmacine.dominio.repository.admi.FuncionAdminRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionAdminRepositoryJdbc implements FuncionAdminRepository {

    private final DatabaseConfig db;

    public FuncionAdminRepositoryJdbc(DatabaseConfig db) {
        if (db == null) throw new IllegalArgumentException("DatabaseConfig nulo");
        this.db = db;
    }

    // ================= CRUD =================

    @Override
    public Funcion crear(Funcion f) {
        final String sql =
            "INSERT INTO FUNCION (ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long id = nextId();
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setDate(2, f.getFecha());
            ps.setTime(3, f.getHora());
            ps.setString(4, f.getEstado());
            ps.setTime(5, f.getDuracion());
            ps.setObject(6, f.getEstadoBool(), Types.BOOLEAN);
            ps.setLong(7, f.getPeliculaId());
            ps.setLong(8, f.getSalaId());
            ps.executeUpdate();
            f.setId(id);
            return f;
        } catch (SQLException e) {
            throw new RuntimeException("Error creando función", e);
        }
    }

    @Override
    public Funcion actualizar(long id, Funcion f) {
        final String sql =
            "UPDATE FUNCION SET FECHA=?, HORA=?, ESTADO=?, DURACION=?, ESTADO_BOOL=?, PELICULA_ID=?, SALA_ID=? WHERE ID=?";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setDate(1, f.getFecha());
            ps.setTime(2, f.getHora());
            ps.setString(3, f.getEstado());
            ps.setTime(4, f.getDuracion());
            ps.setObject(5, f.getEstadoBool(), Types.BOOLEAN);
            ps.setLong(6, f.getPeliculaId());
            ps.setLong(7, f.getSalaId());
            ps.setLong(8, id);
            ps.executeUpdate();
            f.setId(id);
            return f;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando función", e);
        }
    }

    @Override
    public boolean eliminar(long id) {
        final String sql = "DELETE FROM FUNCION WHERE ID=?";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (state != null && state.startsWith("23")) return false; // integridad referencial
            throw new RuntimeException("Error eliminando función", e);
        }
    }

    // ============== Validaciones de traslape ==============

    @Override
    public boolean existeTraslape(Date fecha, long salaId, Time horaInicio, Time duracion) {
        final String sql =
            "SELECT ID, HORA, DURACION " +
            "FROM FUNCION " +
            "WHERE FECHA=? AND SALA_ID=?";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {

            ps.setDate(1, fecha);
            ps.setLong(2, salaId);

            Time finNuevo = sumar(horaInicio, duracion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time ini = rs.getTime("HORA");
                    Time dur = rs.getTime("DURACION");
                    if (ini == null || dur == null) continue;

                    Time fin = sumar(ini, dur);

                    // [ini, fin) vs [horaInicio, finNuevo)
                    boolean solapan = ini.before(finNuevo) && fin.after(horaInicio);
                    if (solapan) return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return true; // conservador
        }
    }

    @Override
    public boolean existeTraslapeExceptoId(Date fecha, long salaId, Time horaInicio, Time duracion, long excluirId) {
        final String sql =
            "SELECT ID, HORA, DURACION " +
            "FROM FUNCION " +
            "WHERE FECHA=? AND SALA_ID=? AND ID<>?";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {

            ps.setDate(1, fecha);
            ps.setLong(2, salaId);
            ps.setLong(3, excluirId);

            Time finNuevo = sumar(horaInicio, duracion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time ini = rs.getTime("HORA");
                    Time dur = rs.getTime("DURACION");
                    if (ini == null || dur == null) continue;

                    Time fin = sumar(ini, dur);

                    boolean solapan = ini.before(finNuevo) && fin.after(horaInicio);
                    if (solapan) return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return true;
        }
    }

    /** Suma segura de hora + duración usando LocalTime */
    private Time sumar(Time inicio, Time duracion) {
        java.time.LocalTime tIni = inicio.toLocalTime();
        java.time.LocalTime tDur = duracion.toLocalTime();
        java.time.LocalTime fin =
            tIni.plusHours(tDur.getHour())
                .plusMinutes(tDur.getMinute())
                .plusSeconds(tDur.getSecond());
        return Time.valueOf(fin);
    }

    // ============== Consultas ==============

    @Override
    public Funcion buscarPorId(long id) {
        final String sql =
            "SELECT ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID " +
            "FROM FUNCION WHERE ID=?";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapFuncion(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando función", e);
        }
    }

    @Override
    public List<Funcion> listarPorFechaYSala(Date fecha, long salaId) {
        final String sql =
            "SELECT ID, FECHA, HORA, ESTADO, DURACION, ESTADO_BOOL, PELICULA_ID, SALA_ID " +
            "FROM FUNCION WHERE FECHA=? AND SALA_ID=? ORDER BY HORA";
        List<Funcion> out = new ArrayList<>();
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setDate(1, fecha);
            ps.setLong(2, salaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapFuncion(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando funciones por fecha/sala", e);
        }
        return out;
    }

    @Override
    public List<FuncionDisponibleDTO> listarPorPelicula(long peliculaId) {
        final String sql =
            "SELECT f.ID, f.FECHA, f.HORA, " +
            "       sd.CIUDAD AS CIUDAD, sd.NOMBRE AS SEDE, " +
            "       s.NUMERO_SALA, s.TIPO AS TIPO_SALA, " +
            "       f.SALA_ID, f.PELICULA_ID " +
            "FROM FUNCION f " +
            "JOIN SALA s   ON s.ID = f.SALA_ID " +
            "JOIN SEDE sd  ON sd.ID = s.SEDE_ID " +
            "WHERE f.PELICULA_ID=? " +
            "ORDER BY f.FECHA DESC, f.HORA DESC";
        List<FuncionDisponibleDTO> out = new ArrayList<>();
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setLong(1, peliculaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapDTO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando funciones por película", e);
        }
        return out;
    }

    @Override
    public List<FuncionDisponibleDTO> listarTodas() {
        final String sql =
            "SELECT f.ID, f.FECHA, f.HORA, " +
            "       sd.CIUDAD AS CIUDAD, sd.NOMBRE AS SEDE, " +
            "       s.NUMERO_SALA, s.TIPO AS TIPO_SALA, " +
            "       f.SALA_ID, f.PELICULA_ID " +
            "FROM FUNCION f " +
            "JOIN SALA s   ON s.ID = f.SALA_ID " +
            "JOIN SEDE sd  ON sd.ID = s.SEDE_ID " +
            "ORDER BY f.FECHA DESC, f.HORA DESC";
        List<FuncionDisponibleDTO> out = new ArrayList<>();
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapDTO(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listando funciones", e);
        }
        return out;
    }

    @Override
    public List<FuncionDisponibleDTO> buscar(String q) {
        final String like = "%" + (q == null ? "" : q.toLowerCase()) + "%";
        final String sql =
            "SELECT f.ID, f.FECHA, f.HORA, " +
            "       sd.CIUDAD AS CIUDAD, sd.NOMBRE AS SEDE, " +
            "       s.NUMERO_SALA, s.TIPO AS TIPO_SALA, " +
            "       f.SALA_ID, f.PELICULA_ID " +
            "FROM FUNCION f " +
            "JOIN SALA s   ON s.ID = f.SALA_ID " +
            "JOIN SEDE sd  ON sd.ID = s.SEDE_ID " +
            "JOIN PELICULA p ON p.ID = f.PELICULA_ID " +
            "WHERE LOWER(p.TITULO) LIKE ? " +
            "   OR LOWER(sd.NOMBRE) LIKE ? " +
            "   OR LOWER(sd.CIUDAD) LIKE ? " +
            "ORDER BY f.FECHA DESC, f.HORA DESC";
        List<FuncionDisponibleDTO> out = new ArrayList<>();
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapDTO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando funciones", e);
        }
        return out;
    }

    // ================= Helpers =================

    private long nextId() {
        final String sql = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM FUNCION";
        try (Connection cnn = db.getConnection();
             PreparedStatement ps = cnn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("NEXT_ID");
            return 1L;
        } catch (SQLException e) {
            return 1L;
        }
    }

    private Funcion mapFuncion(ResultSet rs) throws SQLException {
        Funcion f = new Funcion();
        f.setId(rs.getLong("ID"));
        f.setFecha(rs.getDate("FECHA"));
        f.setHora(rs.getTime("HORA"));
        f.setEstado(rs.getString("ESTADO"));
        f.setDuracion(rs.getTime("DURACION"));
        Object eb = rs.getObject("ESTADO_BOOL");
        f.setEstadoBool(eb == null ? null : (Boolean) eb);
        f.setPeliculaId(rs.getLong("PELICULA_ID"));
        f.setSalaId(rs.getLong("SALA_ID"));
        return f;
    }

    private FuncionDisponibleDTO mapDTO(ResultSet rs) throws SQLException {
        FuncionDisponibleDTO dto = new FuncionDisponibleDTO();
        dto.setFuncionId(rs.getLong("ID"));
        dto.setPeliculaId(rs.getLong("PELICULA_ID"));

        Date f = rs.getDate("FECHA");
        if (f != null) dto.setFecha(f.toLocalDate());

        Time h = rs.getTime("HORA");
        if (h != null) dto.setHora(h.toLocalTime());

        dto.setCiudad(rs.getString("CIUDAD"));
        dto.setSede(rs.getString("SEDE"));
        dto.setNumeroSala(rs.getInt("NUMERO_SALA"));
        dto.setTipoSala(rs.getString("TIPO_SALA"));
        dto.setSalaId(rs.getLong("SALA_ID"));
        return dto;
    }
}
