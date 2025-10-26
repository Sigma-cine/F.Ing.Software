package sigmacine.tools;

import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CompraService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.CompraRepositoryJdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompraIntegrationTest {

    @Test
    public void testProductoCompra() throws Exception {
        DatabaseConfig db = new DatabaseConfig();
        try (Connection conn = db.getConnection()) {
            sigmacine.infraestructura.configDataBase.ScriptLoader.runScripts(conn);
        }
        var repo = new CompraRepositoryJdbc(db);
        var svc = new CompraService(repo);

        CompraProductoDTO item = new CompraProductoDTO(1L, "Combo Popcorn", 1, new BigDecimal("10.50"));
        List<CompraProductoDTO> items = new ArrayList<>();
        items.add(item);

        int clienteId = 2;
        Long compraId = svc.confirmarCompraProductos(clienteId, items, "TEST");
        assertNotNull(compraId);

        try (var c = db.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM COMPRA WHERE ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }
        }
    }

    @Test
    public void testMixedProductoAndBoleto() throws Exception {
        DatabaseConfig db = new DatabaseConfig();
        try (Connection conn = db.getConnection()) {
            sigmacine.infraestructura.configDataBase.ScriptLoader.runScripts(conn);
        }
        var repo = new CompraRepositoryJdbc(db);
        var svc = new CompraService(repo);

        // Product
        CompraProductoDTO prod = new CompraProductoDTO(1L, "Combo Popcorn", 1, new BigDecimal("10.50"));
        // Boleto (funcionId must exist in data.sql)
        CompraProductoDTO boleto = new CompraProductoDTO(null, 1L, "Boleto prueba", 1, new BigDecimal("12.00"), "A1");

        List<CompraProductoDTO> items = new ArrayList<>();
        items.add(prod);
        items.add(boleto);

        int clienteId = 2;
        Long compraId = svc.confirmarCompraProductos(clienteId, items, "TEST-MIX");
        assertNotNull(compraId);

        try (var c = db.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM COMPRA WHERE ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM BOLETO WHERE COMPRA_ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM COMPRA_PRODUCTO WHERE COMPRA_ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }
        }
    }
}
