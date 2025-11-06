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
    public void registrarSigmaCard() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard("abc", "x"));
        assertTrue(svc.registrarCard("123", "0000"));
    }

    @Test
    public void recargarYConsultarSaldo() {
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

    @Test
    public void consultarSaldoNuloDevuelveCero() {
        SigmaCardService svc = new SigmaCardService(new SigmaCardRepository() {
            @Override public boolean crearSiNoExiste(long usuarioId) { return false; }
            @Override public BigDecimal recargar(long usuarioId, BigDecimal monto) { return BigDecimal.ZERO; }
            @Override public BigDecimal consultarSaldo(long usuarioId) { return null; }
        });
        BigDecimal saldo = svc.consultarSaldo("123");
        assertEquals(BigDecimal.ZERO, saldo);
    }

    @Test
    public void constructorSinArgumentos() {
        SigmaCardService svc = new SigmaCardService();
        assertNotNull(svc);
    }

    @Test
    public void formatConValorNulo() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        String formateado = svc.format(null);
        assertNotNull(formateado);
        assertTrue(formateado.contains("0"));
    }
}
