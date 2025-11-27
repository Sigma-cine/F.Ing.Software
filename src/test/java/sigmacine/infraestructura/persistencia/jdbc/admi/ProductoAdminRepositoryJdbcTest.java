package sigmacine.infraestructura.persistencia.jdbc.admi;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.dominio.repository.admi.ProductoAdminRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoAdminRepositoryJdbcTest {
    private static JdbcDataSource ds;
    private static DatabaseConfigTestImpl dbConfig;
    private ProductoAdminRepository repository;

    static class DatabaseConfigTestImpl extends sigmacine.infraestructura.configDataBase.DatabaseConfig {
        private final JdbcDataSource ds;
        public DatabaseConfigTestImpl(JdbcDataSource ds) { this.ds = ds; }
        public Connection getConnection() throws java.sql.SQLException { return ds.getConnection(); }
    }

    @BeforeAll
    static void setupDatabase() throws Exception {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb_producto;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        dbConfig = new DatabaseConfigTestImpl(ds);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE PRODUCTO (ID BIGINT PRIMARY KEY, NOMBRE VARCHAR(100), DESCRIPCION VARCHAR(255), IMAGEN_URL VARCHAR(255), SABORES VARCHAR(100), TIPO VARCHAR(50), PRECIO_LISTA DECIMAL(10,2), ESTADO VARCHAR(20), ESTADO_BOOL BOOLEAN)");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        repository = new ProductoAdminRepositoryJdbc(dbConfig);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM PRODUCTO");
        }
    }

    @Test
    void guardarYListarProducto() {
        ProductoDTO dto = new ProductoDTO(1L, "Coca-Cola", "Bebida gaseosa", "url.jpg", "N/A", "BEBIDA", new BigDecimal("15.00"), true, "ACTIVO");
        ProductoDTO guardado = repository.guardarNuevo(dto);
        assertNotNull(guardado);
        assertEquals("Coca-Cola", guardado.getNombreProducto());
        List<ProductoDTO> productos = repository.listarTodas();
        assertEquals(1, productos.size());
        assertEquals("Coca-Cola", productos.get(0).getNombreProducto());
    }
}
