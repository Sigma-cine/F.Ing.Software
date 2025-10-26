package sigmacine.aplicacion.service;
import java.util.List;

import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.repository.UsuarioRepository;

public class VerHistorialService {
    public final UsuarioRepository repo;
    
    public VerHistorialService(UsuarioRepository repo) { this.repo = repo; }

   /*  public List<Compra> verHistorial(String emailPlano) {
        // Delegar directamente al repositorio. Esto evita depender de efectos secundarios
        // de `buscarPorEmail` y usa la consulta optimizada `verHistorial`.
        if (emailPlano == null || emailPlano.isBlank()) {
            throw new IllegalArgumentException("Email inválido para consultar historial");
        }
        return repo.verHistorial(emailPlano);
    }*/

    public List<HistorialCompraDTO> verHistorial(String emailPlano){
        return repo.verHistorial(emailPlano);
    }
    
}
