package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.math.BigDecimal;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CarritoService;

public class VerCarritoController {

    @FXML private StackPane carritoRoot;
    @FXML private ListView<CompraProductoDTO> listaItems;
    @FXML private Label lblTotal;       // total de tiquetes (subtotal)
    @FXML private Label lblTotalGlobal; // TOTAL grande (si lo agregamos en FXML)
    @FXML private Button btnProcederPago;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        if (listaItems != null) listaItems.setItems(carrito.getItems());
        updateTotal();
        carrito.addListener(c -> updateTotal());

        // Handler principal viene del FXML onAction="#onProcederPago"; no reasignamos aquí.
    }

    private void updateTotal() {
        try {
            BigDecimal total = carrito.getTotal();
            if (lblTotal != null) lblTotal.setText("$" + total.toPlainString());
            if (lblTotalGlobal != null) lblTotalGlobal.setText("$" + total.toPlainString());
        } catch (Exception ignore) {}
    }

    // Permite que otros controladores fuercen el refresco (compatibilidad)
    public void refresh() {
        if (listaItems != null) listaItems.refresh();
        updateTotal();
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
