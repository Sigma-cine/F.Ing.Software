package sigmacine.aplicacion.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory SigmaCard service used by the UI.
 * This is intentionally simple for the demo: it stores balances by card id in memory.
 */
public class SigmaCardService {

	private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();

	/**
	 * Register a card with id and pin. Pin is accepted but not validated in this simple impl.
	 * @return true if card was created, false if it already existed
	 */
	public boolean registrarCard(String id, String pin) {
		return balances.putIfAbsent(id, BigDecimal.ZERO) == null;
	}

	public BigDecimal recargar(String id, BigDecimal monto) {
		if (monto == null) throw new IllegalArgumentException("Monto requerido");
		balances.merge(id, monto, BigDecimal::add);
		return balances.get(id);
	}

	public BigDecimal consultarSaldo(String id) {
		return balances.getOrDefault(id, BigDecimal.ZERO);
	}

	public String format(BigDecimal value) {
		if (value == null) value = BigDecimal.ZERO;
		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
		return nf.format(value);
	}
}
