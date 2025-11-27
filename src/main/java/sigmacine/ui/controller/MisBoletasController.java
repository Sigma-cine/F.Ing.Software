package sigmacine.ui.controller;

import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import sigmacine.aplicacion.session.Session;
import sigmacine.aplicacion.data.UsuarioDTO;

public class MisBoletasController {
    
    @FXML
    private VBox boletasContainer;
    
    @FXML
    private Button btnVolver;
    
    private VerHistorialService historialService;
    private String usuarioEmail;

    public MisBoletasController() {
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            UsuarioRepositoryJdbc usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            this.historialService = new VerHistorialService(usuarioRepo);
        } catch (Exception e) {
            System.err.println("Error inicializando VerHistorialService: " + e.getMessage());
        }
    }

    public MisBoletasController(VerHistorialService historialService) {
        this.historialService = historialService;
    }
    
    public void setUsuarioEmail(String email) {
        this.usuarioEmail = email;
        if (this.boletasContainer != null) {
            this.boletasContainer.getChildren().clear();
            cargarBoletas();
        }
    }

    @FXML
    public void initialize() {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("misboletas");
        }
        
        if (btnVolver != null) {
            btnVolver.setOnAction(e -> onVolverAInicio());
        }
        
        // Obtener email del usuario actual
        UsuarioDTO usuario = Session.getCurrent();
        if (usuario != null) {
            this.usuarioEmail = usuario.getEmail();
        }
        
        if (boletasContainer != null) {
            boletasContainer.getChildren().clear(); 
            cargarBoletas();
        }
    }

    private void cargarBoletas() {
        if (usuarioEmail == null || usuarioEmail.isEmpty()) {
            boletasContainer.getChildren().add(new Label("Error: Email de usuario no disponible para la búsqueda."));
            return;
        }

        try {
            List<HistorialCompraDTO> historial = historialService.verHistorial(usuarioEmail);

            // Filtrar solo compras que tengan boletos (entradas de cine)
            List<HistorialCompraDTO> boletasActivas = historial.stream()
                .filter(dto -> dto.getCantBoletos() > 0)
                .toList();

            if (boletasActivas.isEmpty()) {
                VBox emptyState = new VBox(15);
                emptyState.setAlignment(javafx.geometry.Pos.CENTER);
                emptyState.setStyle("-fx-padding: 50;");
                
                Label emoji = new Label("🎬");
                emoji.setStyle("-fx-font-size: 48;");
                
                Label mensaje = new Label("No tienes boletas activas");
                mensaje.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18; -fx-font-weight: bold;");
                
                Label submensaje = new Label("Compra tus entradas para el cine y aparecerán aquí");
                submensaje.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
                
                emptyState.getChildren().addAll(emoji, mensaje, submensaje);
                boletasContainer.getChildren().add(emptyState);
                return;
            }

            for (HistorialCompraDTO dto : boletasActivas) {
                HBox tarjetaBoleta = crearTarjetaBoleta(dto);
                boletasContainer.getChildren().add(tarjetaBoleta);
            }

        } catch (IllegalArgumentException e) {
            boletasContainer.getChildren().add(new Label("Error: " + e.getMessage()));
        } catch (Exception e) {
            boletasContainer.getChildren().add(new Label("Ocurrió un error al cargar las boletas: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private HBox crearTarjetaBoleta(HistorialCompraDTO dto) {
        String peliculaTitulo = "Película";
        String ubicacion = dto.getSedeCiudad() != null ? dto.getSedeCiudad() : "N/A";
        String sala = "N/A";
        String horario = "N/A";
        String asientos = "";
        long precioTotal = 0;
        String fechaHora = "N/A";
        if (dto.getFuncionFecha() != null) {
            fechaHora = dto.getFuncionFecha().toString();
        } else if (dto.getCompraFecha() != null) {
            fechaHora = dto.getCompraFecha().toString();
        }

        HBox tarjeta = new HBox(20);
        tarjeta.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
        tarjeta.setPrefHeight(200);
        tarjeta.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #8B2E21; -fx-border-width: 2; -fx-border-radius: 10;");
        
        // Poster de la película
        ImageView posterView = new ImageView();
        posterView.setFitWidth(120);
        posterView.setFitHeight(160);
        posterView.setPreserveRatio(true);
        posterView.setSmooth(true);
        posterView.setStyle("-fx-effect: dropshadow( gaussian , rgba(0,0,0,0.6) , 8,0,0,2 ); -fx-background-radius: 8;");

        try {
            if (dto.getCompraId() != null && this.historialService != null && this.historialService.repo != null) {
                var boletos = this.historialService.repo.obtenerBoletosPorCompra(dto.getCompraId());
                if (boletos != null && !boletos.isEmpty()) {
                    // Obtener datos del primer boleto
                    peliculaTitulo = boletos.get(0).getPelicula() != null ? boletos.get(0).getPelicula() : peliculaTitulo;
                    sala = boletos.get(0).getSala() != null ? boletos.get(0).getSala() : sala;
                    horario = boletos.get(0).getHorario() != null ? boletos.get(0).getHorario() : horario;
                    
                    // Concatenar todos los asientos
                    StringBuilder asientosBuilder = new StringBuilder();
                    for (int i = 0; i < boletos.size(); i++) {
                        if (i > 0) asientosBuilder.append(", ");
                        asientosBuilder.append(boletos.get(i).getAsiento());
                        precioTotal += boletos.get(i).getPrecio();
                    }
                    asientos = asientosBuilder.toString();
                    
                    if (!peliculaTitulo.isBlank()) {
                        PeliculaRepositoryJdbc pr = new PeliculaRepositoryJdbc(new DatabaseConfig());
                        var matches = pr.buscarPorTitulo(peliculaTitulo);
                        if (matches != null && !matches.isEmpty()) {
                            String posterRef = matches.get(0).getPosterUrl();
                            try {
                                Image img = resolveImage(posterRef);
                                if (img != null) {
                                    posterView.setImage(img);
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        
        // Detalles de la boleta
        VBox detalles = new VBox(8);
        detalles.setPrefWidth(350);
        
        Label tituloLbl = new Label(peliculaTitulo);
        tituloLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20; -fx-font-weight: bold;");
        
        Label ubicacionLbl = new Label("📍 " + ubicacion + " - " + sala);
        ubicacionLbl.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
        
        Label fechaLbl = new Label("📅 " + fechaHora);
        fechaLbl.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
        
        Label horarioLbl = new Label("🕐 " + horario);
        horarioLbl.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
        
        Label asientosLbl = new Label("💺 Asientos: " + asientos);
        asientosLbl.setStyle("-fx-text-fill: #FFA500; -fx-font-size: 13;");
        asientosLbl.setWrapText(true);
        
        Label boletosLbl = new Label("🎟️ " + dto.getCantBoletos() + " boleto(s) - $" + String.format("%,d", precioTotal));
        boletosLbl.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14; -fx-font-weight: bold;");
        
        Label idLbl = new Label("ID: #" + (dto.getCompraId() != null ? dto.getCompraId().toString() : "N/A"));
        idLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 12;");
        
        detalles.getChildren().addAll(tituloLbl, ubicacionLbl, fechaLbl, horarioLbl, asientosLbl, boletosLbl, idLbl);
        HBox.setHgrow(detalles, javafx.scene.layout.Priority.ALWAYS);

        // Botón para ver QR
        VBox botonesBox = new VBox(10);
        botonesBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button btnVerQR = new Button("Ver QR");
        btnVerQR.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        btnVerQR.setPrefWidth(120);
        btnVerQR.setOnAction(e -> mostrarQRBoleta(dto));
        
        botonesBox.getChildren().add(btnVerQR);

        tarjeta.getChildren().addAll(posterView, detalles, botonesBox);
        return tarjeta;
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        
        String normalized = ref;
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        if (!normalized.startsWith("/Images/")) {
            if (normalized.startsWith("/Posters/") || normalized.startsWith("/Combos/")) {
                normalized = "/Images" + normalized;
            }
        }
        
        try {
            var stream = getClass().getResourceAsStream(normalized);
            if (stream != null) return new Image(stream);
        } catch (Exception e) {}
        
        return null;
    }

    private void mostrarQRBoleta(HistorialCompraDTO dto) {
        try {
            // Crear un Stage para el popup
            javafx.stage.Stage popupStage = new javafx.stage.Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("🎫 Boleta - ID #" + dto.getCompraId());
            
            // Crear contenido del popup
            VBox popupContent = new VBox(20);
            popupContent.setAlignment(javafx.geometry.Pos.CENTER);
            popupContent.setStyle("-fx-padding: 30; -fx-background-color: #1a1a1a;");
            
            // Título
            Label titulo = new Label("🎬 Código QR de tus Boletas");
            titulo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20; -fx-font-weight: bold;");
            
            // Generar QR
            ImageView qrImageView = new ImageView();
            qrImageView.setFitWidth(300);
            qrImageView.setFitHeight(300);
            qrImageView.setPreserveRatio(true);
            
            try {
                // Crear datos para el QR
                StringBuilder qrData = new StringBuilder();
                qrData.append("SIGMACINE - Compra #").append(dto.getCompraId()).append("\n");
                qrData.append("Boletos: ").append(dto.getCantBoletos()).append("\n");
                qrData.append("Total: ").append(dto.getTotal() != null ? String.format("%.2f", dto.getTotal().doubleValue()) : "0.00").append("\n");
                qrData.append("Sede: ").append(dto.getSedeCiudad() != null ? dto.getSedeCiudad() : "N/A");
                
                // Generar QR usando ZXing
                com.google.zxing.qrcode.QRCodeWriter qrWriter = new com.google.zxing.qrcode.QRCodeWriter();
                com.google.zxing.common.BitMatrix bitMatrix = qrWriter.encode(
                    qrData.toString(),
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    300, 300
                );
                
                // Convertir BitMatrix a Image
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
                javafx.scene.image.Image qrImage = new javafx.scene.image.Image(bais);
                qrImageView.setImage(qrImage);
            } catch (Exception e) {
                Label errorLbl = new Label("❌ Error generando QR");
                errorLbl.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 14;");
                popupContent.getChildren().add(errorLbl);
            }
            
            // Contenedor blanco para el QR
            VBox qrContainer = new VBox();
            qrContainer.setAlignment(javafx.geometry.Pos.CENTER);
            qrContainer.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
            qrContainer.getChildren().add(qrImageView);
            
            // Información
            Label info = new Label("Presenta este código QR en la entrada del cine");
            info.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13;");
            info.setWrapText(true);
            info.setMaxWidth(300);
            info.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Botón cerrar
            Button btnCerrar = new Button("Cerrar");
            btnCerrar.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 30; -fx-background-radius: 5; -fx-font-weight: bold;");
            btnCerrar.setOnAction(e -> popupStage.close());
            
            popupContent.getChildren().addAll(titulo, qrContainer, info, btnCerrar);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(popupContent, 400, 550);
            popupStage.setScene(scene);
            popupStage.setResizable(false);
            popupStage.show();
            
        } catch (Exception ex) {
            System.err.println("Error mostrando QR: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolverAInicio() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            UsuarioDTO usuario = Session.getCurrent();
            if (usuario != null) {
                coordinador.mostrarHome(usuario);
            } else {
                coordinador.mostrarPaginaInicial();
            }
        }
    }
}
