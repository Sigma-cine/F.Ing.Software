package sigmacine.dominio.repository.admi;

import java.util.List;

import sigmacine.aplicacion.data.ProductoDTO;

public interface ProductoAdminRepository {

    List<ProductoDTO> listarTodas();
}
