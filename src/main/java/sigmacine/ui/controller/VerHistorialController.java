package sigmacine.ui.controller;

import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.data.HistorialCompraDTO;

public class VerHistorialController {
    
    @FXML
    private VBox comprasContainer;
    
    @FXML
    private Button btnVolver;
    
    @FXML
    private javafx.scene.layout.HBox localTopbar;
    
    private ClienteController clienteController;
    private javafx.scene.Scene previousScene;
    
    private final VerHistorialService historialService;
    private String usuarioEmail;

    public VerHistorialController(VerHistorialService historialService) {
        this.historialService = historialService;
        // constructor - diagnostics removed
    }
    
    public void setClienteController(ClienteController controller) {
        this.clienteController = controller;
    }

    public void setPreviousScene(javafx.scene.Scene scene) {
        this.previousScene = scene;
    }
    
    public void setUsuarioEmail(String email) {
        this.usuarioEmail = email;
        if (this.comprasContainer != null) {
            this.comprasContainer.getChildren().clear();
            cargarHistorialDeCompras();
        }
    }

    @FXML
    public void initialize() {
        if (btnVolver != null) {
            btnVolver.setOnAction(e -> onVolverAInicio());
        }
        if (comprasContainer != null) {
            comprasContainer.getChildren().clear(); 
            cargarHistorialDeCompras();
            javafx.application.Platform.runLater(() -> limpiarNodosPromocionales());
        }

        try {
            if (localTopbar != null && comprasContainer != null && comprasContainer.getScene() != null) {
                var root = comprasContainer.getScene().getRoot();
                boolean sceneHasTopbar = root.lookup(".topbar") != null;
                if (sceneHasTopbar) {
                    localTopbar.setVisible(false);
                    localTopbar.setManaged(false);
                }
            }
        } catch (Exception ignore) {}
    }

    @FXML
    private void onBrandClick() {
        try {
            javafx.stage.Stage stage = null;
            if (btnVolver != null && btnVolver.getScene() != null) {
                stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
            } else if (comprasContainer != null && comprasContainer.getScene() != null) {
                stage = (javafx.stage.Stage) comprasContainer.getScene().getWindow();
            }
            if (stage != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
                javafx.scene.Parent root = loader.load();
                try {
                    Object ctrl = loader.getController();
                    if (ctrl instanceof ClienteController) {
                        ClienteController c = (ClienteController) ctrl;
                        var current = sigmacine.aplicacion.session.Session.getCurrent();
                        if (current != null) c.init(current);
                    }
                } catch (Exception ignore) {}
                javafx.scene.Scene currentScene = stage.getScene();
                double w = currentScene != null ? currentScene.getWidth() : 1000;
                double h = currentScene != null ? currentScene.getHeight() : 700;
                stage.setScene(new javafx.scene.Scene(root, w, h));
                stage.setTitle("Sigma Cine");
                stage.setMaximized(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarNodosPromocionales() {
        try {
            if (comprasContainer == null) return;
            var scene = comprasContainer.getScene();
            if (scene == null) return;
            var root = scene.getRoot();
            eliminarRecursivo(root);
        } catch (Exception e) {
            System.err.println("Advertencia: no se pudo limpiar nodos promocionales: " + e.getMessage());
        }
    }

    private void eliminarRecursivo(javafx.scene.Parent parent) {
        if (parent == null) return;
        if (parent instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
            java.util.List<javafx.scene.Node> copia = new java.util.ArrayList<>(pane.getChildren());
            for (javafx.scene.Node n : copia) {
                boolean removed = false;
                if (n instanceof javafx.scene.control.Button) {
                    javafx.scene.control.Button b = (javafx.scene.control.Button) n;
                    String t = b.getText();
                    if (t != null && t.trim().equalsIgnoreCase("Ver más")) {
                        pane.getChildren().remove(n);
                        removed = true;
                    }
                }
                if (!removed && n instanceof javafx.scene.control.Label) {
                    javafx.scene.control.Label lab = (javafx.scene.control.Label) n;
                    String lt = lab.getText();
                    if (lt != null && lt.trim().equalsIgnoreCase("Label")) {
                        pane.getChildren().remove(n);
                        removed = true;
                    }
                }
                if (!removed && n instanceof javafx.scene.Parent) {
                    eliminarRecursivo((javafx.scene.Parent) n);
                }
            }
        } else {
            java.util.List<javafx.scene.Node> children = parent.getChildrenUnmodifiable();
            for (javafx.scene.Node n : children) {
                if (n instanceof javafx.scene.Parent) eliminarRecursivo((javafx.scene.Parent) n);
            }
        }
    }
    
    private void cargarHistorialDeCompras() {
        if (usuarioEmail == null || usuarioEmail.isEmpty()) {
            comprasContainer.getChildren().add(new Label("Error: Email de usuario no disponible para la búsqueda."));
            return;
        }

        try {
            List<HistorialCompraDTO> historial = historialService.verHistorial(usuarioEmail);

            if (historial.isEmpty()) {
                comprasContainer.getChildren().add(new Label("No has realizado ninguna compra aún."));
                return;
            }

            for (HistorialCompraDTO dto : historial) {
                HBox tarjetaCompra = crearTarjetaCompra(dto);
                comprasContainer.getChildren().add(tarjetaCompra);
            }

        } catch (IllegalArgumentException e) {
            comprasContainer.getChildren().add(new Label("Error: " + e.getMessage()));
        } catch (Exception e) {
            comprasContainer.getChildren().add(new Label("Ocurrió un error al cargar el historial: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private HBox crearTarjetaCompra(HistorialCompraDTO dto) {

        String titulo = dto.getCantBoletos() > 0 ? "Compra de Entradas" : "Compra de Confitería";
        String ubicacion = dto.getSedeCiudad() != null ? dto.getSedeCiudad() : "N/A";
        String fechaHora = "N/A";
        if (dto.getCompraFecha() != null) fechaHora = dto.getCompraFecha().toString();
        else if (dto.getFuncionFecha() != null) fechaHora = dto.getFuncionFecha().toString();

        HBox tarjeta = new HBox(20);
        tarjeta.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        tarjeta.setPrefHeight(150);
    tarjeta.getStyleClass().addAll("tarjeta-compra", "card-wrap", "centered-container");
        
        VBox detalles = new VBox(6);
        Label tituloLbl = new Label(titulo);
        tituloLbl.getStyleClass().add("section-subtitle");
        detalles.getChildren().add(tituloLbl);

        Label ubicacionLbl = new Label("Ubicación: " + ubicacion);
        ubicacionLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(ubicacionLbl);

    // Mostrar fecha si está disponible
    String fechaText = fechaHora;
        Label fechaLbl = new Label("Fecha/Hora: " + fechaText);
        fechaLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(fechaLbl);

        Label idLbl = new Label("ID: " + (dto.getCompraId() != null ? dto.getCompraId().toString() : "N/A"));
        idLbl.getStyleClass().add("small-muted");
        detalles.getChildren().add(idLbl);
        if (dto.getCantBoletos() > 0) {
            Label boletosLbl = new Label("Boletos: " + dto.getCantBoletos());
            boletosLbl.getStyleClass().add("small-muted");
            detalles.getChildren().add(boletosLbl);
        }
        if (dto.getCantProductos() > 0) {
            Label productosLbl = new Label("Productos: " + dto.getCantProductos());
            productosLbl.getStyleClass().add("small-muted");
            detalles.getChildren().add(productosLbl);
        }

        String totalStr = dto.getTotal() != null ? String.format("%.2f", dto.getTotal().doubleValue()) : "0.00";
        Label total = new Label("Total: " + totalStr);
        total.getStyleClass().add("venue-box");
        detalles.getChildren().add(total);

        HBox.setHgrow(detalles, javafx.scene.layout.Priority.ALWAYS);

        ImageView posterView = new ImageView();
        posterView.setFitWidth(100);
        posterView.setFitHeight(130);
        posterView.setPreserveRatio(true);
        posterView.setSmooth(true);
        posterView.setStyle("-fx-effect: dropshadow( gaussian , rgba(0,0,0,0.6) , 6,0,0,2 );");

        // intentar cargar poster a partir de un boleto asociado a la compra
        try {
            if (dto.getCompraId() != null && this.historialService != null && this.historialService.repo != null) {
                var boletos = this.historialService.repo.obtenerBoletosPorCompra(dto.getCompraId());
                if (boletos != null && !boletos.isEmpty()) {
                    String tituloPelicula = boletos.get(0).getPelicula();
                    if (tituloPelicula != null && !tituloPelicula.isBlank()) {
                        PeliculaRepositoryJdbc pr = new PeliculaRepositoryJdbc(new DatabaseConfig());
                        var matches = pr.buscarPorTitulo(tituloPelicula);
                        if (matches != null && !matches.isEmpty()) {
                            String posterRef = matches.get(0).getPosterUrl();
                            try {
                                Image img = resolveImage(posterRef);
                                if (img != null) posterView.setImage(img);
                            } catch (Exception ignore) {}
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // no bloquear la UI por poster
        }

        // Añadir imagen, detalles y botón "Ver detalle"
        Button btnDetalle = new Button("Ver detalle");
        btnDetalle.getStyleClass().add("primary-btn");
        btnDetalle.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/detalleCompra.fxml"));
                // inyectar DTO y repositorio en el controller
                loader.setControllerFactory(cls -> {
                    if (cls == sigmacine.ui.controller.DetalleCompraController.class) {
                        return new sigmacine.ui.controller.DetalleCompraController(dto, this.historialService.repo);
                    }
                    try { return cls.getDeclaredConstructor().newInstance(); } catch (Exception ex) { throw new RuntimeException(ex); }
                });
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Detalle compra " + (dto.getCompraId() != null ? dto.getCompraId() : ""));
                stage.setScene(new javafx.scene.Scene(root));
                stage.initOwner(comprasContainer.getScene().getWindow());
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox accionBox = new VBox();
        accionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        accionBox.getChildren().add(btnDetalle);

        tarjeta.getChildren().addAll(posterView, detalles, accionBox);
        return tarjeta;
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String r = ref;
            String lower = ref.toLowerCase();
            if (lower.contains("src") && (lower.contains("images") || lower.contains("img"))) {
                int idx = Math.max(ref.lastIndexOf('/'), ref.lastIndexOf('\\'));
                if (idx >= 0 && idx + 1 < ref.length()) r = ref.substring(idx + 1);
            }
            java.net.URL res = getClass().getResource("/Images/" + r);
            if (res != null) return new Image(res.toExternalForm(), false);
            // try as absolute URL / file
            java.io.File f = new java.io.File(ref);
            if (f.exists()) return new Image(f.toURI().toString(), false);
            return new Image(ref, true);
        } catch (Exception e) { return null; }
    }
    
    @FXML
    private void onVolverAInicio() {
        // always go to the main page (pagina_inicial.fxml)
        try {
            var scene = btnVolver != null ? btnVolver.getScene() : (comprasContainer != null ? comprasContainer.getScene() : null);
            if (scene != null && scene.getWindow() instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
                javafx.scene.Parent root = loader.load();
                try {
                    Object ctrl = loader.getController();
                    if (ctrl instanceof ClienteController) {
                        ClienteController c = (ClienteController) ctrl;
                        var current = sigmacine.aplicacion.session.Session.getCurrent();
                        if (current != null) {
                            c.init(current);
                        }
                    }
                } catch (Exception ignore) {}
                javafx.scene.Scene currentScene = stage.getScene();
                double w = currentScene != null ? currentScene.getWidth() : 1000;
                double h = currentScene != null ? currentScene.getHeight() : 700;
                stage.setScene(new javafx.scene.Scene(root, w, h));
                stage.setTitle("Sigma Cine");
                stage.setMaximized(true);
            }
        } catch (Exception ignore) {}
    }
}