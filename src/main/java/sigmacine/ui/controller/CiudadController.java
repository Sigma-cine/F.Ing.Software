package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CiudadController {

    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    private Consumer<String> onCiudadSelected;

    @FXML
    private void initialize() {
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }

        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> {
                String ciudad = cbCiudad.getValue();
                if (onCiudadSelected != null) onCiudadSelected.accept(ciudad);
                // close popup
                Stage s = (Stage) btnSeleccionarCiudad.getScene().getWindow();
                s.close();
            });
        }
    }

    public void setOnCiudadSelected(Consumer<String> cb) {
        this.onCiudadSelected = cb;
    }
}
