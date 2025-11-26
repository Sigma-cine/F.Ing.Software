package sigmacine.aplicacion.data;

import java.time.LocalDate;

public class UsuarioDTO {

    private int id;
    private String email;
    private String rol;
    private String nombre;
    private String apellido;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String fechaRegistro;
    private String contrasena;

    // avatar
    private String avatarPath;

    // SigmaCard
    private String codigoSigmaCard;
    private double saldoSigmaCard;
    private int puntosSigmaCard;
    private String nivelSigmaCard;

    // ====== GETTERS ======
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getFechaRegistro() { return fechaRegistro; }
    public String getAvatarPath() { return avatarPath; }
    public String getContrasena() { return contrasena; }

    public String getCodigoSigmaCard() { return codigoSigmaCard; }
    public double getSaldoSigmaCard() { return saldoSigmaCard; }
    public int getPuntosSigmaCard() { return puntosSigmaCard; }
    public String getNivelSigmaCard() { return nivelSigmaCard; }

    // ====== SETTERS ======
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setRol(String rol) { this.rol = rol; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public void setCodigoSigmaCard(String codigoSigmaCard) { this.codigoSigmaCard = codigoSigmaCard; }
    public void setSaldoSigmaCard(double saldoSigmaCard) { this.saldoSigmaCard = saldoSigmaCard; }
    public void setPuntosSigmaCard(int puntosSigmaCard) { this.puntosSigmaCard = puntosSigmaCard; }
    public void setNivelSigmaCard(String nivelSigmaCard) { this.nivelSigmaCard = nivelSigmaCard; }
}
