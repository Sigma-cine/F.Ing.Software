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
    @FXML private Label lblTotal;
    @FXML private Label lblTotalGlobal;
    @FXML private Button btnProcederPago;

    private final CarritoService carrito = CarritoService.getInstance();

    @FXML
    private void initialize() {
        if (listaItems != null) listaItems.setItems(carrito.getItems());
        updateTotal();
        carrito.addListener(c -> updateTotal());

    }

    private void updateTotal() {
        try {
            BigDecimal total = carrito.getTotal();
            if (lblTotal != null) lblTotal.setText("$" + total.toPlainString());
            if (lblTotalGlobal != null) lblTotalGlobal.setText("$" + total.toPlainString());
        } catch (Exception ignore) {}
    }

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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            try (java.io.InputStream in = getClass().getResourceAsStream("/sigmacine/ui/views/pago.fxml")) {
                if (in == null) throw new RuntimeException("Recurso /sigmacine/ui/views/pago.fxml no encontrado en el classpath.");
                javafx.scene.Parent root = loader.load(in);
                Stage stage = new Stage();
                stage.setTitle("Pago");
                stage.initModality(Modality.APPLICATION_MODAL);
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
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de pago: " + ex.getMessage()).showAndWait();
        }
    }


    @FXML
    private void onProcederPago() {
        navegarAPantallaPago();
    }
}
