package sigmacine.infraestructura.persistencia.Mapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;

public final class CompraMapper {

    private CompraMapper() {}

    public static Compra map(ResultSet rs, Usuario cliente) throws SQLException {
        Long id = rs.getLong("COMPRA_ID");
        return new Compra(id, cliente);
    }

    public static HistorialCompraDTO mapHistorial(ResultSet rs) throws SQLException {
        Long compraId = rs.getObject("COMPRA_ID", Long.class);

        Date dCompra  = rs.getDate("COMPRA_FECHA");
        Date dFuncion = rs.getDate("FUNCION_FECHA");
        Time tFuncion = rs.getTime("FUNCION_HORA");

        BigDecimal total  = rs.getBigDecimal("COMPRA_TOTAL");
        String sedeCiudad = rs.getString("SEDE_CIUDAD");
        String salaNombre = rs.getString("SALA_NOMBRE");

        int cantBoletos   = rs.getInt("CANT_BOLETOS");
        int cantProductos = rs.getInt("CANT_PRODUCTOS");

        return new HistorialCompraDTO(
                compraId,
                dCompra  != null ? dCompra.toLocalDate()  : null,
                total,
                sedeCiudad,
                salaNombre,
                dFuncion != null ? dFuncion.toLocalDate() : null,
                tFuncion != null ? tFuncion.toLocalTime() : null,
                cantBoletos,
                cantProductos
        );
    }
}
