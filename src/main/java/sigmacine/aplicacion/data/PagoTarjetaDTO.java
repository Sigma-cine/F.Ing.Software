package sigmacine.aplicacion.data;

public class PagoTarjetaDTO {
    private String tipoTarjeta; // "credito" o "debito"
    private String numeroTarjeta;
    private String nombreTitular;
    private String cvv;
    private String mesExpiracion;
    private String anioExpiracion;
    private String cedula;

    public PagoTarjetaDTO() {}

    public PagoTarjetaDTO(String tipoTarjeta, String numeroTarjeta, String nombreTitular, 
                         String cvv, String mesExpiracion, String anioExpiracion, String cedula) {
        this.tipoTarjeta = tipoTarjeta;
        this.numeroTarjeta = numeroTarjeta;
        this.nombreTitular = nombreTitular;
        this.cvv = cvv;
        this.mesExpiracion = mesExpiracion;
        this.anioExpiracion = anioExpiracion;
        this.cedula = cedula;
    }

    // Getters y Setters
    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getMesExpiracion() {
        return mesExpiracion;
    }

    public void setMesExpiracion(String mesExpiracion) {
        this.mesExpiracion = mesExpiracion;
    }

    public String getAnioExpiracion() {
        return anioExpiracion;
    }

    public void setAnioExpiracion(String anioExpiracion) {
        this.anioExpiracion = anioExpiracion;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
}
