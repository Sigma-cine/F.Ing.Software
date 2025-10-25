package sigmacine.dominio.entity;

public class Silla {
    private Long id;
    private String fila;
    private Integer numero;
    private String tipo;
    private Boolean estadoBool;
    private Long salaId;

    public Silla() {}

    public Silla(Long id, String fila, Integer numero, String tipo, Boolean estadoBool, Long salaId) {
        this.id = id;
        this.fila = fila;
        this.numero = numero;
        this.tipo = tipo;
        this.estadoBool = estadoBool;
        this.salaId = salaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getEstadoBool() {
        return estadoBool;
    }

    public void setEstadoBool(Boolean estadoBool) {
        this.estadoBool = estadoBool;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    @Override
    public String toString() {
        return "Silla{" +
                "id=" + id +
                ", fila='" + fila + '\'' +
                ", numero=" + numero +
                ", tipo='" + tipo + '\'' +
                ", estadoBool=" + estadoBool +
                ", salaId=" + salaId +
                '}';
    }
}
