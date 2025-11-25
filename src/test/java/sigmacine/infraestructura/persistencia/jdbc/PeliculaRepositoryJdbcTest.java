package sigmacine.infraestructura.persistencia.jdbc;

import org.junit.jupiter.api.*;
import org.h2.jdbcx.JdbcDataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PeliculaRepositoryJdbcTest {
    private static TestDbConfig dbConfig;
    private PeliculaRepositoryJdbc repository;

    static class TestDbConfig extends sigmacine.infraestructura.configDataBase.DatabaseConfig {
        private final JdbcDataSource ds;
        public TestDbConfig(JdbcDataSource ds) { this.ds = ds; }
        @Override
        public Connection getConnection() throws java.sql.SQLException { return ds.getConnection(); }
    }

    @BeforeAll
    static void setupDatabase() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        dbConfig = new TestDbConfig(ds);
        try (Connection con = dbConfig.getConnection(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE PELICULA (ID BIGINT PRIMARY KEY, TITULO VARCHAR(100), GENERO VARCHAR(50), CLASIFICACION VARCHAR(10), DURACION INT, DIRECTOR VARCHAR(100), ESTADO VARCHAR(20), POSTER_URL VARCHAR(255), SINOPSIS VARCHAR(255), REPARTO VARCHAR(255), TRAILER VARCHAR(255))");
            st.execute("INSERT INTO PELICULA VALUES (1, 'Matrix', 'Accion', 'PG-13', 120, 'Wachowski', 'ACTIVA', '', '', '', '')");
            st.execute("INSERT INTO PELICULA VALUES (2, 'Titanic', 'Drama', 'PG-13', 195, 'Cameron', 'ACTIVA', '', '', '', '')");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Instancia con cualquier DatabaseConfig (no importa, se reemplaza por reflexión)
        repository = new PeliculaRepositoryJdbc((sigmacine.infraestructura.configDataBase.DatabaseConfig) Class.forName("sigmacine.infraestructura.configDataBase.DatabaseConfig").getDeclaredConstructor().newInstance());
        // Inyecta el db de test por reflexión
        java.lang.reflect.Field dbField = PeliculaRepositoryJdbc.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(repository, dbConfig);
    }

    @Test
    void buscarPorTitulo() {
        List<?> peliculas = repository.buscarPorTitulo("Matrix");
        assertEquals(1, peliculas.size());
    }

    @Test
    void buscarPorGenero() {
        List<?> peliculas = repository.buscarPorGenero("Drama");
        assertEquals(1, peliculas.size());
    }
}
