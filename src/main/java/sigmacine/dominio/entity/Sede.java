package sigmacine.dominio.entity;

import java.util.ArrayList;
import java.util.List;

public class Sede {
    private long id;
    private String nombre;
    private String ciudad;
    private List<Sala> salas = new ArrayList<>();

    public Sede() {}

    public Sede(long id, String nombre, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public List<Sala> getSalas() { return salas; }
    public void setSalas(List<Sala> salas) { this.salas = salas != null ? salas : new ArrayList<>(); }

    public void addSala(Sala sala) {
        if (sala != null) {
            this.salas.add(sala);
            sala.setSedeId(this.id);
        }
    }
}
