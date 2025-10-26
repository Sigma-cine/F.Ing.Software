package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.dominio.entity.Pelicula;

public class PeliculaMapper {

    public static Pelicula map(ResultSet rs) throws SQLException {       
        Pelicula p = new Pelicula(
            rs.getInt("ID"),
            rs.getString("TITULO"),
            rs.getString("GENERO"),
            rs.getString("CLASIFICACION"),
            (Integer) rs.getObject("DURACION"),
            rs.getString("DIRECTOR"),
            rs.getString("ESTADO")
        ); 
        String posterUrl = rs.getString("POSTER_URL");
        if (posterUrl != null) {
            String normalized = posterUrl;
            try {
                String lower = posterUrl.toLowerCase();
                if (lower.contains("src") && (lower.contains("images") || lower.contains("img"))) {
                        int idx = Math.max(posterUrl.lastIndexOf('/'), posterUrl.lastIndexOf('\\'));
                    if (idx >= 0 && idx + 1 < posterUrl.length()) normalized = posterUrl.substring(idx + 1);
                }
            } catch (Exception ignore) {}
            p.setPosterUrl(normalized);
        }

        String sinopsis = rs.getString("SINOPSIS");
        if (sinopsis != null) {
            p.setSinopsis(sinopsis);
        }

        String reparto = rs.getString("REPARTO");
        if (reparto != null) {
            p.setReparto(reparto);
        }

        return p;
    }
    
}