package sigmacine.dominio.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import sigmacine.aplicacion.data.CompraProductoDTO;

public interface CompraRepository {

    Long guardarCompraProductos(int clienteId, LocalDate fecha, List<CompraProductoDTO> items,String metodoPago, BigDecimal total);
}
