//
package sigmacine.aplicacion.service;

import org.junit.jupiter.api.Test;
import sigmacine.dominio.repository.SigmaCardRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SigmaCardServiceTest {

    static class StubRepo implements SigmaCardRepository {
        boolean created = false;
        BigDecimal saldo = BigDecimal.valueOf(10);
        boolean existe = false;
        @Override public boolean crearSiNoExiste(long usuarioId) { this.created = true; this.existe = true; return true; }
        @Override public BigDecimal recargar(long usuarioId, BigDecimal monto) { if (monto == null) throw new IllegalArgumentException("monto"); this.saldo = this.saldo.add(monto); return this.saldo; }
        @Override public BigDecimal consultarSaldo(long usuarioId) { return saldo; }
        @Override public boolean existeCard(long usuarioId) { return existe; }
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
            @Override public boolean existeCard(long usuarioId) { return false; }
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

    @Test
    public void registrarCardConIdNulo() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard(null, "1234"));
    }

    @Test
    public void registrarCardConIdVacio() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> svc.registrarCard("", "1234"));
    }

    @Test
    public void registrarCardConIdConEspacios() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertTrue(svc.registrarCard("  123  ", "0000"));
    }

    @Test
    public void recargarConIdInvalido() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> 
            svc.recargar("no-numerico", BigDecimal.valueOf(10)));
    }

    @Test
    public void recargarConMontoNegativo() {
        StubRepo repo = new StubRepo();
        SigmaCardService svc = new SigmaCardService(repo);
        // El repositorio podría validar esto, pero el servicio no lo hace
        BigDecimal resultado = svc.recargar("1", BigDecimal.valueOf(-5));
        assertEquals(BigDecimal.valueOf(5), resultado);
    }

    @Test
    public void recargarConMontoCero() {
        StubRepo repo = new StubRepo();
        SigmaCardService svc = new SigmaCardService(repo);
        BigDecimal resultado = svc.recargar("1", BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(10), resultado);
    }

    @Test
    public void consultarSaldoConIdInvalido() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        assertThrows(IllegalArgumentException.class, () -> 
            svc.consultarSaldo("abc"));
    }

    @Test
    public void formatConValorGrande() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        String formateado = svc.format(new BigDecimal("1000000.50"));
        assertNotNull(formateado);
        assertTrue(formateado.contains("1") || formateado.contains("000"));
    }

    @Test
    public void formatConValorDecimal() {
        SigmaCardService svc = new SigmaCardService(new StubRepo());
        String formateado = svc.format(new BigDecimal("123.45"));
        assertNotNull(formateado);
        assertTrue(formateado.contains("123") || formateado.contains("45"));
    }

    @Test
    public void recargarActualizaSaldo() {
        StubRepo repo = new StubRepo();
        SigmaCardService svc = new SigmaCardService(repo);
        
        BigDecimal saldoInicial = svc.consultarSaldo("1");
        svc.recargar("1", BigDecimal.valueOf(20));
        BigDecimal saldoFinal = svc.consultarSaldo("1");
        
        assertEquals(saldoInicial.add(BigDecimal.valueOf(20)), saldoFinal);
    }
        @Test
    public void testTieneCard() {
        StubRepo repo = new StubRepo();
        SigmaCardService svc = new SigmaCardService(repo);
        // Al inicio, no tiene tarjeta
        repo.existe = false;
        assertFalse(svc.tieneCard(1L));
        // Simular que ahora tiene tarjeta
        repo.existe = true;
        assertTrue(svc.tieneCard(1L));
    }
}

