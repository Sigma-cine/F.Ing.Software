package sigmacine.infraestructura.configDataBase;

import java.sql.*;

public class TestConexion {
    public static void main(String[] args) {
        try {
            DatabaseConfig db = new DatabaseConfig();               

            try (Connection conn = db.getConnection()) {          
                ScriptLoader.runScripts(conn);     

                System.out.println("Conectado a: " + conn.getMetaData().getURL());

                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT 1")) {
                    rs.next();
                    System.out.println("SELECT 1 -> " + rs.getInt(1));
                }
            

            DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet tablas = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
                    System.out.println("\nTablas encontradas en la BD:");
                    while (tablas.next()) {
                        String nombreTabla = tablas.getString("TABLE_NAME");
                        System.out.println(" - " + nombreTabla);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
