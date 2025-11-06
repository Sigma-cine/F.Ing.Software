package sigmacine.aplicacion.data;

import java.math.BigDecimal;

public class CompraProductoDTO {
    private final Long productoId;
    private final Long funcionId;
    private final String asiento;
    private final String nombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private String sabor; // opcional: sabor/variante seleccionada

    public CompraProductoDTO(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.funcionId = null;
        this.asiento = null;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public CompraProductoDTO(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario, String sabor) {
        this.productoId = productoId;
        this.funcionId = null;
        this.asiento = null;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.sabor = sabor;
    }

    public CompraProductoDTO(Long productoId, Long funcionId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.funcionId = funcionId;
        this.asiento = null;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Constructor including asiento (para boletos)
    public CompraProductoDTO(Long productoId, Long funcionId, String nombre, int cantidad, BigDecimal precioUnitario, String asiento) {
        this.productoId = productoId;
        this.funcionId = funcionId;
        this.asiento = asiento;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getProductoId() { return productoId; }
    public Long getFuncionId() { return funcionId; }
    public String getAsiento() { return asiento; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public String getSabor() { return sabor; }

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
