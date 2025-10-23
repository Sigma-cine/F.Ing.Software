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

        final String nextBoletoIdSql = "SELECT NEXT VALUE FOR SEQ_BOLETO AS NEXT_ID";

        final String insertBoleto = """
            INSERT INTO BOLETO (ID, CODIGO, ESTADO, ESTADO_BOOL, PRECIO_FINAL, COMPRA_ID, FUNCION_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        final String insertBoletoSilla = """
            INSERT INTO BOLETO_SILLA (BOLETO_ID, SILLA_ID)
            VALUES (?, ?)
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
                    System.out.println("[CompraRepositoryJdbc] Preparando INSERT COMPRA -> ID=" + compraId + ", CLIENTE_ID=" + clienteId + ", TOTAL=" + total);
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
                                // boleto -> handled separately below
                                continue;
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

                    // 3b) BOLETOS: insertar cada boleto individualmente (productoId == null)
                    try (PreparedStatement nextBoletoPs = con.prepareStatement(nextBoletoIdSql);
                         PreparedStatement psBoleto = con.prepareStatement(insertBoleto);
                         PreparedStatement psBoletoSilla = con.prepareStatement(insertBoletoSilla)) {
                        for (CompraProductoDTO it : items) {
                            if (it.getProductoId() != null) continue; // skip products

                            if (it.getFuncionId() == null) {
                                throw new IllegalArgumentException("Boleto sin funcionId no puede insertarse.");
                            }
                            // obtain next boleto id
                            long boletoId;
                            try (ResultSet rs = nextBoletoPs.executeQuery()) {
                                rs.next(); boletoId = rs.getLong("NEXT_ID");
                            }

                            // Insert BOLETO
                            psBoleto.setLong(1, boletoId);
                            psBoleto.setString(2, "BOL-" + boletoId);
                            psBoleto.setString(3, "APROBADO");
                            psBoleto.setBoolean(4, true);
                            psBoleto.setBigDecimal(5, it.getPrecioUnitario());
                            psBoleto.setLong(6, compraId);
                            psBoleto.setLong(7, it.getFuncionId());
                            psBoleto.executeUpdate();

                            // Map silla if asiento code is provided: find SILLA.ID by fila+numero within the SALA of the function
                            String asiento = it.getAsiento();
                            if (asiento != null && !asiento.isBlank()) {
                                // Asiento format: 'A5' or 'B10' -> fila letter(s) + number
                                String filaPart = asiento.replaceAll("\\d", "");
                                String numPart = asiento.replaceAll("\\D", "");
                                boolean sillaInserted = false;
                                if (!numPart.isBlank()) {
                                    // Lookup SALA_ID for the function
                                    Long salaId = null;
                                    try (PreparedStatement psSala = con.prepareStatement("SELECT SALA_ID FROM FUNCION WHERE ID = ? FETCH FIRST 1 ROWS ONLY")) {
                                        psSala.setLong(1, it.getFuncionId());
                                        try (ResultSet rsSala = psSala.executeQuery()) {
                                            if (rsSala.next()) salaId = rsSala.getObject("SALA_ID", Long.class);
                                        }
                                    }

                                    if (salaId != null) {
                                        try (PreparedStatement psFindSilla = con.prepareStatement("SELECT ID FROM SILLA WHERE FILA = ? AND NUMERO = ? AND SALA_ID = ? FETCH FIRST 1 ROWS ONLY")) {
                                            psFindSilla.setString(1, filaPart);
                                            psFindSilla.setInt(2, Integer.parseInt(numPart));
                                            psFindSilla.setLong(3, salaId);
                                            try (ResultSet rs = psFindSilla.executeQuery()) {
                                                if (rs.next()) {
                                                    long sillaId = rs.getLong("ID");
                                                    psBoletoSilla.setLong(1, boletoId);
                                                    psBoletoSilla.setLong(2, sillaId);
                                                    psBoletoSilla.executeUpdate();
                                                    sillaInserted = true;
                                                }
                                            }
                                        }

                                        // Fallback: if not found within the sala, try a global lookup by fila+numero
                                        if (!sillaInserted) {
                                            try (PreparedStatement psFindSillaGlobal = con.prepareStatement("SELECT ID FROM SILLA WHERE FILA = ? AND NUMERO = ? FETCH FIRST 1 ROWS ONLY")) {
                                                psFindSillaGlobal.setString(1, filaPart);
                                                psFindSillaGlobal.setInt(2, Integer.parseInt(numPart));
                                                try (ResultSet rs2 = psFindSillaGlobal.executeQuery()) {
                                                    if (rs2.next()) {
                                                        long sillaId = rs2.getLong("ID");
                                                        psBoletoSilla.setLong(1, boletoId);
                                                        psBoletoSilla.setLong(2, sillaId);
                                                        psBoletoSilla.executeUpdate();
                                                        System.out.println("[CompraRepositoryJdbc] Asiento mapeado globalmente: asiento=" + asiento + " -> SILLA_ID=" + sillaId + " (boleto=" + boletoId + ")");
                                                    } else {
                                                        System.out.println("[CompraRepositoryJdbc] No se encontró SILLA para asiento=" + asiento + " (funcionId=" + it.getFuncionId() + ", boleto=" + boletoId + ")");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                System.out.println("[CompraRepositoryJdbc] Compra guardada: COMPRA_ID=" + compraId + ", PAGO_ID=" + pagoId + ", ITEMS=" + items.size());
                return compraId;

            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException r) { System.err.println("[CompraRepositoryJdbc] Error en rollback: " + r.getMessage()); r.printStackTrace(); }
                System.err.println("[CompraRepositoryJdbc] Error guardando compra (antes de rethrow): " + ex.getMessage());
                ex.printStackTrace();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando compra de productos", e);
        }
    }
}
