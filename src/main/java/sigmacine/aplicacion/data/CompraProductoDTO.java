package sigmacine.aplicacion.data;

import java.math.BigDecimal;
import java.util.Objects;

public class CompraProductoDTO {
    private final Long productoId;
    private final Long funcionId;
    private final String asiento;
    private final String nombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private String sabor;
    private String imageUrl;

    public CompraProductoDTO(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.funcionId = null;
        this.asiento = null;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.imageUrl = null;
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
    public String getImageUrl() { return imageUrl; }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompraProductoDTO that = (CompraProductoDTO) obj;
        return cantidad == that.cantidad &&
               Objects.equals(productoId, that.productoId) &&
               Objects.equals(funcionId, that.funcionId) &&
               Objects.equals(asiento, that.asiento) &&
               Objects.equals(nombre, that.nombre) &&
               Objects.equals(precioUnitario, that.precioUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, funcionId, asiento, nombre, cantidad, precioUnitario);
    }
}
