package sigmacine.dominio.repository.admi;

import java.util.List;

import sigmacine.aplicacion.data.ProductoDTO;

public interface ProductoAdminRepository {

    /**
     * Devuelve una lista de todos los productos disponibles en el sistema.
     * 
     * @return Lista de ProductoDTO.
     */
    List<ProductoDTO> listarTodas();

    /**
     * Persiste un nuevo producto en la fuente de datos.
     * 
     * @param dto El ProductoDTO a guardar (sin ID).
     * @return El ProductoDTO guardado con el ID asignado.
     */
    ProductoDTO guardarNuevo(ProductoDTO dto);

    /**
     * Busca productos por un criterio de búsqueda (típicamente nombre o
     * descripción).
     * 
     * @param q El término de búsqueda.
     * @return Una lista de ProductoDTO que coinciden con el criterio.
     */
    List<ProductoDTO> buscarPorCriterio(String q);

    /**
     * Actualiza los datos de un producto existente.
     * 
     * @param id      El ID del producto a modificar.
     * @param cambios El DTO con los datos actualizados.
     */
    void actualizar(Long id, ProductoDTO cambios);

    /**
     * Elimina un producto de la fuente de datos por su ID.
     * 
     * @param id El ID del producto a eliminar.
     */
    void eliminar(Long id);
}