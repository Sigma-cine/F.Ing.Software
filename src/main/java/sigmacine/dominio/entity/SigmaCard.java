package sigmacine.dominio.entity;

import java.math.BigDecimal;

public class SigmaCard {
    private Long id;
    private BigDecimal saldo;
    private boolean activa;

    public SigmaCard() {
        
    }
    
    public SigmaCard(Long id, BigDecimal saldoInicial) {
        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }
        this.id = id;
        this.saldo = saldoInicial;
        this.activa = true;
    }

    public void recargar(BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de recarga debe ser positivo.");
        }
        this.saldo = this.saldo.add(monto);
    }

    public boolean puedeDebitar(BigDecimal monto) {
        return this.activa && this.saldo.compareTo(monto) >= 0;
    }

    public void debitar(BigDecimal monto) {
        if (!puedeDebitar(monto)) {
            throw new IllegalStateException("Saldo insuficiente o tarjeta inactiva.");
        }
        this.saldo = this.saldo.subtract(monto);
    }

    public boolean estaActiva() {
        return activa;
    }

    public void desactivar() {
        this.activa = false;
    }
    
    // Getters
    public Long getId() {
        return id;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
