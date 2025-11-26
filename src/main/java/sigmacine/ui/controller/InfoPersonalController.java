package sigmacine.ui.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.session.Session;
import java.time.LocalDate;

public class InfoPersonalController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private PasswordField txtNuevaClave;
    @FXML private ImageView avatarImage;
    @FXML private Label lblMensaje;

    private UsuarioDTO usuarioActual;
    private File nuevaImagen;

    @FXML
    public void initialize() {
        Platform.runLater(this::cargarDatos);
    }

    private void cargarDatos() {
        usuarioActual = Session.getCurrent();
        if (usuarioActual == null) return;

        txtNombres.setText(usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "");
        txtApellidos.setText(usuarioActual.getApellido() != null ? usuarioActual.getApellido() : "");
        txtTelefono.setText(usuarioActual.getTelefono() != null ? usuarioActual.getTelefono() : "");
        dpFechaNacimiento.setValue(usuarioActual.getFechaNacimiento() != null ? usuarioActual.getFechaNacimiento() : LocalDate.now());

        if (usuarioActual.getAvatarPath() != null && !usuarioActual.getAvatarPath().isBlank()) {
            avatarImage.setImage(new Image("file:" + usuarioActual.getAvatarPath()));
        }

        lblMensaje.setVisible(false);
    }

    @FXML
    private void onCambiarFoto() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            this.nuevaImagen = f;
            avatarImage.setImage(new Image(f.toURI().toString()));
        }
    }

    @FXML
    private void onGuardar() {
        if (usuarioActual == null) return;

        // Actualizar DTO en memoria
        usuarioActual.setNombre(txtNombres.getText());
        usuarioActual.setApellido(txtApellidos.getText());
        usuarioActual.setTelefono(txtTelefono.getText());
        usuarioActual.setFechaNacimiento(dpFechaNacimiento.getValue());
        if (!txtNuevaClave.getText().isBlank()) {
            usuarioActual.setContrasena(txtNuevaClave.getText());
        }

        // Guardar imagen en carpeta Images
        if (nuevaImagen != null) {
            try {
                String avatarPath = "/Images/" + usuarioActual.getId() + ".png";
                Files.createDirectories(Path.of("Images"));
                Files.copy(nuevaImagen.toPath(), Path.of(avatarPath), StandardCopyOption.REPLACE_EXISTING);
                usuarioActual.setAvatarPath(avatarPath);
            } catch (Exception e) {
                e.printStackTrace();
                lblMensaje.setText("Error al guardar imagen");
                lblMensaje.setVisible(true);
                return;
            }
        }

        lblMensaje.setText("Datos guardados en memoria");
        lblMensaje.setVisible(true);
    }

    @FXML
    private void onCancelar() {
        cargarDatos();
    }
}

