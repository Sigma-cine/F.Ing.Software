package sigmacine.dominio.repository;

import java.math.BigDecimal;

public interface SigmaCardRepository {

    boolean crearSiNoExiste(long usuarioId);

    BigDecimal recargar(long usuarioId, BigDecimal monto);

    BigDecimal consultarSaldo(long usuarioId);
}
