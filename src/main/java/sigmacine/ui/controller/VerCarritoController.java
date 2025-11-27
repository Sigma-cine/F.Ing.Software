package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.math.BigDecimal;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CarritoService;

public class VerCarritoController {

    @FXML private StackPane carritoRoot;
    @FXML private VBox boletasContainer;
    @FXML private VBox confiteriaContainer;
    @FXML private Label lblTotalGlobal;
    @FXML private Button btnProcederPago;
    @FXML private ScrollPane scrollPane;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        updateCarrito();
        carrito.addListener(c -> updateCarrito());
    }

    private void updateCarrito() {
        // Guardar la posición actual del scroll antes de actualizar
        double scrollPosition = 0;
        if (scrollPane != null) {
            scrollPosition = scrollPane.getVvalue();
        }
        
        // Desactivar temporalmente el listener para evitar cambios automáticos
        final double savedPosition = scrollPosition;
        
        boletasContainer.getChildren().clear();
        confiteriaContainer.getChildren().clear();
        
        for (CompraProductoDTO item : carrito.getItems()) {
            // Si tiene funcionId es boleta, si no es confitería
            if (item.getFuncionId() != null && item.getFuncionId() > 0) {
                boletasContainer.getChildren().add(crearItemBoleta(item));
            } else {
                confiteriaContainer.getChildren().add(crearItemConfiteria(item));
            }
        }
        updateTotal();
        
        // Restaurar la posición del scroll después de que el layout se haya actualizado
        if (scrollPane != null) {
            final double finalPosition = savedPosition;
            // Usar applyCss y layout para forzar el recálculo antes de restaurar
            scrollPane.applyCss();
            scrollPane.layout();
            javafx.application.Platform.runLater(() -> {
                javafx.application.Platform.runLater(() -> {
                    scrollPane.setVvalue(finalPosition);
                });
            });
        }
    }

    private VBox crearItemBoleta(CompraProductoDTO item) {
        VBox vbox = new VBox();
        vbox.setStyle("-fx-spacing: 4; -fx-padding: 6; -fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-width: 1;");
        
        HBox hboxHeader = new HBox();
        hboxHeader.setStyle("-fx-spacing: 8; -fx-alignment: TOP_RIGHT;");
        
        Label label = new Label(item.toString());
        label.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Monospaced'; -fx-font-size: 10;");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        
        Button btnEliminar = new Button("✕");
        btnEliminar.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-padding: 4 8; -fx-font-weight: bold; -fx-font-size: 14;");
        btnEliminar.setPrefWidth(40);
        btnEliminar.setMinWidth(40);
        
        btnEliminar.setOnAction(ev -> {
            carrito.getItems().remove(item);
            updateCarrito();
        });
        
        hboxHeader.getChildren().addAll(label, btnEliminar);
        HBox.setHgrow(label, Priority.ALWAYS);
        
        vbox.getChildren().add(hboxHeader);
        
        return vbox;
    }

    private HBox crearItemConfiteria(CompraProductoDTO item) {
        HBox hbox = new HBox();
        hbox.setStyle("-fx-spacing: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 6; -fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-width: 1;");
        
        Label label = new Label(item.toString());
        label.setStyle("-fx-text-fill: #e0e0e0; -fx-font-family: 'Monospaced'; -fx-font-size: 10;");
        label.setWrapText(true);
        
        Button btnMenos = new Button("−");
        btnMenos.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-padding: 4 8; -fx-font-weight: bold; -fx-font-size: 11;");
        btnMenos.setPrefWidth(30);
        btnMenos.setOnAction(e -> {
            if (item.getCantidad() > 1) {
                CompraProductoDTO itemModificado = new CompraProductoDTO(
                    item.getProductoId(),
                    item.getNombre(),
                    item.getCantidad() - 1,
                    item.getPrecioUnitario(),
                    item.getSabor()
                );
                int index = carrito.getItems().indexOf(item);
                carrito.getItems().set(index, itemModificado);
                updateCarrito();
            } else {
                // Si cantidad es 1, eliminar el item
                carrito.getItems().remove(item);
                updateCarrito();
            }
        });
        
        Label lblCantidad = new Label(String.valueOf(item.getCantidad()));
        lblCantidad.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 11; -fx-min-width: 25; -fx-text-alignment: CENTER; -fx-alignment: CENTER;");
        lblCantidad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblCantidad, Priority.ALWAYS);
        
        Button btnMas = new Button("+");
        btnMas.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-padding: 4 8; -fx-font-weight: bold; -fx-font-size: 11;");
        btnMas.setPrefWidth(30);
        btnMas.setOnAction(e -> {
            CompraProductoDTO itemModificado = new CompraProductoDTO(
                item.getProductoId(),
                item.getNombre(),
                item.getCantidad() + 1,
                item.getPrecioUnitario(),
                item.getSabor()
            );
            int index = carrito.getItems().indexOf(item);
            carrito.getItems().set(index, itemModificado);
            updateCarrito();
        });
        
        hbox.getChildren().addAll(label, btnMenos, lblCantidad, btnMas);
        HBox.setHgrow(label, Priority.ALWAYS);
        
        return hbox;
    }

    // Permite que otros controladores fuercen el refresco (compatibilidad)
    public void refresh() {
        updateCarrito();
    }

    private void updateTotal() {
        try {
            BigDecimal total = carrito.getTotal();
            if (lblTotalGlobal != null) lblTotalGlobal.setText("$" + total.toPlainString());
        } catch (Exception ignore) {}
    }

    @FXML
    private void onProcederPago() {
        if (carrito.getItems().isEmpty()) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "El carrito está vacío.");
            a.setHeaderText(null);
            a.setTitle("Carrito vacío");
            a.showAndWait();
            return;
        }
        
        try {
            // Cerrar la ventana del carrito si está abierta como Stage
            javafx.stage.Stage carritoStage = (javafx.stage.Stage) carritoRoot.getScene().getWindow();
            if (carritoStage != null) {
                carritoStage.close();
            }
            
            // Obtener la ventana principal a través del coordinador
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
                java.net.URL fxmlUrl = getClass().getResource("/sigmacine/ui/views/pago.fxml");
            
                loader.setLocation(fxmlUrl);
                javafx.scene.Parent root = loader.load();
                
                // Usar la ventana principal del coordinador
                javafx.stage.Stage mainStage = coordinador.getMainStage();
                if (mainStage != null) {
                    javafx.scene.Scene currentScene = mainStage.getScene();
                    double w = currentScene != null ? currentScene.getWidth() : 960;
                    double h = currentScene != null ? currentScene.getHeight() : 600;
                    mainStage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 960, h > 0 ? h : 600));
                    mainStage.setTitle("Pago");
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al abrir pantalla de pago: " + ex.getMessage());
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la pantalla de pago: " + ex.getMessage()).showAndWait();
        }
    }
}
