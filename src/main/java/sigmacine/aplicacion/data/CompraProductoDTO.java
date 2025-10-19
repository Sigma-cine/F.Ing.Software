package sigmacine.aplicacion.data;

import java.math.BigDecimal;

public class CompraProductoDTO {
    private final Long productoId;
    private final Long funcionId; // opcional para boletos
    private final String nombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;

    public CompraProductoDTO(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.funcionId = null;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public CompraProductoDTO(Long productoId, Long funcionId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.funcionId = funcionId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getProductoId() { return productoId; }
    public Long getFuncionId() { return funcionId; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    @Override
    public String toString() {
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return nombre + (cantidad > 1 ? " (" + cantidad + ")" : "") + " - $" + format(total);
    }

    public String getTotalFormateado() {
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return "$" + format(total);
    }

    private String format(BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
