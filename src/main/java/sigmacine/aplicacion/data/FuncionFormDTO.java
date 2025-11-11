package sigmacine.aplicacion.data;

public class FuncionFormDTO {
    public long id;          // 0 si es nuevo
    public String fecha;     // "yyyy-MM-dd"
    public String hora;      // "HH:mm"
    public String estado;    // "Activa" | "Inactiva"
    public String duracion;  // "HH:mm"
    public Boolean estadoBool;
    public long peliculaId;
    public long salaId;
}
