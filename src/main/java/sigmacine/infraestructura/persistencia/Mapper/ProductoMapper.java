package sigmacine.infraestructura.persistencia.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import sigmacine.aplicacion.data.ProductoDTO;

public class ProductoMapper {

    public static ProductoDTO map(ResultSet rs) throws SQLException {       
        ProductoDTO p = new ProductoDTO(
            rs.getLong("ID"),
            rs.getString("NOMBRE"),
            rs.getString("DESCRIPCION"),
            rs.getString("IMAGEN_URL"),
            rs.getString("SABORES"),
            rs.getString("TIPO"),
            rs.getBigDecimal("PRECIO_LISTA"),
            rs.getBoolean("ESTADO_BOOL"),
            rs.getString("ESTADO")
        ); 
        
        return p;
    }
    
}