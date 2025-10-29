package sigmacine.ui.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

public class AgregarSalaController {

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtAforo;

    @FXML
    private TextField txtSede;

    @FXML
    private Button btnAgregarSala;

    @FXML
    void agregarSala(ActionEvent event) {
        String id = txtId.getText();
        String aforo = txtAforo.getText();
        String sede = txtSede.getText();

        if (id.isEmpty() || aforo.isEmpty() || sede.isEmpty()) {
            mostrarAlerta("Error", "Por favor llena todos los campos.");
            return;
        }

        mostrarAlerta("Éxito", "Sala agregada correctamente:\n" +
                "ID: " + id + "\nAforo: " + aforo + "\nSede: " + sede);

        txtId.clear();
        txtAforo.clear();
        txtSede.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
