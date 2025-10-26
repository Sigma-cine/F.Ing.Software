package sigmacine.dominio.repository;

import java.math.BigDecimal;

public interface SigmaCardRepository {
    /**
     * Ensure a SIGMA_CARD row exists for the given user id. Returns true if a new row was created, false if it already existed.
     */
    boolean crearSiNoExiste(long usuarioId);

    /**
     * Add monto to the user's sigma card and return the new saldo.
     */
    BigDecimal recargar(long usuarioId, BigDecimal monto);

    /**
     * Return the current saldo (or null if no card exists).
     */
    BigDecimal consultarSaldo(long usuarioId);
}
