package sigmacine.infraestructura.configDataBase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2.tools.RunScript;
public class ScriptLoader {
    public static void runScripts(Connection conn) {
        try {
            runSqlFromClasspath(conn, "/schema.sql");
                runSqlFromClasspath(conn, "/data.sql"); 
            
            try (var ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('USUARIO','PELICULA','SIGMA_CARD')")) {
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int cnt = rs.getInt("CNT");
                        if (cnt < 3) {
                            throw new SQLException("Scripts ejecutados pero tablas esperadas no encontradas (found=" + cnt + ")");
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error ejecutando scripts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudieron inicializar los scripts de la base de datos: " + e.getMessage(), e);
        }
                    }
                    private static void runSqlFromClasspath(Connection conn, String resourcePath) throws SQLException, IOException {
                        InputStream in = ScriptLoader.class.getResourceAsStream(resourcePath);
                        if (in == null) {
                        return;
                        }
                        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8))
                        { RunScript.execute(conn, reader);
                        }
                    }
                }