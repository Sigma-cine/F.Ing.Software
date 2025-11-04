package sigmacine.dominio.repository;

import java.util.Set;

public interface SillaRepository {
    
    /**
     * Obtiene los códigos de asientos ocupados para una función específica.
     * Un asiento está ocupado si tiene un boleto asociado en esa función.
     * @param funcionId ID de la función
     * @return Set con códigos de asientos ocupados (ej: "A1", "B5")
     */
    Set<String> obtenerAsientosOcupadosPorFuncion(Long funcionId);
    
    /**
     * Obtiene los códigos de asientos accesibles (tipo especial) para una función específica.
     * @param funcionId ID de la función
     * @return Set con códigos de asientos accesibles
     */
    Set<String> obtenerAsientosAccesiblesPorFuncion(Long funcionId);
}