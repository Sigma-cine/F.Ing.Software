package sigmacine.aplicacion.service;

import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.repository.CompraRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CompraService {

    private final CompraRepository compraRepository;

    public CompraService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    public Long confirmarCompraProductos(int clienteId, List<CompraProductoDTO> items, String metodoPago) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La compra no tiene productos.");
        }

        // Calcular total (BigDecimal) y validaciones mínimas
        BigDecimal total = BigDecimal.ZERO;
        for (CompraProductoDTO it : items) {
            if (it.getProductoId() == null) {
                throw new IllegalArgumentException("Hay un item de tipo boleto; no se inserta en COMPRA_PRODUCTO.");
            }
            if (it.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para producto " + it.getProductoId());
            }
            BigDecimal sub = it.getPrecioUnitario().multiply(BigDecimal.valueOf(it.getCantidad()));
            total = total.add(sub);
        }

        LocalDate hoy = LocalDate.now();
        return compraRepository.guardarCompraProductos(clienteId, hoy, items, metodoPago, total);
    }
}
