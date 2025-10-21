package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.repository.CompraRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class CompraRepositoryJdbc implements CompraRepository {

    private final DatabaseConfig db;

    public CompraRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public Long guardarCompraProductos(int clienteId, LocalDate fecha, List<CompraProductoDTO> items, String metodoPago,  BigDecimal total) {

        //if (clienteId == null) throw new IllegalArgumentException("clienteId requerido");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("La compra no tiene productos");
        if (metodoPago == null || metodoPago.trim().isEmpty()) throw new IllegalArgumentException("metodoPago requerido");
        if (total == null) throw new IllegalArgumentException("total requerido");

        /*final String nextCompraIdSql = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM COMPRA";
        final String nextPagoIdSql   = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM PAGO";
*/
        final String nextCompraIdSql = "SELECT NEXT VALUE FOR SEQ_COMPRA AS NEXT_ID";
        final String nextPagoIdSql   = "SELECT NEXT VALUE FOR SEQ_PAGO AS NEXT_ID";


        final String insertCompra = """
            INSERT INTO COMPRA (ID, TOTAL, FECHA, CLIENTE_ID)
            VALUES (?, ?, ?, ?)
        """;

        final String insertDetalle = """
            INSERT INTO COMPRA_PRODUCTO (COMPRA_ID, PRODUCTO_ID, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL)
            VALUES (?, ?, ?, ?, ?)
        """;

        final String insertPago = """
            INSERT INTO PAGO (ID, METODO, MONTO, ESTADO, ESTADO_BOOL, FECHA, COMPRA_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);

            Long compraId;
            Long pagoId;

            try {
                // 1) IDs
                try (PreparedStatement ps = con.prepareStatement(nextCompraIdSql);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    compraId = rs.getLong("NEXT_ID");
                }
                try (PreparedStatement ps = con.prepareStatement(nextPagoIdSql);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    pagoId = rs.getLong("NEXT_ID");
                }

                // 2) COMPRA
                try (PreparedStatement ps = con.prepareStatement(insertCompra)) {
                    ps.setLong(1, compraId);
                    ps.setBigDecimal(2, total);
                    ps.setDate(3, Date.valueOf(fecha));
                    ps.setLong(4, clienteId);
                    ps.executeUpdate();
                }

                // 3) DETALLES
                try (PreparedStatement ps = con.prepareStatement(insertDetalle)) {
                    for (CompraProductoDTO it : items) {
                        if (it.getProductoId() == null) {
                            throw new IllegalArgumentException("Hay un item sin PRODUCTO_ID (boletos no se insertan aquí).");
                        }
                        if (it.getCantidad() <= 0) {
                            throw new IllegalArgumentException("Cantidad inválida para producto " + it.getProductoId());
                        }
                        BigDecimal subtotal = it.getPrecioUnitario()
                                .multiply(BigDecimal.valueOf(it.getCantidad()));
                        ps.setLong(1, compraId);
                        ps.setLong(2, it.getProductoId());
                        ps.setInt(3, it.getCantidad());
                        ps.setBigDecimal(4, it.getPrecioUnitario());
                        ps.setBigDecimal(5, subtotal);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 4) PAGO (por ahora estado fijo APROBADO)
                try (PreparedStatement ps = con.prepareStatement(insertPago)) {
                    ps.setLong(1, pagoId);
                    ps.setString(2, metodoPago.trim());
                    ps.setBigDecimal(3, total);
                    ps.setString(4, "APROBADO");
                    ps.setBoolean(5, true);
                    ps.setDate(6, Date.valueOf(fecha));
                    ps.setLong(7, compraId);
                    ps.executeUpdate();
                }

                con.commit();
                return compraId;

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando compra de productos", e);
        }
    }
}
