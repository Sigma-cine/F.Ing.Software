package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import sigmacine.infraestructura.persistencia.Mapper.CompraMapper;
import java.sql.Date;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private final DatabaseConfig db;

    public UsuarioRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public Usuario buscarPorEmail(Email email) {
        final String sql = """
            SELECT
                U.ID,
                U.EMAIL,
                U.CONTRASENA,
                U.ROL,
                A.NOMBRE  AS NOMBRE_ADMIN,
                C.NOMBRE  AS NOMBRE_CLIENTE,
                C.FECHA_REGISTRO,
                SC.SALDO   AS SIGMA_SALDO,
                SC.ESTADO  AS SIGMA_ESTADO
            FROM USUARIO U
            LEFT JOIN ADMIN   A ON A.ID = U.ID
            LEFT JOIN CLIENTE C ON C.ID = U.ID
            LEFT JOIN SIGMA_CARD SC ON SC.ID = U.ID
            WHERE U.EMAIL = ?
            FETCH FIRST 1 ROWS ONLY
        """;

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.value());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando USUARIO por email", e);
        }
    }

    @Override
    public void guardar(Usuario u) {
        final String sql = """
            UPDATE USUARIO
            SET CONTRASENA = ?,
                ROL        = ?
            WHERE ID = ?
        """;
        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getPasswordHash().value());
            ps.setString(2, u.getRol().name());
            ps.setLong(3, u.getId());

            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("USUARIO no existe (id=" + u.getId() + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando USUARIO", e);
        }
    }

    @Override
    public int crearCliente(Email email, PasswordHash passwordHash, String nombre) {
        final String nextIdSql     = "SELECT COALESCE(MAX(ID),0)+1 AS NEXT_ID FROM USUARIO";
        final String insertUsuario = "INSERT INTO USUARIO (ID, EMAIL, CONTRASENA, ROL) VALUES (?, ?, ?, 'CLIENTE')";
        final String insertCliente = "INSERT INTO CLIENTE (ID, NOMBRE, FECHA_REGISTRO) VALUES (?, ?, CURRENT_DATE)";
    final String insertSigma = "INSERT INTO SIGMA_CARD (ID, SALDO, ESTADO) VALUES (?, 0.00, TRUE)";

        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(nextIdSql);
                    ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getInt("NEXT_ID");
                }

                try (PreparedStatement psU = con.prepareStatement(insertUsuario);
                    PreparedStatement psC = con.prepareStatement(insertCliente);
                    PreparedStatement psS = con.prepareStatement(insertSigma)) {

                    psU.setLong(1, id);
                    psU.setString(2, email.value());
                    psU.setString(3, passwordHash.value());
                    psU.executeUpdate();

                    psC.setLong(1, id);
                    psC.setString(2, nombre);
                    psC.executeUpdate();

                    psS.setLong(1, id);
                    psS.executeUpdate();
                }

                con.commit();
                return id;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando cliente", e);
        }
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        int id         = rs.getInt("ID");
        Email email     = new Email(rs.getString("EMAIL"));
        PasswordHash ph = new PasswordHash(rs.getString("CONTRASENA"));

        Usuario.Rol rol = normalizarRol(rs.getString("ROL"));

        if (rol == Usuario.Rol.ADMIN) {
            String nombreAdmin = rs.getString("NOMBRE_ADMIN");// ADMIN no tiene fechaRegistro/SigmaCard
            return Usuario.crearAdmin(id, email, ph, nombreAdmin);
        } else {
            String nombreCliente = rs.getString("NOMBRE_CLIENTE");
            Date f = rs.getDate("FECHA_REGISTRO");    // LocalDate si viene de DB, si no, null
            java.time.LocalDate fecha = (f != null) ? f.toLocalDate() : null;
            java.math.BigDecimal sigmaSaldo = rs.getBigDecimal("SIGMA_SALDO");
            Boolean sigmaEstado = rs.getObject("SIGMA_ESTADO", Boolean.class);
            if (sigmaSaldo != null) {
                sigmacine.dominio.entity.SigmaCard sc = new sigmacine.dominio.entity.SigmaCard((long) id, sigmaSaldo);
                if (sigmaEstado != null && !sigmaEstado) {
                    try { sc.desactivar(); } catch (Exception ignore) {}
                }
                return Usuario.crearCliente(id, email, ph, nombreCliente, fecha, sc);
            } else {
                return Usuario.crearCliente(id, email, ph, nombreCliente, fecha);
            }
        }
    }

    private Usuario.Rol normalizarRol(String raw) {
        if (raw == null) return Usuario.Rol.CLIENTE;
        String v = raw.trim().toUpperCase();
        if (v.equals("ADMIN") || v.equals("ADMI")) return Usuario.Rol.ADMIN;
        return Usuario.Rol.CLIENTE;
    }

    @Override
    public Usuario buscarPorId(int id) {
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }

    @Override
    public List<HistorialCompraDTO> verHistorial(String emailPlano) {
        // diagnostics removed
        final String sql = """
        SELECT
            co.ID AS COMPRA_ID,
            co.FECHA AS COMPRA_FECHA,
            (
                (SELECT COALESCE(SUM(b2.PRECIO_FINAL),0) FROM BOLETO b2 WHERE b2.COMPRA_ID = co.ID)
                + (SELECT COALESCE(SUM(cp2.SUBTOTAL),0) FROM COMPRA_PRODUCTO cp2 WHERE cp2.COMPRA_ID = co.ID)
            ) AS COMPRA_TOTAL,
            MIN(se.ID) AS SEDE_ID,
            MIN(se.CIUDAD) AS SEDE_CIUDAD,
            MIN(f.FECHA) AS FUNCION_FECHA,
            MIN(f.HORA) AS FUNCION_HORA,
            (SELECT COUNT(*) FROM BOLETO b3 WHERE b3.COMPRA_ID = co.ID) AS CANT_BOLETOS,
            (SELECT COALESCE(SUM(cp3.CANTIDAD),0) FROM COMPRA_PRODUCTO cp3 WHERE cp3.COMPRA_ID = co.ID) AS CANT_PRODUCTOS
        FROM COMPRA co
        JOIN CLIENTE c ON c.ID = co.CLIENTE_ID
        JOIN USUARIO u ON u.ID = c.ID
        LEFT JOIN BOLETO b ON b.COMPRA_ID = co.ID
        LEFT JOIN FUNCION f ON f.ID = b.FUNCION_ID
        LEFT JOIN SALA sa ON sa.ID = f.SALA_ID
        LEFT JOIN SEDE se ON se.ID = sa.SEDE_ID
        WHERE u.EMAIL = ?
        GROUP BY co.ID, co.FECHA, co.TOTAL
        ORDER BY co.FECHA DESC, co.ID DESC;

        """;

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emailPlano);

            try (ResultSet rs = ps.executeQuery()) {
                var lista = new ArrayList<HistorialCompraDTO>();
                var ids = new ArrayList<Long>();
                while (rs.next()) {
                    lista.add(CompraMapper.mapHistorial(rs));
                    ids.add(rs.getObject("COMPRA_ID", Long.class));
                }
                // compras found for user
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando historial de compras del usuario " + emailPlano, e);
        }
    }

    @Override
    public List<Boleto> obtenerBoletosPorCompra(Long compraId) {
        final String sqlBoletos = "SELECT b.ID, b.PRECIO_FINAL, f.HORA, sa.NUMERO_SALA, p.TITULO, bs.SILLA_ID, s.FILA, s.NUMERO AS SILLA_NUMERO "
                + "FROM BOLETO b "
                + "LEFT JOIN BOLETO_SILLA bs ON bs.BOLETO_ID = b.ID "
                + "LEFT JOIN SILLA s ON s.ID = bs.SILLA_ID "
                + "LEFT JOIN FUNCION f ON f.ID = b.FUNCION_ID "
                + "LEFT JOIN SALA sa ON sa.ID = f.SALA_ID "
                + "LEFT JOIN PELICULA p ON p.ID = f.PELICULA_ID "
                + "WHERE b.COMPRA_ID = ?";

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlBoletos)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                var lista = new ArrayList<Boleto>();
                while (rs.next()) {
                    Boleto b = new Boleto();
                    Long bId = rs.getObject("ID", Long.class);
                    b.setId(bId);
                    String fila = rs.getString("FILA");
                    Integer nro = rs.getObject("SILLA_NUMERO", Integer.class);
                    String asiento = null;
                    if (fila != null || nro != null) {
                        asiento = (fila != null ? fila : "") + (nro != null ? String.valueOf(nro) : "");
                    }
                    if (asiento == null || asiento.isBlank()) {
                        asiento = "Sin asignar";
                    }
                    b.setAsiento(asiento);
                    java.math.BigDecimal precioBd = rs.getBigDecimal("PRECIO_FINAL");
                    long precio = precioBd != null ? precioBd.longValue() : 0L;
                    b.setPrecio(precio);
                    b.setHorario(rs.getString("HORA"));
                    b.setSala(rs.getString("NUMERO_SALA"));
                    b.setPelicula(rs.getString("TITULO"));
                    lista.add(b);
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando boletos por compra " + compraId, e);
        }
    }

    @Override
    public List<CompraProductoDTO> obtenerProductosPorCompra(Long compraId) {
        final String sqlProductos = "SELECT cp.PRODUCTO_ID, pr.NOMBRE, cp.CANTIDAD, cp.PRECIO_UNITARIO "
                + "FROM COMPRA_PRODUCTO cp "
                + "LEFT JOIN PRODUCTO pr ON pr.ID = cp.PRODUCTO_ID "
                + "WHERE cp.COMPRA_ID = ?";

        try (Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlProductos)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                var lista = new ArrayList<CompraProductoDTO>();
                while (rs.next()) {
                    Long pid = rs.getObject("PRODUCTO_ID", Long.class);
                    String nombre = rs.getString("NOMBRE");
                    int cant = rs.getInt("CANTIDAD");
                    java.math.BigDecimal precio = rs.getBigDecimal("PRECIO_UNITARIO");
                    lista.add(new CompraProductoDTO(pid, nombre, cant, precio));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando productos por compra " + compraId, e);
        }
    }
}