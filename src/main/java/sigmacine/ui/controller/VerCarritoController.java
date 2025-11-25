package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.math.BigDecimal;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CarritoService;

public class VerCarritoController {

    @FXML private StackPane carritoRoot;
    @FXML private VBox boletasContainer;
    @FXML private VBox confiteriaContainer;
    @FXML private Label lblTotalGlobal;
    @FXML private Button btnProcederPago;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        updateCarrito();
        carrito.addListener(c -> updateCarrito());
    }

    private void updateCarrito() {
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

    private void navegarAPantallaPago() {
        if (carrito.getItems().isEmpty()) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "El carrito está vacío.");
            a.setHeaderText(null);
            a.setTitle("Carrito vacío");
            a.showAndWait();
            return;
        }
        // Cargar el FXML de pago para usar PagoController (persistencia real)
        try {
            // Use getResourceAsStream to avoid URL/URI conversion issues on paths with
            // special characters (OneDrive, accents, spaces). FXMLLoader can load from InputStream.
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            try (java.io.InputStream in = getClass().getResourceAsStream("/sigmacine/ui/views/pago.fxml")) {
                if (in == null) throw new RuntimeException("Recurso /sigmacine/ui/views/pago.fxml no encontrado en el classpath.");
                javafx.scene.Parent root = loader.load(in);
                Stage stage = new Stage();
                stage.setTitle("Pago");
                stage.initModality(Modality.APPLICATION_MODAL);
                // intentar establecer owner si es posible
                try {
                    if (carritoRoot != null && carritoRoot.getScene() != null) {
                        Stage owner = (Stage) carritoRoot.getScene().getWindow();
                        if (owner != null) stage.initOwner(owner);
                    }
                } catch (Exception ignore) {}
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // fallback: informar al usuario y no cerrar la ventana principal
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de pago: " + ex.getMessage()).showAndWait();
        }
    }


    // Handler invocado desde el FXML (onAction="#onProcederPago")
    @FXML
    private void onProcederPago() {
        navegarAPantallaPago();
    }
}
