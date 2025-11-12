package sigmacine.aplicacion.data;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductoDTO {
  private final Long productoId;
  private final String nombreProducto;
  private final String descripcionProducto;
  private final String imagenURL;
  private final String sabores;
  private final String tipo;
  private final BigDecimal precioLista;
  private final boolean estadoBoolean;
  private final String estado;

  public ProductoDTO(
      Long productoId,
      String nombreProducto,
      String descripcionProducto,
      String imagenURL,
      String sabores,
      String tipo,
      BigDecimal precioLista,
      boolean estadoBoolean,
      String estado) {
    this.productoId = productoId;
    this.nombreProducto = nombreProducto;
    this.descripcionProducto = descripcionProducto;
    this.imagenURL = imagenURL;
    this.sabores = sabores;
    this.tipo = tipo;
    this.precioLista = precioLista;
    this.estadoBoolean = estadoBoolean;
    this.estado = estado;
  }

  public String getDescripcionProducto() {
    return descripcionProducto;
  }

  public String getEstado() {
    return estado;
  }

  public String getImagenURL() {
    return imagenURL;
  }

  public String getNombreProducto() {
    return nombreProducto;
  }

  public BigDecimal getPrecioLista() {
    return precioLista;
  }

  public Long getProductoId() {
    return productoId;
  }

  public String getSabores() {
    return sabores;
  }

  public String getTipo() {
    return tipo;
  }

  public String getTotalFormateado() {
    BigDecimal total = precioLista;
    return "$" + format(total);
  }

  private String format(BigDecimal v) {
    return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;

    ProductoDTO that = (ProductoDTO) obj;

    return precioLista == that.precioLista &&
        productoId == that.productoId &&
        estadoBoolean == that.estadoBoolean &&
        Objects.equals(nombreProducto, that.nombreProducto) &&
        Objects.equals(descripcionProducto, that.descripcionProducto) &&
        Objects.equals(imagenURL, that.imagenURL) &&
        Objects.equals(sabores, that.sabores) &&
        Objects.equals(tipo, that.tipo) &&
        Objects.equals(estado, that.estado);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        productoId,
        nombreProducto,
        descripcionProducto,
        imagenURL,
        sabores,
        tipo,
        precioLista,
        estadoBoolean,
        estado);
  }
}
