package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CiudadController {

    // Raíz y cabecera para manejar arrastre/cierre
    @FXML private StackPane rootOverlay;   // contenedor exterior (overlay/blurring)
    @FXML private HBox headerBar;          // barra superior (color vino)
    @FXML private Button btnCerrar;        // botón ✕

    // Controles de contenido
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    // Callback hacia el llamador
    private Consumer<String> onCiudadSelected;

    // offsets para arrastrar la ventana
    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    private void initialize() {
        // 1) Poblado de la lista de ciudades
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }

        // 2) Botón 'Seleccionar'
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> confirmAndClose());
        }

        // 3) Botón '✕' cerrar
        if (btnCerrar != null) {
            btnCerrar.setOnAction(e -> closeStage(btnCerrar));
        }

        // 4) Arrastre de la ventana con la cabecera
        Node dragZone = (headerBar != null) ? headerBar : rootOverlay; // fallback si no hay headerBar
        if (dragZone != null) {
            dragZone.setOnMousePressed(this::onDragPressed);
            dragZone.setOnMouseDragged(this::onDragDragged);
        }

        // 5) Tecla ESC para cerrar
        if (rootOverlay != null) {
            rootOverlay.sceneProperty().addListener((obs, old, scene) -> {
                if (scene != null) {
                    scene.setOnKeyPressed(ev -> {
                        if (ev.getCode() == KeyCode.ESCAPE) {
                            closeStage(rootOverlay);
                        }
                    });
                }
            });
        }
    }

    // --- Arrastre de ventana ---
    private void onDragPressed(MouseEvent e) {
        Stage s = getStageFrom((Node) e.getSource());
        if (s != null) {
            dragOffsetX = e.getScreenX() - s.getX();
            dragOffsetY = e.getScreenY() - s.getY();
        }
    }

    private void onDragDragged(MouseEvent e) {
        Stage s = getStageFrom((Node) e.getSource());
        if (s != null) {
            s.setX(e.getScreenX() - dragOffsetX);
            s.setY(e.getScreenY() - dragOffsetY);
        }
    }

    // Click en el logo (si lo agregaste en FXML con onMouseClicked="#onLogoClick")
    @FXML
    private void onLogoClick(MouseEvent e) {
        // Si quieres que el logo confirme y cierre como el botón:
        confirmAndClose();
        // Si prefieres solo cerrar sin confirmar, usa:
        // closeStage((Node) e.getSource());
    }

    // Callback para el llamador
    public void setOnCiudadSelected(Consumer<String> callback) {
        this.onCiudadSelected = callback;
    }

    // --- Helpers ---
    private void confirmAndClose() {
        String ciudad = cbCiudad != null ? cbCiudad.getValue() : null;
        if (onCiudadSelected != null) onCiudadSelected.accept(ciudad);
        if (btnSeleccionarCiudad != null) closeStage(btnSeleccionarCiudad);
    }

    private void closeStage(Node nodeInWindow) {
        Stage s = getStageFrom(nodeInWindow);
        if (s != null) s.close();
    }

    private Stage getStageFrom(Node n) {
        return (n != null && n.getScene() != null) ? (Stage) n.getScene().getWindow() : null;
        }
}
