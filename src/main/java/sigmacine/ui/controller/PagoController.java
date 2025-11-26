package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.CompraService;
import sigmacine.aplicacion.service.PagoService;
import sigmacine.aplicacion.service.SigmaCardService;
import sigmacine.aplicacion.data.PagoTarjetaDTO;
import sigmacine.aplicacion.data.ResultadoPagoDTO;
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
    @FXML private RadioButton rbSigmaCard;
    @FXML private Button btnPagar;
    @FXML private VBox formularioPagoContainer;

    private final CarritoService carrito = CarritoService.getInstance();
    private CompraService compraService;
    private PagoService pagoService;
    private SigmaCardService sigmaCardService;
    private DatabaseConfig dbConfig;
    
    // Campos del formulario de tarjeta
    private ComboBox<String> cbTipoTarjeta;
    private TextField txtNumeroTarjeta;
    private TextField txtNombreTitular;
    private TextField txtCVV;
    private ComboBox<String> cbMes;
    private ComboBox<String> cbAnio;
    private TextField txtCedula;
    
    // Label para SigmaCard
    private Label lblSigmaCardSaldo;
    private Label lblSigmaCardInfo;

    @FXML
    private void initialize() {
        // Configurar ToggleGroup para los RadioButtons
        ToggleGroup metodoPagoGroup = new ToggleGroup();
        rbTarjeta.setToggleGroup(metodoPagoGroup);
        rbSigmaCard.setToggleGroup(metodoPagoGroup);
        
        // Listeners para cambios de método de pago
        rbTarjeta.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) mostrarFormularioTarjeta();
        });
        rbSigmaCard.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) mostrarFormularioSigmaCard();
        });
        
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
            sigmaCardService = new SigmaCardService();
            pagoService = new PagoService(sigmaCardService);
        } catch (Exception ex) {
            compraService = null;
            pagoService = null;
            System.err.println("AVISO: No se pudo inicializar servicios: " + ex.getMessage());
            if (btnPagar != null) btnPagar.setDisable(true);
            mostrarAlerta(Alert.AlertType.ERROR, "Error de configuracion",
                    "No se pudo inicializar la conexion a la base de datos. La compra no podra ser registrada.");
        }
        
        // Mostrar formulario inicial (tarjeta por defecto)
        mostrarFormularioTarjeta();
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
            
            if (imagePath != null && !imagePath.isEmpty()) {
                java.io.InputStream imgStream = getClass().getResourceAsStream(imagePath);
                
                if (imgStream != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(imgStream, 40, 40, true, true);
                    imgView.setImage(img);
                }
            }
        } catch (Exception e) {
            // Silently handle image loading errors
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

        BigDecimal montoTotal = carrito.getTotal();
        String metodo;
        ResultadoPagoDTO resultadoPago = null;
        BigDecimal saldoAnterior = null;
        BigDecimal saldoNuevo = null;

        // Procesar según el método de pago seleccionado
        if (rbTarjeta != null && rbTarjeta.isSelected()) {
            metodo = "TARJETA";
            resultadoPago = procesarPagoConTarjeta(montoTotal);
        } else if (rbSigmaCard != null && rbSigmaCard.isSelected()) {
            metodo = "SIGMACARD";
            // Guardar saldo anterior
            saldoAnterior = sigmaCardService.consultarSaldo(String.valueOf(user.getId()));
            resultadoPago = procesarPagoConSigmaCard(user.getId(), montoTotal);
            // Obtener saldo nuevo después del pago
            if (resultadoPago.isExitoso()) {
                saldoNuevo = sigmaCardService.consultarSaldo(String.valueOf(user.getId()));
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Método no seleccionado",
                    "Por favor, selecciona un método de pago.");
            return;
        }

        // Verificar si el pago fue exitoso
        if (resultadoPago == null || !resultadoPago.isExitoso()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Pago Rechazado",
                    resultadoPago != null ? resultadoPago.getMensaje() : "Error al procesar el pago.");
            return;
        }

        if (compraService == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de persistencia",
                    "No se pudo inicializar el servicio de compras. La compra no pudo ser guardada en la base de datos.");
            return;
        }

        try {
            List<CompraProductoDTO> items = new ArrayList<>(carrito.getItems());
            BigDecimal totalCompra = carrito.getTotal();
            Long compraId = compraService.confirmarCompraProductos(user.getId(), items, metodo);
            
            if (compraId != null) {
                // Limpiar carrito y mostrar pantalla de confirmación
                carrito.clear();
                mostrarConfirmacionCompra(compraId, metodo, totalCompra, items, saldoAnterior, saldoNuevo);
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
    
    /**
     * Procesa el pago con tarjeta de crédito/débito
     */
    private ResultadoPagoDTO procesarPagoConTarjeta(BigDecimal monto) {
        if (pagoService == null) {
            return new ResultadoPagoDTO(false, "Servicio de pago no disponible");
        }
        
        // Validar que todos los campos estén llenos
        if (cbTipoTarjeta == null || txtNumeroTarjeta == null || txtNombreTitular == null || 
            txtCVV == null || cbMes == null || cbAnio == null || txtCedula == null) {
            return new ResultadoPagoDTO(false, "Formulario no inicializado correctamente");
        }
        
        String tipoTarjeta = cbTipoTarjeta.getValue();
        String numeroTarjeta = txtNumeroTarjeta.getText();
        String nombreTitular = txtNombreTitular.getText();
        String cvv = txtCVV.getText();
        String mes = cbMes.getValue();
        String anio = cbAnio.getValue();
        String cedula = txtCedula.getText();
        
        // Crear DTO con los datos de la tarjeta
        PagoTarjetaDTO datosTarjeta = new PagoTarjetaDTO(
            tipoTarjeta, numeroTarjeta, nombreTitular, cvv, mes, anio, cedula
        );
        
        // Procesar el pago
        return pagoService.procesarPagoTarjeta(datosTarjeta, monto);
    }
    
    /**
     * Procesa el pago con SigmaCard
     */
    private ResultadoPagoDTO procesarPagoConSigmaCard(int usuarioId, BigDecimal monto) {
        if (pagoService == null) {
            return new ResultadoPagoDTO(false, "Servicio de pago no disponible");
        }
        
        return pagoService.procesarPagoSigmaCard(usuarioId, monto);
    }
    
    /**
     * Muestra el formulario para pago con tarjeta
     */
    private void mostrarFormularioTarjeta() {
        if (formularioPagoContainer == null) return;
        
        formularioPagoContainer.getChildren().clear();
        
        Label lblTitulo = new Label("Información de la Tarjeta");
        lblTitulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-padding: 0 0 10 0;");
        
        // Tipo de tarjeta
        Label lblTipo = new Label("Tipo de Tarjeta:");
        lblTipo.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        cbTipoTarjeta = new ComboBox<>();
        cbTipoTarjeta.getItems().addAll("credito", "debito");
        cbTipoTarjeta.setValue("credito");
        cbTipoTarjeta.setMaxWidth(Double.MAX_VALUE);
        cbTipoTarjeta.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white;");
        
        // Número de tarjeta
        Label lblNumero = new Label("Número de Tarjeta:");
        lblNumero.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        txtNumeroTarjeta = new TextField();
        txtNumeroTarjeta.setPromptText("1234 5678 9012 3456");
        txtNumeroTarjeta.setMaxWidth(Double.MAX_VALUE);
        txtNumeroTarjeta.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #666666;");
        
        // Nombre del titular
        Label lblNombre = new Label("Nombre del Titular:");
        lblNombre.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        txtNombreTitular = new TextField();
        txtNombreTitular.setPromptText("Como aparece en la tarjeta");
        txtNombreTitular.setMaxWidth(Double.MAX_VALUE);
        txtNombreTitular.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #666666;");
        
        // CVV y Fecha de expiración en la misma fila
        HBox rowCvvFecha = new HBox(10);
        
        VBox vboxCvv = new VBox(5);
        Label lblCVV = new Label("CVV:");
        lblCVV.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        txtCVV = new TextField();
        txtCVV.setPromptText("123");
        txtCVV.setPrefWidth(100);
        txtCVV.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #666666;");
        vboxCvv.getChildren().addAll(lblCVV, txtCVV);
        
        VBox vboxFecha = new VBox(5);
        Label lblFecha = new Label("Fecha de Expiración:");
        lblFecha.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        HBox hboxFecha = new HBox(5);
        cbMes = new ComboBox<>();
        cbMes.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        cbMes.setPromptText("Mes");
        cbMes.setPrefWidth(80);
        cbMes.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white;");
        
        cbAnio = new ComboBox<>();
        int anioActual = java.time.Year.now().getValue();
        for (int i = 0; i <= 10; i++) {
            cbAnio.getItems().add(String.valueOf(anioActual + i));
        }
        cbAnio.setPromptText("Año");
        cbAnio.setPrefWidth(90);
        cbAnio.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white;");
        hboxFecha.getChildren().addAll(cbMes, cbAnio);
        vboxFecha.getChildren().addAll(lblFecha, hboxFecha);
        
        rowCvvFecha.getChildren().addAll(vboxCvv, vboxFecha);
        
        // Cédula
        Label lblCedula = new Label("Cédula:");
        lblCedula.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");
        txtCedula = new TextField();
        txtCedula.setPromptText("Número de identificación");
        txtCedula.setMaxWidth(Double.MAX_VALUE);
        txtCedula.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #666666;");
        
        formularioPagoContainer.getChildren().addAll(
            lblTitulo, lblTipo, cbTipoTarjeta, lblNumero, txtNumeroTarjeta,
            lblNombre, txtNombreTitular, rowCvvFecha, lblCedula, txtCedula
        );
    }
    
    /**
    /**
     * Muestra el formulario para pago con SigmaCard
     */
    private void mostrarFormularioSigmaCard() {
        if (formularioPagoContainer == null) return;
        
        formularioPagoContainer.getChildren().clear();
        
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-padding: 15; -fx-background-color: #2a2a2a; -fx-background-radius: 5;");
        
        Label lblTitulo = new Label("🎫 SigmaCard");
        lblTitulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        // Verificar si el usuario tiene SigmaCard
        UsuarioDTO user = Session.getCurrent();
        if (user != null && sigmaCardService != null) {
            boolean tieneCard = sigmaCardService.tieneCard(user.getId());
            
            if (tieneCard) {
                BigDecimal saldo = sigmaCardService.consultarSaldo(String.valueOf(user.getId()));
                
                lblSigmaCardSaldo = new Label("Saldo disponible: " + sigmaCardService.format(saldo));
                lblSigmaCardSaldo.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14; -fx-font-weight: bold;");
                
                BigDecimal montoRequerido = carrito.getTotal();
                if (saldo.compareTo(montoRequerido) >= 0) {
                    lblSigmaCardInfo = new Label("✓ Tienes saldo suficiente para esta compra.");
                    lblSigmaCardInfo.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12;");
                } else {
                    lblSigmaCardInfo = new Label("⚠️ Saldo insuficiente. Necesitas recargar tu SigmaCard.");
                    lblSigmaCardInfo.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12; -fx-font-weight: bold;");
                }
                
                infoBox.getChildren().addAll(lblTitulo, lblSigmaCardSaldo, lblSigmaCardInfo);
            } else {
                lblSigmaCardInfo = new Label("⚠️ No tienes una SigmaCard registrada. Por favor, registra tu tarjeta primero en la sección de tu perfil.");
                lblSigmaCardInfo.setWrapText(true);
                lblSigmaCardInfo.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12; -fx-font-weight: bold;");
                
                infoBox.getChildren().addAll(lblTitulo, lblSigmaCardInfo);
            }
        }
        
        formularioPagoContainer.getChildren().add(infoBox);
    }
    
    /**
     * Muestra la pantalla de confirmación de compra
     */
    private void mostrarConfirmacionCompra(Long compraId, String metodoPago, BigDecimal totalPagado,
                                           List<CompraProductoDTO> items, BigDecimal saldoAnterior, BigDecimal saldoNuevo) {
        try {
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                coordinador.mostrarConfirmacionCompra(compraId, metodoPago, totalPagado, items, saldoAnterior, saldoNuevo);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", 
                    "No se pudo acceder al coordinador de vistas.");
            }
        } catch (Exception ex) {
            System.err.println("Error al mostrar confirmación de compra: " + ex.getMessage());
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", 
                "No se pudo mostrar la confirmación de compra: " + ex.getMessage());
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