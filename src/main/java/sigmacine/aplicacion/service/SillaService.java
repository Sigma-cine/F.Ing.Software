package sigmacine.aplicacion.service;

import sigmacine.dominio.repository.SillaRepository;

import java.util.Set;

public class SillaService {

    private final SillaRepository sillaRepository;

    public SillaService(SillaRepository sillaRepository) {
        this.sillaRepository = sillaRepository;
    }

    /**
     * Obtiene los códigos de asientos ocupados para una función específica.
     * @param funcionId ID de la función
     * @return Set con códigos de asientos (ej: "A1", "B5", "C3")
     */
    public Set<String> obtenerAsientosOcupados(Long funcionId) {
        if (funcionId == null) {
            throw new IllegalArgumentException("FuncionId no puede ser nulo");
        }
        return sillaRepository.obtenerAsientosOcupadosPorFuncion(funcionId);
    }

    /**
     * Obtiene los códigos de asientos accesibles (disponibles) para una función específica.
     * @param funcionId ID de la función
     * @return Set con códigos de asientos accesibles
     */
    public Set<String> obtenerAsientosAccesibles(Long funcionId) {
        if (funcionId == null) {
            throw new IllegalArgumentException("FuncionId no puede ser nulo");
        }
        return sillaRepository.obtenerAsientosAccesiblesPorFuncion(funcionId);
    }
}