package sigmacine.dominio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public class Usuario {

    public enum Rol { CLIENTE, ADMIN }

    private final int id;
    private final Email email;
    private final PasswordHash passwordHash;

    private final Rol rol;

    private String nombre;

    private LocalDate fechaRegistro;
    private SigmaCard sigmaCard;
    private List<Compra> compras;

    private Usuario(int id, Email email, PasswordHash passwordHash, Rol rol, String nombre, LocalDate fechaRegistro, SigmaCard sigmaCard, List<Compra> compras) {

        if (rol == null) throw new IllegalArgumentException("Rol requerido");
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombre = nombre;
        this.fechaRegistro = fechaRegistro;
        this.sigmaCard = sigmaCard;
        this.compras = (compras != null) ? compras : new ArrayList<>();
    }

    
    public static Usuario crearAdmin(int id, Email email, PasswordHash passwordHash, String nombre) {
        return new Usuario(id, email, passwordHash, Rol.ADMIN, nombre, null, null, new ArrayList<>());
    }

    public static Usuario crearCliente(int id, Email email, PasswordHash passwordHash, String nombre, LocalDate fechaRegistro) {
        return new Usuario(id, email, passwordHash, Rol.CLIENTE, nombre, fechaRegistro, new SigmaCard(), new ArrayList<>());
    }
    
    public static Usuario crearCliente(int id, Email email, PasswordHash passwordHash, String nombre, LocalDate fechaRegistro, SigmaCard sigmaCard) {
        return new Usuario(id, email, passwordHash, Rol.CLIENTE, nombre, fechaRegistro, sigmaCard != null ? sigmaCard : new SigmaCard(), new ArrayList<>());
    }

    public boolean autenticar(String plainPassword) {
        return this.passwordHash != null && this.passwordHash.matches(plainPassword);
    }

    public boolean esAdmin()   { return this.rol == Rol.ADMIN; }
    public boolean esCliente() { return this.rol == Rol.CLIENTE; }

    public void agregarCompra(Compra compra) {
        assertCliente();
        this.compras.add(compra);
        
    }

    public void recargarSigmaCard(BigDecimal monto) {
        assertCliente();
        this.sigmaCard.recargar(monto);
    }

    public BigDecimal consultarSaldoSigmaCard() {
        assertCliente();
        return this.sigmaCard.getSaldo();
    }

    private void assertCliente() {
        if (!esCliente()) {
            throw new IllegalStateException("Operación permitida solo para usuarios CLIENTE.");
        }
    }


    public int getId() { return id; }
    public Email getEmail() { return email; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    
    public String getNombre() { return nombre; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public SigmaCard getSigmaCard() { return sigmaCard; }
    public List<Compra> getCompras() { return compras; }

    public void setNombre(String nombre) { this.nombre = nombre; }
}
