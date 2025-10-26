package sigmacine.dominio.entity;

public class Boleto {
    private Long id;
    private String pelicula;
    private String sala;
    private String horario;
    private String asiento;
    private long precio;

    public Boleto(Long id, String pelicula, String sala, String horario, String asiento, long precio) {
        this.id = id;
        this.pelicula = pelicula;
        this.sala = sala;
        this.horario = horario;
        this.asiento = asiento;
        this.precio = precio;
    }

    // Constructor vacío para uso de frameworks o mapeo manual
    public Boleto() {
    }

    // Setters para permitir construcción por pasos
    public void setId(Long id) { this.id = id; }
    public void setPelicula(String pelicula) { this.pelicula = pelicula; }
    public void setSala(String sala) { this.sala = sala; }
    public void setHorario(String horario) { this.horario = horario; }
    public void setAsiento(String asiento) { this.asiento = asiento; }
    public void setPrecio(long precio) { this.precio = precio; }

    public String getPelicula() {
        return pelicula;
    }

    public String getSala() {
        return sala;
    }

    public String getHorario() {
        return horario;
    }
    
    public long getPrecio() {
        return precio;
    }

    public String getAsiento() { return asiento; }
}