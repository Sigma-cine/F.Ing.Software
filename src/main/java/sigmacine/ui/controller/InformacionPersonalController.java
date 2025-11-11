package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.File;

public class InformacionPersonalController {

    @FXML private Button btnCambiarFoto;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private PasswordField txtNuevaClave;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        try {
            if (lblMensaje != null) lblMensaje.setVisible(false);
            if (btnCambiarFoto != null) btnCambiarFoto.setOnAction(e -> onCambiarFoto());
            if (btnCancelar != null) btnCancelar.setOnAction(e -> onCancelar());
            if (btnGuardar != null) btnGuardar.setOnAction(e -> onGuardar());
        } catch (Exception ignore) {}
    }

    @FXML
    private void onCambiarFoto() {
        try {
            Stage stage = null;
            if (btnCambiarFoto != null && btnCambiarFoto.getScene() != null) {
                stage = (Stage) btnCambiarFoto.getScene().getWindow();
            }
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                lblMensaje.setText("Foto seleccionada: " + f.getName());
                lblMensaje.setVisible(true);
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error seleccionando foto: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onCancelar() {
        try {
            Stage stage = null;
            if (btnCancelar != null && btnCancelar.getScene() != null) stage = (Stage) btnCancelar.getScene().getWindow();
            if (stage != null) stage.close();
            else if (lblMensaje != null) { lblMensaje.setVisible(false); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    public void onGuardar() {
        try {
            if (txtNombres == null || txtNombres.getText() == null || txtNombres.getText().isBlank()) {
                lblMensaje.setText("El nombre es requerido"); lblMensaje.setVisible(true); return;
            }
            lblMensaje.setText("Cambios guardados");
            lblMensaje.setVisible(true);
            new Alert(Alert.AlertType.INFORMATION, "Información guardada").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error guardando: " + ex.getMessage()).showAndWait();
        }
    }
}
