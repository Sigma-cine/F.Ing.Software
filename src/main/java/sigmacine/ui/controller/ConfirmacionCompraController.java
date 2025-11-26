package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.service.SigmaCardService;
import sigmacine.aplicacion.session.Session;
import sigmacine.aplicacion.data.UsuarioDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controlador para la pantalla de confirmación de compra
 */
public class ConfirmacionCompraController {

    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblIdCompra;
    @FXML private Label lblFechaCompra;
    @FXML private Label lblMetodoPago;
    @FXML private VBox boletasContainer;
    @FXML private VBox productosContainer;
    @FXML private Label lblTotal;
    @FXML private VBox sigmaCardContainer;
    @FXML private Label lblSaldoAnterior;
    @FXML private Label lblMontoDebitado;
    @FXML private Label lblSaldoNuevo;
    @FXML private VBox qrContainer;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRInfo;
    @FXML private Button btnDescargarQR;
    @FXML private Button btnVolverInicio;
    @FXML private Button btnVerHistorial;

    private Long compraId;
    private String metodoPago;
    private BigDecimal totalPagado;
    private List<CompraProductoDTO> items;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoNuevo;
    private SigmaCardService sigmaCardService;

    @FXML
    private void initialize() {
        sigmaCardService = new SigmaCardService();
        // Por defecto ocultar el contenedor de SigmaCard
        if (sigmaCardContainer != null) {
            sigmaCardContainer.setVisible(false);
            sigmaCardContainer.setManaged(false);
        }
    }

    /**
     * Inicializa la pantalla de confirmación con los datos de la compra
     */
    public void inicializar(Long compraId, String metodoPago, BigDecimal totalPagado, 
                           List<CompraProductoDTO> items, BigDecimal saldoAnterior, BigDecimal saldoNuevo) {
        this.compraId = compraId;
        this.metodoPago = metodoPago;
        this.totalPagado = totalPagado;
        this.items = items;
        this.saldoAnterior = saldoAnterior;
        this.saldoNuevo = saldoNuevo;

        cargarDatosCompra();
        cargarItems();
        mostrarInformacionMetodoPago();
        generarCodigoQR();
    }

    private void cargarDatosCompra() {
        if (lblIdCompra != null) {
            lblIdCompra.setText("ID de Compra: #" + compraId);
        }

        if (lblFechaCompra != null) {
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblFechaCompra.setText("Fecha: " + ahora.format(formatter));
        }

        if (lblMetodoPago != null) {
            String metodoTexto = switch (metodoPago) {
                case "TARJETA" -> "💳 Tarjeta de Crédito/Débito";
                case "SIGMACARD" -> "🎫 SigmaCard";
                case "PRESENCIAL" -> "💵 Efectivo en Caja";
                default -> metodoPago;
            };
            lblMetodoPago.setText(metodoTexto);
        }

        if (lblTotal != null) {
            lblTotal.setText(formatearMoneda(totalPagado));
        }
    }

    private void cargarItems() {
        if (boletasContainer != null) {
            boletasContainer.getChildren().clear();
        }
        if (productosContainer != null) {
            productosContainer.getChildren().clear();
        }

        for (CompraProductoDTO item : items) {
            HBox row = crearFilaItem(item);
            
            if (item.getFuncionId() != null) {
                // Es una boleta
                if (boletasContainer != null) {
                    boletasContainer.getChildren().add(row);
                }
            } else {
                // Es un producto de confitería
                if (productosContainer != null) {
                    productosContainer.getChildren().add(row);
                }
            }
        }
    }

    private HBox crearFilaItem(CompraProductoDTO item) {
        HBox row = new HBox(15);
        row.setStyle("-fx-padding: 10; -fx-background-color: #2a2a2a; -fx-background-radius: 5; -fx-alignment: CENTER_LEFT;");

        // Icono o imagen
        Label lblIcono = new Label(item.getFuncionId() != null ? "🎬" : "🍿");
        lblIcono.setStyle("-fx-font-size: 24; -fx-text-fill: white;");

        // Información del item
        VBox infoBox = new VBox(5);
        
        Label lblNombre = new Label(item.getNombre() != null ? item.getNombre() : "Producto");
        lblNombre.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14; -fx-font-weight: bold;");
        lblNombre.setMaxWidth(300);
        lblNombre.setWrapText(true);

        String detalles = "Cantidad: " + item.getCantidad();
        if (item.getAsiento() != null && !item.getAsiento().isEmpty()) {
            detalles += " • Asiento: " + item.getAsiento();
        }
        Label lblDetalles = new Label(detalles);
        lblDetalles.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12;");

        infoBox.getChildren().addAll(lblNombre, lblDetalles);

        // Precio
        BigDecimal precioTotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
        Label lblPrecio = new Label(formatearMoneda(precioTotal));
        lblPrecio.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14; -fx-font-weight: bold;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(lblIcono, infoBox, spacer, lblPrecio);
        return row;
    }

    private void mostrarInformacionMetodoPago() {
        if ("SIGMACARD".equals(metodoPago) && sigmaCardContainer != null) {
            sigmaCardContainer.setVisible(true);
            sigmaCardContainer.setManaged(true);

            if (lblSaldoAnterior != null && saldoAnterior != null) {
                lblSaldoAnterior.setText("Saldo anterior: " + formatearMoneda(saldoAnterior));
            }

            if (lblMontoDebitado != null && saldoAnterior != null && saldoNuevo != null) {
                BigDecimal montoDebitado = saldoAnterior.subtract(saldoNuevo);
                lblMontoDebitado.setText("Monto debitado: " + formatearMoneda(montoDebitado));
            }

            if (lblSaldoNuevo != null && saldoNuevo != null) {
                lblSaldoNuevo.setText("Saldo actual: " + formatearMoneda(saldoNuevo));
            }
        }
    }

    private void generarCodigoQR() {
        try {
            // Crear información para el QR
            UsuarioDTO usuario = Session.getCurrent();
            StringBuilder qrData = new StringBuilder();
            qrData.append("SIGMACINE - Compra #").append(compraId).append("\n");
            qrData.append("Cliente: ").append(usuario != null ? usuario.getEmail() : "N/A").append("\n");
            qrData.append("Total: ").append(formatearMoneda(totalPagado)).append("\n");
            qrData.append("Fecha: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            
            // Agregar información de boletas
            int numBoletas = 0;
            for (CompraProductoDTO item : items) {
                if (item.getFuncionId() != null) {
                    numBoletas += item.getCantidad();
                    qrData.append("\n").append(item.getNombre());
                    if (item.getAsiento() != null && !item.getAsiento().isEmpty()) {
                        qrData.append(" - Asiento: ").append(item.getAsiento());
                    }
                }
            }

            if (numBoletas > 0) {
                // Generar código QR
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrData.toString(), BarcodeFormat.QR_CODE, 250, 250);

                // Convertir a imagen
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                // Mostrar en ImageView
                Image qrImage = new Image(inputStream);
                if (imgQRCode != null) {
                    imgQRCode.setImage(qrImage);
                }

                if (lblQRInfo != null) {
                    lblQRInfo.setText("Presenta este código QR en la entrada del cine para acceder a tu función");
                }
            } else {
                // No hay boletas, ocultar sección QR
                if (qrContainer != null) {
                    qrContainer.setVisible(false);
                    qrContainer.setManaged(false);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al generar código QR: " + e.getMessage());
            e.printStackTrace();
            
            if (lblQRInfo != null) {
                lblQRInfo.setText("No se pudo generar el código QR. Puedes usar tu ID de compra en la entrada.");
            }
        }
    }

    @FXML
    private void onDescargarQR() {
        if (imgQRCode.getImage() == null) {
            mostrarAlerta(javafx.scene.control.Alert.AlertType.WARNING, 
                "No disponible", 
                "No hay código QR para descargar.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar Código QR");
        fileChooser.setInitialFileName("boletas_compra_" + compraId + ".png");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("PNG Images", "*.png")
        );

        javafx.stage.Stage stage = (javafx.stage.Stage) btnDescargarQR.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Regenerar el QR y guardarlo
                UsuarioDTO usuario = Session.getCurrent();
                StringBuilder qrData = new StringBuilder();
                qrData.append("SIGMACINE - Compra #").append(compraId).append("\n");
                qrData.append("Cliente: ").append(usuario != null ? usuario.getEmail() : "N/A").append("\n");
                qrData.append("Total: ").append(formatearMoneda(totalPagado)).append("\n");
                
                for (CompraProductoDTO item : items) {
                    if (item.getFuncionId() != null) {
                        qrData.append("\n").append(item.getNombre());
                        if (item.getAsiento() != null) {
                            qrData.append(" - Asiento: ").append(item.getAsiento());
                        }
                    }
                }

                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrData.toString(), BarcodeFormat.QR_CODE, 400, 400);
                MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
                
                mostrarAlerta(javafx.scene.control.Alert.AlertType.INFORMATION, 
                    "Código Guardado", 
                    "El código QR se guardó exitosamente en: " + file.getAbsolutePath());
            } catch (Exception e) {
                mostrarAlerta(javafx.scene.control.Alert.AlertType.ERROR, 
                    "Error", 
                    "No se pudo guardar el código QR: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onVolverInicio() {
        try {
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                coordinador.mostrarPaginaInicial();
            }
        } catch (Exception e) {
            System.err.println("Error al volver al inicio: " + e.getMessage());
        }
    }

    @FXML
    private void onVerHistorial() {
        try {
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                coordinador.mostrarHistorialCompras();
            }
        } catch (Exception e) {
            System.err.println("Error al mostrar historial: " + e.getMessage());
        }
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return nf.format(valor);
    }

    private void mostrarAlerta(javafx.scene.control.Alert.AlertType tipo, String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
