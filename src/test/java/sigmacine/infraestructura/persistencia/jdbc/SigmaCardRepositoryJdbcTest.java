package sigmacine.infraestructura.persistencia.jdbc;

import org.junit.jupiter.api.*;
import org.h2.jdbcx.JdbcDataSource;
import java.sql.Connection;
import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;


class SigmaCardRepositoryJdbcTest {
    private static TestDbConfig dbConfig;
    private SigmaCardRepositoryJdbc repository;

    static class TestDbConfig extends sigmacine.infraestructura.configDataBase.DatabaseConfig {
        private final DataSource ds;
        public TestDbConfig(DataSource ds) {
            this.ds = ds;
        }
        @Override
        public Connection getConnection() throws java.sql.SQLException {
            return ds.getConnection();
        }
    }

    @BeforeAll
    static void setupDatabase() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        dbConfig = new TestDbConfig(ds);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE SIGMA_CARD (ID BIGINT PRIMARY KEY, SALDO DECIMAL(10,2), ESTADO BOOLEAN)");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Instancia con cualquier DatabaseConfig (no importa, se reemplaza por reflexión)
        repository = new SigmaCardRepositoryJdbc((sigmacine.infraestructura.configDataBase.DatabaseConfig) Class.forName("sigmacine.infraestructura.configDataBase.DatabaseConfig").getDeclaredConstructor().newInstance());
        // Inyecta el db de test por reflexión
        java.lang.reflect.Field dbField = SigmaCardRepositoryJdbc.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(repository, dbConfig);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM SIGMA_CARD");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void crearSiNoExiste_creaNueva() {
        boolean creado = repository.crearSiNoExiste(1L);
        assertTrue(creado);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            var rs = st.executeQuery("SELECT * FROM SIGMA_CARD WHERE ID = 1");
            assertTrue(rs.next());
            java.math.BigDecimal saldo = rs.getBigDecimal("SALDO");
            assertTrue(saldo.compareTo(java.math.BigDecimal.ZERO) == 0, "Expected saldo 0 but was: " + saldo);
            assertTrue(rs.getBoolean("ESTADO"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void crearSiNoExiste_noDuplica() {
        repository.crearSiNoExiste(2L);
        boolean creado = repository.crearSiNoExiste(2L);
        assertFalse(creado);
    }

    @Test
    void consultarSaldo_existente() {
        repository.crearSiNoExiste(3L);
        BigDecimal saldo = repository.consultarSaldo(3L);
        assertTrue(saldo.compareTo(BigDecimal.ZERO) == 0, "Expected saldo 0 but was: " + saldo);
    }

    @Test
    void consultarSaldo_noExistente() {
        assertNull(repository.consultarSaldo(999L));
    }

    @Test
    void recargar_nuevaTarjeta() {
        BigDecimal saldo = repository.recargar(4L, new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), saldo);
        assertEquals(saldo, repository.consultarSaldo(4L));
    }

    @Test
    void recargar_tarjetaExistente() {
        repository.crearSiNoExiste(5L);
        repository.recargar(5L, new BigDecimal("20.00"));
        BigDecimal saldo = repository.recargar(5L, new BigDecimal("30.00"));
        assertEquals(new BigDecimal("50.00"), saldo);
    }

    @Test
    void recargar_montoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> repository.recargar(6L, null));
    }
}
