package sigmacine.dominio.entity;

import java.time.LocalDate;

/**
 * Dominio: Pago
 * Representa la información del pago asociada a una compra.
 */
public class Pago {
	private Long id;
	private String metodo;
	private double monto;
	private String estado;
	private boolean estadoBool;
	private LocalDate fecha;

	public Pago() {
	}

	public Pago(Long id, String metodo, double monto, String estado, boolean estadoBool, LocalDate fecha) {
		this.id = id;
		this.metodo = metodo;
		this.monto = monto;
		this.estado = estado;
		this.estadoBool = estadoBool;
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public double getMonto() {
		return monto;
	}

	public void setMonto(double monto) {
		this.monto = monto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public boolean isEstadoBool() {
		return estadoBool;
	}

	public void setEstadoBool(boolean estadoBool) {
		this.estadoBool = estadoBool;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	@Override
	public String toString() {
		return "Pago{" +
				"id=" + id +
				", metodo='" + metodo + '\'' +
				", monto=" + monto +
				", estado='" + estado + '\'' +
				", estadoBool=" + estadoBool +
				", fecha=" + fecha +
				'}';
	}
}

