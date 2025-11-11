package sigmacine.ui.controller.admin;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class AgregarItemController {

    @FXML
    private TextField nombreItemField;

    @FXML
    private ComboBox<String> extensionComboBox;

    @FXML
    private TextField nuevaExtensionField;

    @FXML
    private Button adjuntarFotoButton;

    @FXML
    private Button agregarExtensionButton;

    @FXML
    private Button agregarItemButton;

    @FXML
    private Label mensajeLabel;

    private File imagenSeleccionada;

    @FXML
    public void initialize() {
        // Inicializar ComboBox con las extensiones predefinidas
        extensionComboBox.getItems().addAll("Tamaño", "Sabor", "Adiciones", "Toppings");
    }

    @FXML
    private void onAdjuntarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de imagen", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            imagenSeleccionada = file;
            mensajeLabel.setText("Imagen seleccionada: " + file.getName());
            mensajeLabel.setStyle("-fx-text-fill: lightgreen;");
        }
    }

    @FXML
    private void onAgregarExtension() {
        String nuevaExtension = nuevaExtensionField.getText().trim();
        if (!nuevaExtension.isEmpty()) {
            extensionComboBox.getItems().add(nuevaExtension);
            nuevaExtensionField.clear();
            mensajeLabel.setText("Extensión agregada correctamente.");
            mensajeLabel.setStyle("-fx-text-fill: lightgreen;");
        } else {
            mensajeLabel.setText("Debe ingresar un nombre para la extensión.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onAgregarItem() {
        String nombre = nombreItemField.getText().trim();
        String extension = extensionComboBox.getValue();

        if (nombre.isEmpty() || extension == null || imagenSeleccionada == null) {
            mensajeLabel.setText("Por favor complete todos los campos y agregue una imagen.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Aquí podrías llamar al servicio que guarda el item en la base de datos.
        // Por ahora solo mostramos el mensaje de éxito.
        mensajeLabel.setText("¡Item agregado correctamente!");
        mensajeLabel.setStyle("-fx-text-fill: lightgreen;");

        // Limpiar campos
        nombreItemField.clear();
        extensionComboBox.getSelectionModel().clearSelection();
        imagenSeleccionada = null;
    }
}
