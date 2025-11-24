package sigmacine.infraestructura.configDataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String USER   = "sa";
    private static final String PASS   = "";

    //private static final String URL ="jdbc:h2:file:./sigmacine/db/cine_db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE";
   // private static final String URL ="jdbc:h2:file:~/sigmacine/db/cine_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
    private static final String URL ="jdbc:h2:~/sigmacine/db/cine_db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";

    private static final String DRIVER = "org.h2.Driver";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de H2", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public Connection GetConexionDBH2() throws SQLException {
        return getConnection();
    }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
