package sigmacine.dominio.entity;

public class Producto {
	private Long id;
	private String nombre;
	private Float precio; // en centavos o unidad mínima
	private String imagenUrl;
	private String sabores;
	private String descripcion;
	private String tipo;
	private String estado;
	private boolean estadoBool;

	public Producto() {}

	public Producto(Long id, String nombre, Float precio) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
	}

	public Producto(Long id, String nombre, Float precio, String imagenUrl) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.imagenUrl = imagenUrl;
	}

	public Producto(Long id, String nombre, Float precio, String imagenUrl, String sabores) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.imagenUrl = imagenUrl;
		this.sabores = sabores;
	}
	public Producto(Long id, String nombre, String descripcion, String imagenUrl, String sabores, String tipo, float precio, String estado, boolean estadoBool) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.imagenUrl = imagenUrl;
		this.sabores = sabores;
		this.tipo = tipo;
		this.precio = precio;
		this.estado = estado;
		this.estadoBool = estadoBool;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public Float getPrecio() { return precio; }
	public void setPrecio(Float precio) { this.precio = precio; }

	public String getImagenUrl() { return imagenUrl; }
	public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

	public String getSabores() { return sabores; }
	public void setSabores(String sabores) { this.sabores = sabores; }

	@Override
	public String toString() {
		return nombre + " - $" + String.format("%.2f", precio / 100.0);
	}
}
