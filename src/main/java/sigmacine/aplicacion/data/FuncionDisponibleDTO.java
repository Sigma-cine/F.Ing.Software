package sigmacine.aplicacion.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FuncionDisponibleDTO that = (FuncionDisponibleDTO) obj;
        return funcionId == that.funcionId &&
               peliculaId == that.peliculaId &&
               numeroSala == that.numeroSala &&
               Objects.equals(fecha, that.fecha) &&
               Objects.equals(hora, that.hora) &&
               Objects.equals(ciudad, that.ciudad) &&
               Objects.equals(sede, that.sede) &&
               Objects.equals(tipoSala, that.tipoSala);
    }

    @Override
    public int hashCode() {
        return Objects.hash(funcionId, peliculaId, fecha, hora, ciudad, sede, numeroSala, tipoSala);
    }

    @Override
    public String toString() {
        return "FuncionDisponibleDTO{" +
                "funcionId=" + funcionId +
                ", peliculaId=" + peliculaId +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", ciudad='" + ciudad + '\'' +
                ", sede='" + sede + '\'' +
                ", numeroSala=" + numeroSala +
                ", tipoSala='" + tipoSala + '\'' +
                '}';
    }
}
