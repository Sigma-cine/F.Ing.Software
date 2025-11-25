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

    /**
     * Busca productos por un criterio (típicamente nombre o descripción).
     * 
     * @param q El término de búsqueda.
     * @return Una lista de ProductoDTO que coinciden con el criterio.
     */
    public List<ProductoDTO> buscarProductos(String q) {
        if (q == null || q.trim().isEmpty()) {
            return listarTodas();
        }
        return repo.buscarPorCriterio(q.trim());
    }

    /**
     * Crea y persiste un nuevo Producto.
     * 
     * @param dto El objeto ProductoDTO a crear.
     * @return El ProductoDTO creado con el ID asignado por la persistencia.
     * @throws IllegalArgumentException Si el DTO o el nombre son inválidos.
     */
    public ProductoDTO crear(ProductoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO de producto nulo.");
        }
        if (dto.getNombreProducto() == null || dto.getNombreProducto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }

        return repo.guardarNuevo(dto);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id      El ID del producto a actualizar.
     * @param cambios Los nuevos datos del producto.
     * @throws IllegalArgumentException Si el ID no existe o los datos son
     *                                  inválidos.
     */
    public void actualizar(Long id, ProductoDTO cambios) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de producto inválido.");
        }
        if (cambios == null) {
            throw new IllegalArgumentException("DTO de cambios nulo.");
        }
        if (cambios.getNombreProducto() == null || cambios.getNombreProducto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }

        repo.actualizar(id, cambios);
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id El ID del producto a eliminar.
     * @throws IllegalArgumentException Si el ID es inválido.
     */
    public void eliminar(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de producto inválido para eliminar.");
        }
        repo.eliminar(id);
    }
}