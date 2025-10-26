package sigmacine.aplicacion.data;

import java.time.LocalDate;
import java.time.LocalTime;

public class FuncionDisponibleDTO {
    private long funcionId;
    private long peliculaId;
    private LocalDate fecha;
    private LocalTime hora;
    private String ciudad;
    private String sede;
    private int numeroSala;
    private String tipoSala;

    public FuncionDisponibleDTO(long funcionId, long peliculaId, LocalDate fecha, LocalTime hora,
                                String ciudad, String sede, int numeroSala, String tipoSala) {
        this.funcionId = funcionId;
        this.peliculaId = peliculaId;
        this.fecha = fecha;
        this.hora = hora;
        this.ciudad = ciudad;
        this.sede = sede;
        this.numeroSala = numeroSala;
        this.tipoSala = tipoSala;
    }

    public long getFuncionId() { return funcionId; }
    public long getPeliculaId() { return peliculaId; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getCiudad() { return ciudad; }
    public String getSede() { return sede; }
    public int getNumeroSala() { return numeroSala; }
    public String getTipoSala() { return tipoSala; }
}
