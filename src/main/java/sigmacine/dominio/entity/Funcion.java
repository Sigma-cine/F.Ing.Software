package sigmacine.dominio.entity;

import java.sql.Date;
import java.sql.Time;

public class Funcion {

    private long id;
    private List<Pelicula> trailers;
    private Pelicula pelicula;
    private Sala sala;
    private Date fecha;
    private Time hora;
    private String estado;
    private Time duracion;
    private Boolean estadoBool;
    private long peliculaId;
    private long salaId;

    public Funcion() {}

    public Funcion(long id, Date fecha, Time hora, String estado, Time duracion, Boolean estadoBool, long peliculaId, long salaId) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.duracion = duracion;
        this.estadoBool = estadoBool;
        this.peliculaId = peliculaId;
        this.salaId = salaId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Time getHora() { return hora; }
    public void setHora(Time hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Time getDuracion() { return duracion; }
    public void setDuracion(Time duracion) { this.duracion = duracion; }

    public Boolean getEstadoBool() { return estadoBool; }
    public void setEstadoBool(Boolean estadoBool) { this.estadoBool = estadoBool; }

    public long getPeliculaId() { return peliculaId; }
    public void setPeliculaId(long peliculaId) { this.peliculaId = peliculaId; }

    public long getSalaId() { return salaId; }
    public void setSalaId(long salaId) { this.salaId = salaId; }
}
