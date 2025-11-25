package sigmacine.infraestructura.persistencia.jdbc.admi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.dominio.repository.admi.ProductoAdminRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.Mapper.ProductoMapper;

public class ProductoAdminRepositoryJdbc implements ProductoAdminRepository {
    private final DatabaseConfig db;

    public ProductoAdminRepositoryJdbc(DatabaseConfig db) {
        this.db = db;
    }

    @Override
    public List<ProductoDTO> listarTodas() {
        String sql = "SELECT * FROM PRODUCTO";

        try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            List<ProductoDTO> out = new ArrayList<>();
            while (rs.next()) {
                out.add(ProductoMapper.map(rs));
            }
            return out;

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("table") && msg.contains("not found")) {
                System.err.println(
                        "ProductoAdminRepositoryJdbc: tabla PRODUCTO no encontrada (listarTodas) - devolviendo lista vacía");
                return new ArrayList<>();
            }
            throw new RuntimeException("Error en el listado de productos", e);
        }
    }

    @Override
    public ProductoDTO guardarNuevo(ProductoDTO dto) {
        String sql = "INSERT INTO PRODUCTO (ID, NOMBRE, DESCRIPCION, IMAGEN_URL, SABORES, TIPO, PRECIO_LISTA, ESTADO, ESTADO_BOOL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Mapeo de parámetros
            ps.setLong(1, dto.getProductoId());
            ps.setString(2, dto.getNombreProducto());
            ps.setString(3, dto.getDescripcionProducto());
            ps.setString(4, dto.getImagenURL());
            ps.setString(5, dto.getSabores());
            ps.setString(6, dto.getTipo());
            ps.setBigDecimal(7, dto.getPrecioLista());
            ps.setString(8, dto.getEstado());
            ps.setBoolean(9, dto.getEstado().equals("ACTIVO"));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La creación del producto falló, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long newId = generatedKeys.getLong(1);

                    return new ProductoDTO(
                            newId,
                            dto.getNombreProducto(),
                            dto.getDescripcionProducto(),
                            dto.getImagenURL(),
                            dto.getSabores(),
                            dto.getTipo(),
                            dto.getPrecioLista(),
                            dto.getEstado().equals("ACTIVO"),
                            dto.getEstado());
                } else {
                    throw new SQLException("La creación del producto falló, no se obtuvo ID.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar nuevo producto", e);
        }
    }

    @Override
    public List<ProductoDTO> buscarPorCriterio(String q) {
        String sql = "SELECT * FROM PRODUCTO WHERE NOMBRE LIKE ? OR DESCRIPCION LIKE ?";
        String searchParam = "%" + q.trim() + "%";

        try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, searchParam);
            ps.setString(2, searchParam);

            try (ResultSet rs = ps.executeQuery()) {
                List<ProductoDTO> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(ProductoMapper.map(rs));
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos por criterio: " + q, e);
        }
    }

    @Override
    public void actualizar(Long id, ProductoDTO cambios) {
        String sql = "UPDATE PRODUCTO SET NOMBRE=?, DESCRIPCION=?, IMAGEN_URL=?, SABORES=?, TIPO=?, PRECIO_LISTA=?, ESTADO=?, ESTADO_BOOL=? WHERE ID = ?";

        try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            // Mapeo de parámetros
            ps.setString(1, cambios.getNombreProducto());
            ps.setString(2, cambios.getDescripcionProducto());
            ps.setString(3, cambios.getImagenURL());
            ps.setString(4, cambios.getSabores());
            ps.setString(5, cambios.getTipo());
            ps.setBigDecimal(6, cambios.getPrecioLista());
            ps.setString(7, cambios.getEstado());
            ps.setBoolean(8, cambios.getEstado().equals("ACTIVO"));
            ps.setLong(9, id); 

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Advertencia: No se encontró el producto con ID " + id + " para actualizar.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el producto con ID: " + id, e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM PRODUCTO WHERE ID = ?";

        try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Advertencia: No se encontró el producto con ID " + id + " para eliminar.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar eliminar el producto con ID: " + id +
                    ". Puede que otros registros dependan de él.", e);
        }
    }
}