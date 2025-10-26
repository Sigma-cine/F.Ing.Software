package sigmacine.infraestructura.configDataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static final String USER   = "sa";
    private static final String PASS   = "";
    //private static final String URL ="jdbc:h2:file:./sigmacine/db/cine_db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE";
   // private static final String URL ="jdbc:h2:file:~/sigmacine/db/cine_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
    private static final String URL ="jdbc:h2:~/sigmacine/db/cine_db;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DRIVER = "org.h2.Driver";

    static {
        try {
            Class.forName(DRIVER); // cargar driver UNA sola vez
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de H2", e);
        }
    }

    /** Método estándar (instancia) */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Alias para compatibilidad con código viejo */
    public Connection GetConexionDBH2() throws SQLException {
        return getConnection();
    }

    /** (Opcional) Versión estática si prefieres llamar sin instanciar */
    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
