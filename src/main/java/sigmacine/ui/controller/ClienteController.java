package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.session.Session;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;

import java.util.List;
import java.util.Set;
import java.util.Locale;

public class ClienteController {

    @FXML private Button btnPromoVerMas;
    @FXML private javafx.scene.layout.StackPane promoPane;
    @FXML private ImageView imgPublicidad;
    @FXML private Label lblPromo;
    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.layout.StackPane content;
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    @FXML private javafx.scene.layout.GridPane footerGrid;
    @FXML private ImageView imgCard1;
    @FXML private Label lblCard1;
    @FXML private ImageView imgCard2;
    @FXML private Label lblCard2;
    @FXML private ImageView imgCard3;
    @FXML private Label lblCard3;
    @FXML private ImageView imgCard4;
    @FXML private Label lblCard4;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;
    private boolean postersRequested = false;
    
    public void setCoordinador(ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }

    public void init(UsuarioDTO usuario) { 
        this.usuario = usuario;
        
        // Usar la ciudad de la sesión si está disponible
        String ciudad = sigmacine.aplicacion.session.Session.getSelectedCity();
        if (ciudad != null && !ciudad.isEmpty()) {
            this.ciudadSeleccionada = ciudad;
        }
        
        esperarYCargarPeliculas();
    }
    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        sigmacine.aplicacion.session.Session.setSelectedCity(ciudad);
        esperarYCargarPeliculas();
    }
    
    private void esperarYCargarPeliculas() {
        if (postersRequested) {
            return;
        }
        postersRequested = true;
        if (promoPane != null && promoPane.getScene() != null) {
            Platform.runLater(() -> cargarPeliculasInicio());
        } else if (promoPane != null) {
            promoPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> cargarPeliculasInicio());
                }
            });
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Esperar 1 segundo
                    Platform.runLater(() -> cargarPeliculasInicio());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogot\u00E1", "Medell\u00EDn", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void initialize() {
        // Solo mantener el Singleton para marcar la página activa
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("inicio");
        }
        
        if (promoPane != null && imgPublicidad != null) {
            imgPublicidad.fitWidthProperty().bind(promoPane.widthProperty());
            imgPublicidad.setFitHeight(110);
            imgPublicidad.setPreserveRatio(true);
        }
        
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
        }

        if (content != null) {
            content.getChildren().addListener((ListChangeListener<Node>) (c -> updatePublicidadVisibility()));
        }
    }

    private void updatePublicidadVisibility() {
        try {
            boolean hasContent = content != null && !content.getChildren().isEmpty();
            boolean show = !hasContent;
            if (imgPublicidad != null) {
                imgPublicidad.setVisible(show);
                imgPublicidad.setManaged(show);
            }
            if (promoPane != null) {
                promoPane.setVisible(show);
                promoPane.setManaged(show);
            }
            try {
                if (footerGrid == null) {
                    footerGrid = localizarFooterGridDesdeRoot();
                }
                if (footerGrid != null) {
                    footerGrid.setVisible(show);
                    footerGrid.setManaged(show);
                    if (!show) {
                        try {
                            for (javafx.scene.Node n : footerGrid.getChildren()) {
                                if (n instanceof ImageView) {
                                    ((ImageView) n).setImage(null);
                                }
                            }
                        } catch (Exception ignore) {}
                    } else {
                        Platform.runLater(() -> {
                            try { cargarPeliculasInicio(); } catch (Exception ignore) {}
                        });
                    }
                }
            } catch (Exception ignore) {}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

    private void doSearch(String texto) {
        if (texto == null) texto = "";
        
        try {
            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Resultados de b\u00FAsqueda");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            content.getChildren().setAll(new Label("Error cargando resultados: " + ex.getMessage()));
        }
    }

    @FXML private void onPromoVerMas() { }
    @FXML private void onCard1(){ }
    @FXML private void onCard2(){ }
    @FXML private void onCard3(){ }
    @FXML private void onCard4(){ }

    private void onSeleccionarCiudad() {
        String ciudad= (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.init(this.usuario, ciudad);
            sigmacine.aplicacion.session.Session.setSelectedCity(ciudad);

            Stage stage = (Stage) btnSeleccionarCiudad.getScene().getWindow();
            stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.show();
            stage.setMaximized(true);
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando pagina_inicial.fxml", ex);
        }
    }

    private ImageView buscarImageView(String fxId) {
        try {
            if (promoPane != null && promoPane.getScene() != null) {
                return (ImageView) promoPane.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (ImageView) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private Label buscarLabel(String fxId) {
        try {
            if (promoPane != null && promoPane.getScene() != null) {
                return (Label) promoPane.getScene().lookup(fxId);
            }
            if (content != null && content.getScene() != null) {
                return (Label) content.getScene().lookup(fxId);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private void cargarCard(ImageView img, Label lbl, Pelicula pelicula) {
        if (pelicula == null) return;
        if (lbl != null) lbl.setText(pelicula.getTitulo() != null ? pelicula.getTitulo() : "Sin t\u00EDtulo");
        if (img != null) {
            Image posterImage = null;
            if (pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isBlank()) {
                posterImage = resolveImage(pelicula.getPosterUrl());
            }
            if (posterImage == null) posterImage = resolveImage("placeholder.png");
            if (posterImage != null) {
                img.setImage(posterImage);
                img.setPreserveRatio(true);
                img.setFitWidth(220);
                img.setSmooth(true);
                img.setCache(true);
            }
        }
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String r = ref.trim();
            String lower = r.toLowerCase(Locale.ROOT);

            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(r, true);
            }

            int slash = Math.max(r.lastIndexOf('/'), r.lastIndexOf('\\'));
            String fileName = (slash >= 0) ? r.substring(slash + 1) : r;

            java.net.URL res = getClass().getResource("/Images/" + fileName);
            if (res != null) return new Image(res.toExternalForm(), false);

            res = getClass().getResource(r.startsWith("/") ? r : ("/" + r));
            if (res != null) return new Image(res.toExternalForm(), false);

            java.io.File f = new java.io.File(r);
            if (f.exists()) return new Image(f.toURI().toString(), false);
        } catch (Exception ignored) {}
        return null;
    }

    private void cargarPeliculasInicio() {
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> peliculas = repo.buscarPorTitulo(""); // buscar todas
            
            if (peliculas == null || peliculas.isEmpty()) {
                return;
            }
            if (footerGrid == null) {
                footerGrid = localizarFooterGridDesdeRoot();
            }
            if (footerGrid == null) {
                buscarYCargarConLookup(peliculas);
                return;
            }
            
            renderizarFooterDinamico(footerGrid, peliculas);
            return;
            
        } catch (Exception ex) {
            System.err.println("Error cargando películas para la página inicial: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private javafx.scene.layout.GridPane localizarFooterGridDesdeRoot() {
        try {
            javafx.scene.Scene sc = null;
            if (promoPane != null) sc = promoPane.getScene();
            if (sc == null && content != null) sc = content.getScene();
            if (sc == null && imgPublicidad != null) sc = imgPublicidad.getScene();
            if (sc == null && txtBuscar != null) sc = txtBuscar.getScene();
            if (sc == null) return null;

            javafx.scene.Parent root = sc.getRoot();
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane bp = (javafx.scene.layout.BorderPane) root;
                javafx.scene.Node bottom = bp.getBottom();
                if (bottom instanceof javafx.scene.layout.HBox) {
                    for (javafx.scene.Node ch : ((javafx.scene.layout.HBox) bottom).getChildren()) {
                        if (ch instanceof javafx.scene.layout.GridPane) {
                            return (javafx.scene.layout.GridPane) ch;
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    private void renderizarFooterDinamico(javafx.scene.layout.GridPane grid, List<Pelicula> peliculas) {
        try {
            if (grid == null) return;
            grid.getChildren().clear();

            if (grid.getColumnConstraints() != null) grid.getColumnConstraints().clear();
            if (grid.getRowConstraints() != null) grid.getRowConstraints().clear();

            grid.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            grid.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            grid.setPadding(new javafx.geometry.Insets(12, 64, 12, 64)); // margen y ligero padding superior

            grid.setHgap(64);
            grid.setVgap(16);
            grid.setAlignment(javafx.geometry.Pos.CENTER);
            grid.setTranslateY(-30);

            int max = Math.min(3, peliculas.size());
            int startCol = 0;

            for (int i = 0; i < max; i++) {
                Pelicula p = peliculas.get(i);
                int col = startCol + i;

                ImageView poster = new ImageView();
                poster.setPreserveRatio(true);
                poster.setFitWidth(220);
                String posterRef = p.getPosterUrl();
                if (posterRef != null && !posterRef.isBlank()) {
                    Image img = resolveImage(posterRef);
                    if (img != null) poster.setImage(img);
                }
                javafx.scene.layout.GridPane.setColumnIndex(poster, col);
                javafx.scene.layout.GridPane.setRowIndex(poster, 0);
                javafx.scene.layout.GridPane.setMargin(poster, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(poster);

                Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin título");
                titulo.setWrapText(true);
                titulo.setMaxWidth(220);
                titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
                titulo.setAlignment(javafx.geometry.Pos.CENTER);
                titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                javafx.scene.layout.GridPane.setColumnIndex(titulo, col);
                javafx.scene.layout.GridPane.setRowIndex(titulo, 1);
                javafx.scene.layout.GridPane.setHalignment(titulo, javafx.geometry.HPos.CENTER);
                javafx.scene.layout.GridPane.setMargin(titulo, new javafx.geometry.Insets(0, 0, 8, 0));
                grid.getChildren().add(titulo);

                Button verMas = new Button("Ver más");
                verMas.setStyle("-fx-background-color: #993726; -fx-background-radius: 14; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
                verMas.setPrefWidth(96);
                verMas.setPrefHeight(34);
                verMas.setOnAction(e -> abrirDetallePelicula(p));
                verMas.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml"));
                        Parent detailRoot = loader.load();
                        Object ctrl = loader.getController();
                        if (ctrl instanceof sigmacine.ui.controller.VerDetallePeliculaController) {
                            sigmacine.ui.controller.VerDetallePeliculaController detalle = (sigmacine.ui.controller.VerDetallePeliculaController) ctrl;
                            detalle.setPelicula(p);
                            detalle.setUsuario(this.usuario);
                            detalle.setCoordinador(this.coordinador);
                        }

                        Stage stage = (Stage) content.getScene().getWindow();
                        javafx.scene.Scene current = stage.getScene();
                        double w = current != null ? current.getWidth() : 900;
                        double h = current != null ? current.getHeight() : 600;

                        Parent currentRoot = current != null ? current.getRoot() : null;
                        if (currentRoot != null) {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), currentRoot);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(fe -> {
                                try {
                                    Scene newScene = new Scene(detailRoot, w > 0 ? w : 900, h > 0 ? h : 600);
                                    stage.setScene(newScene);
                                    stage.setTitle("Sigma Cine - Detalle película");
                                    stage.setMaximized(true);
                                    FadeTransition fadeIn = new FadeTransition(Duration.millis(220), newScene.getRoot());
                                    newScene.getRoot().setOpacity(0.0);
                                    fadeIn.setFromValue(0.0);
                                    fadeIn.setToValue(1.0);
                                    fadeIn.play();
                                } catch (Exception ex) { ex.printStackTrace(); }
                            });
                            fadeOut.play();
                        } else {
                            stage.setScene(new Scene(detailRoot, w > 0 ? w : 900, h > 0 ? h : 600));
                            stage.setTitle("Sigma Cine - Detalle película");
                            stage.setMaximized(true);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error abriendo detalle de película: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                javafx.scene.layout.GridPane.setColumnIndex(verMas, col);
                javafx.scene.layout.GridPane.setRowIndex(verMas, 2);
                javafx.scene.layout.GridPane.setHalignment(verMas, javafx.geometry.HPos.CENTER);
                grid.getChildren().add(verMas);
            }

            javafx.application.Platform.runLater(() -> {
                try { grid.applyCss(); grid.layout(); } catch (Exception ignore) {}
            });
        } catch (Exception ex) {
            System.err.println("Error renderizando footer dinámico: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void buscarYCargarConLookup(List<Pelicula> peliculas) {
        try {
            ImageView img1 = buscarImageView("#imgCard1");
            ImageView img2 = buscarImageView("#imgCard2");
            ImageView img3 = buscarImageView("#imgCard3");
            
            Label lbl1 = buscarLabel("#lblCard1");
            Label lbl2 = buscarLabel("#lblCard2");
            Label lbl3 = buscarLabel("#lblCard3");
            
            if (peliculas.size() > 0 && img1 != null) {
                cargarCard(img1, lbl1, peliculas.get(0));
            }
            if (peliculas.size() > 1 && img2 != null) {
                cargarCard(img2, lbl2, peliculas.get(1));
            }
            if (peliculas.size() > 2 && img3 != null) {
                cargarCard(img3, lbl3, peliculas.get(2));
            }
            
        } catch (Exception ex) {
            System.err.println("Error en buscarYCargarConLookup: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    private void abrirDetallePelicula(Pelicula p) {
        if (p == null) return;
        try {
            var url = getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml");
            if (url == null) throw new IllegalStateException("No se encontró contenidoCartelera.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent rootDetalle = loader.load();

            ContenidoCarteleraController ctrl = loader.getController();
            try { ctrl.setCoordinador(this.coordinador); } catch (Exception ignore) {}
            try { ctrl.setUsuario(this.usuario); } catch (Exception ignore) {}
            ctrl.setPelicula(p);

            Stage stage = null;
            if (content != null && content.getScene() != null) stage = (Stage) content.getScene().getWindow();
            else if (footerGrid != null && footerGrid.getScene() != null) stage = (Stage) footerGrid.getScene().getWindow();
            else if (promoPane != null && promoPane.getScene() != null) stage = (Stage) promoPane.getScene().getWindow();
            if (stage == null) return;

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(rootDetalle, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
            stage.setMaximized(true);
        } catch (Exception ex) {
            System.err.println("Error abriendo detalle: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void mostrarAsientos(String titulo, String hora, Set<String> ocupados, Set<String> accesibles) {
        try {
            var url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AsientosController ctrl = loader.getController();
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);
            try {
                if (titulo != null && !titulo.isBlank()) {
                    DatabaseConfig db = new DatabaseConfig();
                    sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
                    var resultados = repo.buscarPorTitulo(titulo);
                    if (resultados != null && !resultados.isEmpty()) {
                        var p = resultados.get(0);
                        String posterRef = p.getPosterUrl();
                        if (posterRef != null && !posterRef.isBlank()) {
                            try {
                                java.io.InputStream is = getClass().getResourceAsStream(posterRef.startsWith("/") ? posterRef : ("/" + posterRef));
                                javafx.scene.image.Image img = null;
                                if (is != null) img = new javafx.scene.image.Image(is);
                                else img = resolveImage(posterRef);
                                if (img != null) ctrl.setPoster(img);
                            } catch (Exception ignore) {}
                        }
                    }
                }
            } catch (Exception ignore) {}

            Stage stage = null;
            if (content != null && content.getScene() != null) stage = (Stage) content.getScene().getWindow();
            else if (promoPane != null && promoPane.getScene() != null) stage = (Stage) promoPane.getScene().getWindow();
            if (stage == null) return;

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1100;
            double h = current != null ? current.getHeight() : 620;
            stage.setScene(new Scene(root, w, h));
            stage.setMaximized(true);
            stage.setTitle("Selecciona tus asientos");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public boolean isSameScene(Button anyButton) {
    try {
        if (anyButton == null) return false;
        if (content != null && content.getScene() != null) {
            return anyButton.getScene() == content.getScene();
        }
        return false;
    } catch (Throwable t) {
        return false;
    }
    }
}