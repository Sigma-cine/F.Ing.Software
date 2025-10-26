package sigmacine.dominio.entity;

public class Producto {
	private Long id;
	private String nombre;
	private long precio;

	public Producto() {}

	public Producto(Long id, String nombre, long precio) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public long getPrecio() { return precio; }
	public void setPrecio(long precio) { this.precio = precio; }

	@Override
	public String toString() {
		return nombre + " - $" + String.format("%.2f", precio / 100.0);
	}
}
