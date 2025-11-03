package sigmacine.ui.controller;
import javafx.fxml.FXML;

public class ConfiteriaController {
    private ControladorControlador coordinador;

    @FXML
    public void initialize() {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("confiteria");
        }
    }

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }
}