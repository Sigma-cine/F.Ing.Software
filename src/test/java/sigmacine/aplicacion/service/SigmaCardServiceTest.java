package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.dominio.repository.SigmaCardRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SigmaCardServiceTest {

    static class StubRepo implements SigmaCardRepository {
        boolean created = false;
        BigDecimal saldo = BigDecimal.valueOf(10);
        @Override public boolean crearSiNoExiste(long usuarioId) { this.created = true; return true; }
        @Override public BigDecimal recargar(long usuarioId, BigDecimal monto) { if (monto == null) throw new IllegalArgumentException("monto"); this.saldo = this.saldo.add(monto); return this.saldo; }
        @Override public BigDecimal consultarSaldo(long usuarioId) { return saldo; }
    }

    @Test
    public void registrar_and_parse_errors() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard("abc", "x"));
        assertTrue(svc.registrarCard("123", "0000"));
    }

    @Test
    public void recargar_and_consultar_and_format() {
        StubRepo r = new StubRepo();
        SigmaCardService svc = new SigmaCardService(r);
        assertThrows(IllegalArgumentException.class, () -> svc.recargar("1", null));
        BigDecimal nuevo = svc.recargar("1", BigDecimal.valueOf(5));
        assertEquals(BigDecimal.valueOf(15), nuevo);
    BigDecimal saldo = svc.consultarSaldo("1");
    assertEquals(BigDecimal.valueOf(15), saldo);
    String f = svc.format(BigDecimal.ZERO);
    assertNotNull(f);
    assertTrue(f.contains("0") || f.contains("00"));
    }
}
