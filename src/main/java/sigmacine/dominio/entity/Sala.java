package sigmacine.dominio.entity;

public class Sala {
    private long id;
    private int numeroSala;
    private int capacidad;
    private String tipo;
    private long sedeId;

    public Sala() {}

    public Sala(long id, int numeroSala, int capacidad, String tipo, long sedeId) {
        this.id = id;
        this.numeroSala = numeroSala;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.sedeId = sedeId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getNumeroSala() { return numeroSala; }
    public void setNumeroSala(int numeroSala) { this.numeroSala = numeroSala; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public long getSedeId() { return sedeId; }
    public void setSedeId(long sedeId) { this.sedeId = sedeId; }
}
