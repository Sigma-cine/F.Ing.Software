package sigmacine.aplicacion.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import sigmacine.dominio.repository.SigmaCardRepository;
import sigmacine.infraestructura.persistencia.jdbc.SigmaCardRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

public class SigmaCardService {

	private final SigmaCardRepository repository;

	public SigmaCardService() {
		this.repository = new SigmaCardRepositoryJdbc(new DatabaseConfig());
	}

	// For tests or DI
	public SigmaCardService(SigmaCardRepository repository) {
		this.repository = repository;
	}

	public boolean registrarCard(String id, String pin) {
		long uid = parseUserId(id);
		return repository.crearSiNoExiste(uid);
	}

	public BigDecimal recargar(String id, BigDecimal monto) {
		if (monto == null) throw new IllegalArgumentException("Monto requerido");
		long uid = parseUserId(id);
		return repository.recargar(uid, monto);
	}

	public BigDecimal consultarSaldo(String id) {
		long uid = parseUserId(id);
		BigDecimal val = repository.consultarSaldo(uid);
		return val != null ? val : BigDecimal.ZERO;
	}

	private long parseUserId(String id) {
		if (id == null) throw new IllegalArgumentException("Identificación requerida");
		try {
			return Long.parseLong(id.trim());
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Identificación inválida: debe ser numérica");
		}
	}

	public String format(BigDecimal value) {
		if (value == null) value = BigDecimal.ZERO;
		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
		return nf.format(value);
	}
}
