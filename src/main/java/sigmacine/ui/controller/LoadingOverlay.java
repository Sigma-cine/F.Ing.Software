package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Componente de overlay de carga que muestra un spinner mientras se cargan las pantallas
 */
public class LoadingOverlay {
    
    private final StackPane overlayPane;
    private final ProgressIndicator spinner;
    private final Label loadingLabel;
    
    public LoadingOverlay() {
        // Crear el spinner
        spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #8A2F24;");
        spinner.setPrefSize(60, 60);
        
        // Crear el label
        loadingLabel = new Label("Cargando...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Contenedor vertical para spinner y label
        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(spinner, loadingLabel);
        
        // Overlay con fondo semi-transparente
        overlayPane = new StackPane();
        overlayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlayPane.setAlignment(Pos.CENTER);
        overlayPane.getChildren().add(contentBox);
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
    }
    
    /**
     * Obtiene el pane del overlay para agregarlo a la escena
     */
    public StackPane getOverlayPane() {
        return overlayPane;
    }
    
    /**
     * Muestra el overlay de carga
     */
    public void show() {
        Platform.runLater(() -> {
            overlayPane.setVisible(true);
            overlayPane.setManaged(true);
            overlayPane.toFront();
        });
    }
    
    /**
     * Oculta el overlay de carga
     */
    public void hide() {
        Platform.runLater(() -> {
            overlayPane.setVisible(false);
            overlayPane.setManaged(false);
        });
    }
    
    /**
     * Actualiza el texto del mensaje de carga
     */
    public void setMessage(String message) {
        Platform.runLater(() -> loadingLabel.setText(message));
    }
}
