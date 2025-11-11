package sigmacine.infraestructura.persistencia.Mapper;

import sigmacine.dominio.entity.Funcion;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionMapper {
    public static Funcion map(ResultSet rs) throws SQLException {
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
}
