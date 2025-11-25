package sigmacine.aplicacion.data;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductoDTO {
  private Long productoId;
  private String nombreProducto;
  private String descripcionProducto;
  private String imagenURL;
  private String sabores;
  private String tipo;
  private BigDecimal precioLista;
  private boolean estadoBoolean;
  private String estado;

  public ProductoDTO() {
    this.productoId = 0l;
    this.nombreProducto = "";
    this.descripcionProducto = "";
    this.imagenURL = "";
    this.sabores = "";
    this.tipo = "";
    this.precioLista = BigDecimal.ZERO;
    this.estadoBoolean = false;
    this.estado = "";
  }

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

  public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public void setSabores(String sabores) {
        this.sabores = sabores;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPrecioLista(BigDecimal precioLista) {
        this.precioLista = precioLista;
    }

    public void setEstadoBoolean(boolean estadoBoolean) {
        this.estadoBoolean = estadoBoolean;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}
