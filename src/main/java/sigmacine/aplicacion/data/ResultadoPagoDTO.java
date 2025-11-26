package sigmacine.aplicacion.data;

public class ResultadoPagoDTO {
    private boolean exitoso;
    private String mensaje;
    private String codigoTransaccion;
    private String codigoBarras; // Para pagos en efectivo

    public ResultadoPagoDTO(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public ResultadoPagoDTO(boolean exitoso, String mensaje, String codigoTransaccion) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.codigoTransaccion = codigoTransaccion;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public void setCodigoTransaccion(String codigoTransaccion) {
        this.codigoTransaccion = codigoTransaccion;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }
}
