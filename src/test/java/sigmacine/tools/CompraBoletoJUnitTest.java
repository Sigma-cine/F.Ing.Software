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

public class CompraBoletoJUnitTest {

    @Test
    public void testMultipleBoletosAndProduct() throws Exception {
        DatabaseConfig db = new DatabaseConfig();
        try (Connection conn = db.getConnection()) {
            sigmacine.infraestructura.configDataBase.ScriptLoader.runScripts(conn);
        }

        var repo = new CompraRepositoryJdbc(db);
        var svc = new CompraService(repo);

        // Product item
        CompraProductoDTO prod = new CompraProductoDTO(1L, "Combo Popcorn", 1, new BigDecimal("10.50"));
        // Two boletos for funcionId = 1 (A1 and A2)
        CompraProductoDTO boleto1 = new CompraProductoDTO(null, 1L, "Boleto A1", 1, new BigDecimal("12.00"), "A1");
        CompraProductoDTO boleto2 = new CompraProductoDTO(null, 1L, "Boleto A2", 1, new BigDecimal("12.00"), "A2");

        List<CompraProductoDTO> items = new ArrayList<>();
        items.add(prod);
        items.add(boleto1);
        items.add(boleto2);

        int clienteId = 2;
        Long compraId = svc.confirmarCompraProductos(clienteId, items, "TEST-MULTI-BOLETO");
        assertNotNull(compraId);

        try (var c = db.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM COMPRA WHERE ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM BOLETO WHERE COMPRA_ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("CNT"));
            }

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM COMPRA_PRODUCTO WHERE COMPRA_ID = " + compraId)) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("CNT"));
            }

            // ensure BOLETO_SILLA entries are present for each boleto
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS CNT FROM BOLETO_SILLA WHERE BOLETO_ID IN (SELECT ID FROM BOLETO WHERE COMPRA_ID = " + compraId + ")")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("CNT"));
            }
        }
    }
}
