package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CompraService;
import sigmacine.dominio.repository.CompraRepository;
import sigmacine.infraestructura.persistencia.jdbc.CompraRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.aplicacion.session.Session;
import sigmacine.aplicacion.data.UsuarioDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class PagoController {

    @FXML private ListView<CompraProductoDTO> listaResumen;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbCredito;
    @FXML private RadioButton rbDebito;
    @FXML private Button btnPagar;

    private final CarritoService carrito = CarritoService.getInstance();
    private CompraService compraService;
    private DatabaseConfig dbConfig;

    @FXML
    private void initialize() {
        if (listaResumen != null) listaResumen.setItems(carrito.getItems());
        actualizarTotal();
        carrito.addListener(c -> actualizarTotal());
        if (btnPagar != null) btnPagar.setOnAction(e -> realizarPago());

        try {
            ToggleGroup tg = new ToggleGroup();
            if (rbCredito != null) rbCredito.setToggleGroup(tg);
            if (rbDebito != null) rbDebito.setToggleGroup(tg);
        } catch (Exception ignored) {}

        try {
            dbConfig = new DatabaseConfig();
            CompraRepository repo = new CompraRepositoryJdbc(dbConfig);
            compraService = new CompraService(repo);
        } catch (Exception ex) {
            compraService = null;
            System.err.println("AVISO: No se pudo inicializar CompraService: " + ex.getMessage());
            if (btnPagar != null) btnPagar.setDisable(true);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de configuracion",
                    "No se pudo inicializar la conexion a la base de datos. La compra no podra ser registrada.");
        }
    }

    private void actualizarTotal() {
        BigDecimal t = carrito.getTotal();
        if (lblTotal != null) lblTotal.setText("$" + t.toPlainString());
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void realizarPago() {
        UsuarioDTO user = Session.getCurrent();
        if (user == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "No autenticado",
                    "Debes iniciar sesion para realizar la compra.");
            return;
        }

        if (user.getId() <= 0 || user.getEmail() == null || user.getEmail().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cuenta no valida",
                    "Debes iniciar sesion con una cuenta registrada para que la compra se guarde en el historial.");
            return;
        }

        String metodo = (rbCredito != null && rbCredito.isSelected()) ? "CREDITO" : "DEBITO";

        if (compraService == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de persistencia",
                    "No se pudo inicializar el servicio de compras. La compra no pudo ser guardada en la base de datos.");
            return;
        }

        try {
            List<CompraProductoDTO> items = new ArrayList<>(carrito.getItems());
            Long compraId = compraService.confirmarCompraProductos(user.getId(), items, metodo);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Pago exitoso",
                    compraId != null ? "Compra realizada. ID=" + compraId : "Compra realizada.");
            carrito.clear();
            Stage st = (Stage) btnPagar.getScene().getWindow();
            st.close();
        } catch (Exception ex) {
            System.err.println("[PagoController] Error al guardar la compra: " + ex.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error al procesar pago",
                    "Ocurrio un error al guardar la compra: " + ex.getMessage());
        }
    }
}