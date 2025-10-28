package sigmacine.dominio.entity;

/** Representa un producto (por ejemplo confitería) que puede añadirse al carrito. */
public class Producto {
	private Long id;
	private String nombre;
	private long precio; // en centavos o unidad mínima
	private String imagenUrl;

	public Producto() {}

	public Producto(Long id, String nombre, long precio) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
	}

	public Producto(Long id, String nombre, long precio, String imagenUrl) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.imagenUrl = imagenUrl;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public long getPrecio() { return precio; }
	public void setPrecio(long precio) { this.precio = precio; }

	public String getImagenUrl() { return imagenUrl; }
	public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

	@Override
	public String toString() {
		return nombre + " - $" + String.format("%.2f", precio / 100.0);
	}
}
