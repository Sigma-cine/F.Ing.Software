package sigmacine.aplicacion.data;

public class UsuarioDTO {
    private int id;
    private String email;
    private String rol;
    private String nombre;
    private String fechaRegistro;

    // SigmaCard fields
    private String codigoSigmaCard;
    private double saldoSigmaCard;
    private int puntosSigmaCard;
    private String nivelSigmaCard;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
    public String getFechaRegistro() { return fechaRegistro; }

    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setRol(String rol) { this.rol = rol; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // SigmaCard getters and setters
    public String getCodigoSigmaCard() { return codigoSigmaCard; }
    public void setCodigoSigmaCard(String codigoSigmaCard) { this.codigoSigmaCard = codigoSigmaCard; }

    public double getSaldoSigmaCard() { return saldoSigmaCard; }
    public void setSaldoSigmaCard(double saldoSigmaCard) { this.saldoSigmaCard = saldoSigmaCard; }

    public int getPuntosSigmaCard() { return puntosSigmaCard; }
    public void setPuntosSigmaCard(int puntosSigmaCard) { this.puntosSigmaCard = puntosSigmaCard; }

    public String getNivelSigmaCard() { return nivelSigmaCard; }
    public void setNivelSigmaCard(String nivelSigmaCard) { this.nivelSigmaCard = nivelSigmaCard; }
}
