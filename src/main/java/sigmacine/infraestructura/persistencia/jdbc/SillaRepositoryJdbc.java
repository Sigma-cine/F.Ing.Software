package sigmacine.infraestructura.persistencia.jdbc;

import sigmacine.dominio.repository.SillaRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SillaRepositoryJdbc implements SillaRepository {

    private final DatabaseConfig db;

    public SillaRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public Set<String> obtenerAsientosOcupadosPorFuncion(Long funcionId) {
        Set<String> ocupados = new HashSet<>();
        
        // Query para obtener asientos ocupados:
        // - Buscar boletos de la función específica
        // - Unir con BOLETO_SILLA para obtener las sillas
        // - Concatenar fila + numero para formar el código del asiento
        String sql = """
            SELECT DISTINCT s.FILA || s.NUMERO AS CODIGO_ASIENTO
            FROM SILLA s
            INNER JOIN BOLETO_SILLA bs ON s.ID = bs.SILLA_ID
            INNER JOIN BOLETO b ON bs.BOLETO_ID = b.ID
            WHERE b.FUNCION_ID = ?
            AND b.ESTADO = 'APROBADO'
            AND b.ESTADO_BOOL = TRUE
        """;
        
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setLong(1, funcionId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigoAsiento = rs.getString("CODIGO_ASIENTO");
                    if (codigoAsiento != null && !codigoAsiento.trim().isEmpty()) {
                        ocupados.add(codigoAsiento.trim());
                    }
                }
            }
            
        } catch (SQLException e) {
            // En caso de error, retornar set vacío para no bloquear la UI
            e.printStackTrace();
        }
        
        return ocupados;
    }

    @Override
    public Set<String> obtenerAsientosAccesiblesPorFuncion(Long funcionId) {
        Set<String> accesibles = new HashSet<>();
        
        // Query para obtener asientos accesibles disponibles:
        // - Buscar sillas de tipo "ACCESIBLE" o similar
        // - Que estén en la sala de la función
        // - Que no estén ocupadas
        String sql = """
            SELECT DISTINCT s.FILA || s.NUMERO AS CODIGO_ASIENTO
            FROM SILLA s
            INNER JOIN SALA sa ON s.SALA_ID = sa.ID
            INNER JOIN FUNCION f ON sa.ID = f.SALA_ID
            WHERE f.ID = ?
            AND (UPPER(s.TIPO) LIKE '%ACCESIBLE%' OR UPPER(s.TIPO) LIKE '%DISABILITY%')
            AND s.ESTADO_BOOL = TRUE
            AND s.ID NOT IN (
                SELECT bs.SILLA_ID 
                FROM BOLETO_SILLA bs
                INNER JOIN BOLETO b ON bs.BOLETO_ID = b.ID
                WHERE b.FUNCION_ID = ?
                AND b.ESTADO = 'APROBADO'
                AND b.ESTADO_BOOL = TRUE
            )
        """;
        
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setLong(1, funcionId);
            ps.setLong(2, funcionId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigoAsiento = rs.getString("CODIGO_ASIENTO");
                    if (codigoAsiento != null && !codigoAsiento.trim().isEmpty()) {
                        accesibles.add(codigoAsiento.trim());
                    }
                }
            }
            
        } catch (SQLException e) {
            // En caso de error, usar datos por defecto
            e.printStackTrace();
            // Asientos accesibles por defecto (pueden ser configurables)
            accesibles.addAll(Set.of("E3", "E4", "E5", "E6"));
        }
        
        return accesibles;
    }
}