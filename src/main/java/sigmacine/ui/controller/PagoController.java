package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML private VBox boletasContainer;
    @FXML private VBox confiteriaContainer;
    @FXML private Label lblTotal;
    @FXML private RadioButton rbTarjeta;
    @FXML private RadioButton rbPresencial;
    @FXML private RadioButton rbSigmaCard;
    @FXML private Button btnPagar;

    private final CarritoService carrito = CarritoService.getInstance();
    private CompraService compraService;
    private DatabaseConfig dbConfig;

    @FXML
    private void initialize() {
        // Configurar ToggleGroup para los RadioButtons
        ToggleGroup metodoPagoGroup = new ToggleGroup();
        rbTarjeta.setToggleGroup(metodoPagoGroup);
        rbPresencial.setToggleGroup(metodoPagoGroup);
        rbSigmaCard.setToggleGroup(metodoPagoGroup);
        
        cargarProductos();
        actualizarTotal();
        carrito.addListener(c -> {
            cargarProductos();
            actualizarTotal();
        });

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

    private void cargarProductos() {
        if (boletasContainer != null) boletasContainer.getChildren().clear();
        if (confiteriaContainer != null) confiteriaContainer.getChildren().clear();

        for (CompraProductoDTO item : carrito.getItems()) {
            if (item.getFuncionId() != null) {
                // Es una boleta
                if (boletasContainer != null) {
                    boletasContainer.getChildren().add(crearFilaProducto(item));
                }
            } else {
                // Es un producto de confitería
                if (confiteriaContainer != null) {
                    confiteriaContainer.getChildren().add(crearFilaProducto(item));
                }
            }
        }
    }

    private HBox crearFilaProducto(CompraProductoDTO item) {
        HBox row = new HBox();
        row.setStyle("-fx-spacing: 10; -fx-padding: 5; -fx-alignment: CENTER_LEFT;");

        // Imagen del producto/poster
        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        imgView.setFitWidth(40);
        imgView.setFitHeight(40);
        imgView.setPreserveRatio(true);
        imgView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        
        try {
            String imagePath = item.getImageUrl();
            System.out.println("[DEBUG] Cargando imagen para: " + item.getNombre());
            System.out.println("[DEBUG] ImageUrl: " + imagePath);
            
            if (imagePath != null && !imagePath.isEmpty()) {
                // Intentar cargar la imagen
                java.io.InputStream imgStream = getClass().getResourceAsStream(imagePath);
                System.out.println("[DEBUG] InputStream es null? " + (imgStream == null));
                
                if (imgStream != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(imgStream, 40, 40, true, true);
                    imgView.setImage(img);
                    System.out.println("[DEBUG] Imagen cargada exitosamente");
                } else {
                    System.err.println("[ERROR] No se encontró el recurso: " + imagePath);
                }
            } else {
                System.out.println("[DEBUG] ImageUrl está vacío o null");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción al cargar imagen: " + item.getImageUrl());
            e.printStackTrace();
        }

        // Contenedor vertical para nombre y detalles
        javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(2);
        
        Label lblNombre = new Label(item.getNombre() != null ? item.getNombre() : "Producto");
        lblNombre.setStyle("-fx-text-fill: #fff; -fx-font-size: 12; -fx-font-weight: bold;");
        lblNombre.setMaxWidth(180);
        lblNombre.setWrapText(true);
        
        // Línea de detalles (cantidad y asiento si es boleta)
        String detalles = "x" + item.getCantidad();
        if (item.getAsiento() != null && !item.getAsiento().isEmpty()) {
            detalles += " • Asiento: " + item.getAsiento();
        }
        Label lblDetalles = new Label(detalles);
        lblDetalles.setStyle("-fx-text-fill: #bbb; -fx-font-size: 10;");
        
        infoBox.getChildren().addAll(lblNombre, lblDetalles);

        Label lblPrecio = new Label("$" + (item.getPrecioUnitario() != null ? item.getPrecioUnitario().toPlainString() : "0"));
        lblPrecio.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 13;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(imgView, infoBox, spacer, lblPrecio);
        return row;
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

    @FXML
    private void onPagar() {
        realizarPago();
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

        String metodo;
        if (rbTarjeta != null && rbTarjeta.isSelected()) {
            metodo = "TARJETA";
        } else if (rbPresencial != null && rbPresencial.isSelected()) {
            metodo = "PRESENCIAL";
        } else {
            metodo = "SIGMACARD";
        }

        if (compraService == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de persistencia",
                    "No se pudo inicializar el servicio de compras. La compra no pudo ser guardada en la base de datos.");
            return;
        }

        try {
            List<CompraProductoDTO> items = new ArrayList<>(carrito.getItems());
            String totalCompra = carrito.getTotal().toString(); // Guardar el total antes de limpiar
            Long compraId = compraService.confirmarCompraProductos(user.getId(), items, metodo);
            
            // Mostrar alerta de confirmación simple
            if (compraId != null) {
                // Primero limpiamos el carrito
                carrito.clear();
                
                // Mostrar alerta de éxito
                mostrarAlerta(Alert.AlertType.INFORMATION, "¡Pago Exitoso!", 
                    "Tu compra ha sido procesada correctamente.\nID de compra: " + compraId + 
                    "\nTotal pagado: $" + totalCompra);
                
                // Regresar a la pantalla del carrito
                regresarAlCarrito();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Pago procesado", 
                    "La compra fue procesada pero no se pudo obtener el ID de confirmación.");
            }
            
        } catch (Exception ex) {
            System.err.println("[PagoController] Error al guardar la compra: " + ex.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error al procesar pago",
                    "Ocurrio un error al guardar la compra: " + ex.getMessage());
        }
    }

    private void regresarAlCarrito() {
        try {
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
                loader.setLocation(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
                javafx.scene.Parent root = loader.load();
                
                // Obtener la stage principal y cambiar su scene
                javafx.stage.Stage mainStage = coordinador.getMainStage();
                if (mainStage != null) {
                    javafx.scene.Scene currentScene = mainStage.getScene();
                    double w = currentScene != null ? currentScene.getWidth() : 960;
                    double h = currentScene != null ? currentScene.getHeight() : 600;
                    mainStage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 960, h > 0 ? h : 600));
                    mainStage.setTitle("Carrito");
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al regresar al carrito: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}