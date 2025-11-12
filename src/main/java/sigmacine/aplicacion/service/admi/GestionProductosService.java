package sigmacine.aplicacion.service.admi;

import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.dominio.repository.admi.ProductoAdminRepository;

import java.util.List;

public class GestionProductosService {
    private final ProductoAdminRepository repo;

    public GestionProductosService(ProductoAdminRepository repo) {
        if (repo == null)
            throw new IllegalArgumentException("repo nulo");
        this.repo = repo;
    }

    public List<ProductoDTO> listarTodas() {
        return repo.listarTodas();
    }
}
