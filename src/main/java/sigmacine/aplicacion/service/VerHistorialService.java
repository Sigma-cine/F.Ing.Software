package sigmacine.aplicacion.service;
import java.util.List;

import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.repository.UsuarioRepository;

public class VerHistorialService {
    public final UsuarioRepository repo;
    
    public VerHistorialService(UsuarioRepository repo) { this.repo = repo; }

    public List<HistorialCompraDTO> verHistorial(String emailPlano){
        return repo.verHistorial(emailPlano);
    }
    
}
