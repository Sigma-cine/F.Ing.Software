package sigmacine.infraestructura.persistencia.jdbc.admi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            while (rs.next()){ 
                out.add(ProductoMapper.map(rs));
            }
            return out;

        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("table") && msg.contains("not found")) {
                System.err.println("PeliculaRepositoryJdbc: tabla PELICULA no encontrada (buscarTodas) - devolviendo lista vacía");
                return new ArrayList<>();
            }
            throw new RuntimeException("Error en el listado de las peliculas", e);
        }
    }
}
