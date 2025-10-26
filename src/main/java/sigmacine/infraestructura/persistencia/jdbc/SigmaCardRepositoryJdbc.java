package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.dominio.repository.SigmaCardRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SigmaCardRepositoryJdbc implements SigmaCardRepository {

    private final DatabaseConfig db;

    public SigmaCardRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public boolean crearSiNoExiste(long usuarioId) {
        final String select = "SELECT COUNT(1) AS CNT FROM SIGMA_CARD WHERE ID = ?";
        final String insert = "INSERT INTO SIGMA_CARD (ID, SALDO, ESTADO) VALUES (?, 0.00, TRUE)";
        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setLong(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    int cnt = 0;
                    if (rs.next()) cnt = rs.getInt("CNT");
                    if (cnt > 0) {
                        con.setAutoCommit(true);
                        return false;
                    }
                }
            }

            try (PreparedStatement ps2 = con.prepareStatement(insert)) {
                ps2.setLong(1, usuarioId);
                ps2.executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error creando SIGMA_CARD", e);
        }
    }

    @Override
    public BigDecimal consultarSaldo(long usuarioId) {
        final String sql = "SELECT SALDO FROM SIGMA_CARD WHERE ID = ?";
        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getBigDecimal("SALDO");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando saldo SIGMA_CARD", e);
        }
    }

    @Override
    public BigDecimal recargar(long usuarioId, BigDecimal monto) {
        if (monto == null) throw new IllegalArgumentException("Monto requerido");
        final String select = "SELECT SALDO FROM SIGMA_CARD WHERE ID = ?";
        final String insert = "INSERT INTO SIGMA_CARD (ID, SALDO, ESTADO) VALUES (?, ?, TRUE)";
        final String update = "UPDATE SIGMA_CARD SET SALDO = ? WHERE ID = ?";

        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            BigDecimal current = null;
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setLong(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) current = rs.getBigDecimal("SALDO");
                }
            }

            BigDecimal nuevo;
            if (current == null) {
                nuevo = monto;
                try (PreparedStatement psIns = con.prepareStatement(insert)) {
                    psIns.setLong(1, usuarioId);
                    psIns.setBigDecimal(2, nuevo);
                    psIns.executeUpdate();
                }
            } else {
                nuevo = current.add(monto);
                try (PreparedStatement psUpd = con.prepareStatement(update)) {
                    psUpd.setBigDecimal(1, nuevo);
                    psUpd.setLong(2, usuarioId);
                    psUpd.executeUpdate();
                }
            }
            con.commit();
            con.setAutoCommit(true);
            return nuevo;
        } catch (SQLException e) {
            throw new RuntimeException("Error recargando SIGMA_CARD", e);
        }
    }
}
