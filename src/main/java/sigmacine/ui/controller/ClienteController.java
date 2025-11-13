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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;




public class ClienteController {

    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private javafx.scene.control.MenuItem miCerrarSesion;
    @FXML private javafx.scene.control.MenuItem miHistorial;
    @FXML private javafx.scene.control.MenuButton menuPerfil;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;
    @FXML private Label lblUserName;
    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.layout.StackPane content;
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;
    //publicidad y promociones
    @FXML private javafx.scene.layout.StackPane promoPane;
    @FXML private javafx.scene.image.ImageView promoImg;
    @FXML private javafx.scene.layout.HBox dotsBox;
    @FXML private StackPane peliculasPane;
    @FXML private ScrollPane peliculasScroll;
    @FXML private HBox peliculasBox;
    @FXML private Button btnPrevMovies;
    @FXML private Button btnNextMovies;
    private Timeline moviesAuto;
    private double stepFraction = 0.0; 
    private static final int CARDS_PER_VIEW = 4;   // pon 3 si quieres 3 visibles
    private static final double CARD_W = 156;      // ancho de tarjeta (tu buildMovieCard)
    private static final double SPACING = 12;      // peliculasBox.setSpacing(12)
    private static final double HBOX_PADDING_X = 32; // padding horizontal total (16 + 16)

       
    @FXML private void onPrevMovies() {
    if (moviesAuto != null) { moviesAuto.stop(); moviesAuto.playFromStart(); }
    advanceMovies(-1);
}

@FXML private void onNextMovies() {
    if (moviesAuto != null) { moviesAuto.stop(); moviesAuto.playFromStart(); }
    advanceMovies(1);
}

       
private void toggleAutoplay(boolean on) {
    if (moviesAuto == null) return;
    if (on) moviesAuto.play(); else moviesAuto.pause();
}





    private final java.util.List<String> banners = java.util.List.of("banner_promo_1.png","banner_promo_2.png","banner_promo_3.png");
    private final java.util.List<javafx.scene.image.Image> cache = new java.util.ArrayList<>();
    private int bannerIdx = 0;
    private javafx.animation.Timeline promoAuto;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;
    private boolean postersRequested = false;
    // añade en la clase
    
    
    public void setCoordinador(ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }

     private double computeStepFraction() {
    if (peliculasScroll == null || peliculasScroll.getContent() == null) return 0.2;
    double viewport = peliculasScroll.getViewportBounds().getWidth();
    double content  = peliculasScroll.getContent().getBoundsInLocal().getWidth();
    if (content <= viewport || viewport <= 1) return 0.0; // si no hay overflow, no hay paso

    // intenta paginar por ancho de 1 tarjeta
    double card = 0;
    if (!peliculasBox.getChildren().isEmpty()) {
        Node first = peliculasBox.getChildren().get(0);
        card = first.getBoundsInParent().getWidth() + peliculasBox.getSpacing();
    }
    if (card > 0) {
        double denom = (content - viewport);           // rango desplazable real
        double frac  = card / denom;                   // cuánto es una tarjeta en hvalue
        return Math.max(0.05, Math.min(0.5, frac));
    }

    // fallback: 80% del viewport
    double page = 0.8 * (viewport / content);
    return Math.max(0.05, Math.min(0.5, page));
}



        private void applyCenteringRules() {
    if (peliculasScroll == null || peliculasBox == null) return;

    double viewport = peliculasScroll.getViewportBounds().getWidth();
    double contentW = peliculasBox.getBoundsInLocal().getWidth();
    boolean overflow = contentW > viewport + 1;

    // Centra cuando cabe; alinea a la izquierda cuando no
    peliculasScroll.setFitToWidth(!overflow);
    peliculasBox.setAlignment(!overflow ? javafx.geometry.Pos.CENTER : javafx.geometry.Pos.CENTER_LEFT);

    // Flechas y autoplay
    if (!overflow) {
        if (btnPrevMovies != null) btnPrevMovies.setDisable(true);
        if (btnNextMovies != null) btnNextMovies.setDisable(true);
        if (moviesAuto != null) moviesAuto.stop();
    } else {
        updateMovieButtons();
        if (moviesAuto == null) startMoviesAutoplay(4.5); else moviesAuto.play();
    }

    stepFraction = computeStepFraction();
}


private void construirCarruselPeliculas(List<Pelicula> peliculas) {
    if (peliculasScroll == null || peliculasBox == null) return;

    peliculasScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    peliculasScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    peliculasScroll.setPannable(true);
    peliculasScroll.setFitToHeight(true);

    peliculasBox.getChildren().clear();
    peliculasBox.setSpacing(SPACING);
    for (Pelicula p : peliculas) {
        peliculasBox.getChildren().add(buildMovieCard(p));
    }

    // listeners que ya tenías
    peliculasScroll.viewportBoundsProperty().addListener((o, ov, nv) -> applyCenteringRules());
    peliculasBox.widthProperty().addListener((o, ov, nv) -> applyCenteringRules());

    // === AQUI va el bloque del viewport/cap ===
    Platform.runLater(() -> {
        double cap = CARDS_PER_VIEW * CARD_W + (CARDS_PER_VIEW - 1) * SPACING + HBOX_PADDING_X;

        peliculasScroll.setFitToWidth(false);      // permitir overflow horizontal
        peliculasScroll.setPrefViewportWidth(cap); // << clave
        // opcional: evita que el ScrollPane se expanda más que el viewport
        peliculasScroll.setPrefWidth(cap);
        peliculasScroll.setMaxWidth(cap);

        peliculasScroll.setHvalue(0.0);
        applyCenteringRules();                      // centra o habilita scroll

        // autoplay (solo se activará si hay overflow, por applyCenteringRules)
        startMoviesAutoplay(4.5);
    });
}
    
private void updateMovieButtons() {
    if (peliculasScroll == null || btnPrevMovies == null || btnNextMovies == null) return;
    double h = peliculasScroll.getHvalue();
    btnPrevMovies.setDisable(h <= 0.0001);
    btnNextMovies.setDisable(h >= 0.9999);
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
            if (postersRequested) return;
            postersRequested = true;

            StackPane anchor = (peliculasPane != null) ? peliculasPane : promoPane;

            if (anchor != null && anchor.getScene() != null) {
                Platform.runLater(this::cargarPeliculasInicio);
                return;
            }
            if (anchor != null) {
                anchor.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) Platform.runLater(this::cargarPeliculasInicio);
                });
                return;
            }
            // Último recurso
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    Platform.runLater(this::cargarPeliculasInicio);
                } catch (InterruptedException ignored) {}
            }).start();
        }

// === MOVIES CAROUSEL CORE ===
private void advanceMovies(int dir) {
    if (peliculasScroll == null) return;
    stepFraction = computeStepFraction();
    if (stepFraction <= 0.0) return; // sin overflow, nada que mover

    double now = peliculasScroll.getHvalue();
    double target = Math.max(0.0, Math.min(1.0, now + dir * stepFraction));

    Timeline tl = new Timeline(
        new KeyFrame(Duration.millis(280),
            new javafx.animation.KeyValue(
                peliculasScroll.hvalueProperty(), target,
                javafx.animation.Interpolator.EASE_BOTH
            )
        )
    );
    tl.setOnFinished(ev -> updateMovieButtons());
    tl.play();
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
         initCarrusel();
        // Solo mantener el Singleton para marcar la página activa
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("inicio");
        }
    
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        if (btnCartelera != null) btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> {
            try {
                // Prefer using the central coordinator to show the Confitería/menu as a full view (keeps top bar)
                if (this.coordinador != null) {
                    this.coordinador.mostrarConfiteria();
                    return;
                }
                // Fallback: if no coordinator available, try to load menu.fxml into content
                java.net.URL url = getClass().getResource("/sigmacine/ui/views/menu.fxml");
                if (url == null) {
                    if (content != null) content.getChildren().setAll(new Label("Error: No se encontró menu.fxml"));
                    return;
                }
                FXMLLoader loader = new FXMLLoader(url);
                Parent menuView = loader.load();

                if (content != null) {
                    content.getChildren().setAll(menuView);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        if (btnSigmaCard != null) btnSigmaCard.setOnAction(e -> onSigmaCardTop());
        if (btnCart != null) btnCart.setOnAction(e -> toggleCarritoOverlay());
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial());
        
        updateMenuPerfilState();

        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        if (btnRegistrarse  != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
            btnRegistrarse.setDisable(Session.isLoggedIn());
        }

        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
        }

        if (content != null) {
            content.getChildren().addListener((ListChangeListener<Node>) (c -> updatePublicidadVisibility()));
        }
    }
    // ===== Carrusel (métodos) =====
        private void initCarrusel() {
            if (promoPane == null || promoImg == null || dotsBox == null) return;

            // --- Ajuste para banner FULL-WIDTH ---
            promoImg.setPreserveRatio(true);
            promoImg.setSmooth(true);

            // Nos aseguramos de no tener bindings previos
            promoImg.fitHeightProperty().unbind();
            promoImg.fitWidthProperty().unbind();

            // Escalar por ancho del contenedor (ocupa todo el ancho)
            promoImg.fitWidthProperty().bind(promoPane.widthProperty());

            // Recorte por alto (para que no “desborde” verticalmente)
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(promoPane.widthProperty());
            clip.heightProperty().bind(promoPane.heightProperty());
            promoPane.setClip(clip);

            // Mantener centrado
            javafx.scene.layout.StackPane.setAlignment(promoImg, javafx.geometry.Pos.CENTER);

            // --- Precarga de imágenes ---
            cache.clear();
            for (String f : banners) cache.add(loadRes("/Images/" + f));
            updateSlide(0);

            // --- Dots ---
            renderDots();

            // --- Autoplay ---
            promoAuto = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> nextSlide())
            );
            promoAuto.setCycleCount(javafx.animation.Timeline.INDEFINITE);
            promoAuto.play();

            // Pausa con hover y navegación con teclado
            promoPane.setOnMouseEntered(e -> promoAuto.pause());
            promoPane.setOnMouseExited(e -> promoAuto.play());
            promoPane.setFocusTraversable(true);
            promoPane.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case LEFT -> prevSlide();
                    case RIGHT -> nextSlide();
                }
            });
        }

        private javafx.scene.image.Image loadRes(String path) {
            try (var is = getClass().getResourceAsStream(path)) {
                if (is == null) throw new IllegalArgumentException("Recurso no encontrado: " + path);
                // Firma válida: (InputStream, requestedWidth, requestedHeight, preserveRatio, smooth)
                return new javafx.scene.image.Image(is, 0, 0, true, true);
            } catch (Exception ex) {
                System.err.println("Error cargando " + path + ": " + ex.getMessage());
                // Fallback: placeholder
                var ph = getClass().getResourceAsStream("/Images/placeholder.jpg");
                if (ph != null) return new javafx.scene.image.Image(ph, 0, 0, true, true);
                // Último recurso: imagen vacía para no romper
                return new javafx.scene.image.Image((java.io.InputStream) null);
            }
        }


        private void renderDots() {
            dotsBox.getChildren().clear();
            for (int i = 0; i < banners.size(); i++) {
                final int at = i;
                var dot = new javafx.scene.control.Button();
                dot.setFocusTraversable(false);
                dot.getStyleClass().add("promo-dot");
                dot.setMinSize(10,10);
                dot.setMaxSize(10,10);
                dot.setOnAction(e -> updateSlide(at));
                dotsBox.getChildren().add(dot);
            }
            syncDots();
        }

        private void syncDots() {
            var kids = dotsBox.getChildren();
            for (int i = 0; i < kids.size(); i++) {
                var n = kids.get(i);
                n.getStyleClass().removeAll(java.util.List.of("active","inactive"));
                n.getStyleClass().add(i == bannerIdx ? "active" : "inactive");
            }
        }

        private void updateSlide(int newIndex) {
            if (cache.isEmpty()) return;
            bannerIdx = ((newIndex % cache.size()) + cache.size()) % cache.size();
            promoImg.setImage(cache.get(bannerIdx));
            syncDots();
        }

        @FXML public void nextSlide() { updateSlide(bannerIdx + 1); }
        @FXML public void prevSlide() { updateSlide(bannerIdx - 1); }

        public void disposeCarrusel() { if (promoAuto != null) promoAuto.stop(); }


        private void updatePublicidadVisibility() {
            try {
                boolean hasContent = content != null && !content.getChildren().isEmpty();
                boolean show = !hasContent;

                if (promoPane != null) {
                    promoPane.setVisible(show);
                    promoPane.setManaged(show);
                }
                if (peliculasPane != null) {
                    peliculasPane.setVisible(show);
                    peliculasPane.setManaged(show);
                }

                if (show) {
                    Platform.runLater(() -> {
                        try { cargarPeliculasInicio(); } catch (Exception ignore) {}
                    });
                }

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
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
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

    private void onIniciarSesion() {
        
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesi\u00F3n");
            a.setHeaderText("\u00BFDesea cerrar sesi\u00F3n?");
            a.setContentText("Salir de la cuenta " + (Session.getCurrent() != null ? Session.getCurrent().getEmail() : ""));
            var opt = a.showAndWait();
            if (opt.isPresent() && opt.get() == javafx.scene.control.ButtonType.OK) onLogout();
            return;
        }
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        
        if (Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesi\u00F3n");
            a.setHeaderText(null);
            a.setContentText("Cierra sesi\u00F3n si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }

    @SuppressWarnings("unused")
    private String safeCiudad() { return ciudadSeleccionada != null ? ciudadSeleccionada : "sin ciudad"; }

    private void onLogout() {
        
        Session.clear();
        this.usuario = null;
        if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
        if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi\u00F3n"); }
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
        updateMenuPerfilState();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            ClienteController ctrl = loader.getController();
            ctrl.init(null);
            ctrl.setCoordinador(this.coordinador);

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cliente");
            stage.setMaximized(true);
        } catch (Exception ex) {
            System.err.println("No se pudo navegar a pagina_inicial tras cerrar sesión: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshSessionUI() {
        boolean logged = Session.isLoggedIn();
        if (logged) {
            var u = Session.getCurrent();
            String label = "";
            if (u != null) {
                if (u.getNombre() != null && !u.getNombre().isBlank()) label = u.getNombre();
                else if (u.getEmail() != null) {
                    String e = u.getEmail(); int at = e.indexOf('@'); label = at > 0 ? e.substring(0, at) : e;
                }
            }
            if (lblUserName != null) { lblUserName.setText(label); lblUserName.setVisible(true); }
            if (btnIniciarSesion != null) btnIniciarSesion.setVisible(false);
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(true);
        } else {
            if (lblUserName != null) { lblUserName.setText(""); lblUserName.setVisible(false); }
            if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(true); btnIniciarSesion.setText("Iniciar sesi\u00F3n"); }
            if (btnRegistrarse  != null) btnRegistrarse.setDisable(false);
        }
        updateMenuPerfilState();
    }

    private void updateMenuPerfilState() {
        try {
            boolean logged = Session.isLoggedIn();
            if (menuPerfil != null) {
                menuPerfil.setDisable(!logged);
                menuPerfil.setVisible(logged);
                menuPerfil.setManaged(logged);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onSigmaCardTop() {
        try {
            javafx.stage.Stage stage = null;
            try { stage = content != null && content.getScene() != null ? (javafx.stage.Stage) content.getScene().getWindow() : (btnCartelera != null && btnCartelera.getScene() != null ? (javafx.stage.Stage) btnCartelera.getScene().getWindow() : null); } catch (Exception ignore) {}
            if (stage != null) SigmaCardController.openAsScene(stage);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void toggleCarritoOverlay() {
        // Implementación del carrito
    }

    @FXML
    private void onVerHistorial() {
        
        if (!Session.isLoggedIn()) {
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesi\u00F3n para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            var usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            var historialService = new VerHistorialService(usuarioRepo);
            
            // Carga el FXML (ruta verificada y correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            VerHistorialController historialController = new VerHistorialController(historialService);

            if (this.usuario != null) {
                historialController.setUsuarioEmail(this.usuario.getEmail());
            } else {
                var current = Session.getCurrent();
                if (current != null && current.getEmail() != null && !current.getEmail().isBlank()) {
                    historialController.setUsuarioEmail(current.getEmail());
                }
            }

            loader.setControllerFactory(cls -> {
                if (cls == sigmacine.ui.controller.VerHistorialController.class) return historialController;
                try { return cls.getDeclaredConstructor().newInstance(); } catch (Exception ex) { throw new RuntimeException(ex); }
            });
            javafx.scene.Scene prev = null;
            try { prev = (content != null && content.getScene() != null) ? content.getScene() : (btnCartelera != null ? btnCartelera.getScene() : null); } catch (Exception ignore) {}
            try { historialController.setPreviousScene(prev); } catch (Exception ignore) {}
            Parent historialView = loader.load();
            Stage stage = (Stage) (content != null && content.getScene() != null ? content.getScene().getWindow() : btnCartelera.getScene().getWindow());
            if (stage != null) {
                Scene current = stage.getScene();
                double w = current != null ? current.getWidth() : 900;
                double h = current != null ? current.getHeight() : 600;
                stage.setScene(new Scene(historialView, w > 0 ? w : 900, h > 0 ? h : 600));
                stage.setTitle("Historial de compras");
                stage.setMaximized(true);
            } else {
                content.getChildren().setAll(historialView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = ex.toString();
            javafx.scene.control.Label lab = new javafx.scene.control.Label("Error cargando Historial: " + msg);
            lab.setStyle("-fx-text-fill: #fff;");
            javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea();
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            ta.setText(sw.toString());
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(10);
            ta.setStyle("-fx-control-inner-background: #111; -fx-text-fill: #fff;");
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(8, lab, ta);
            box.setStyle("-fx-padding: 12;");
            content.getChildren().setAll(box);
        }
    }

    public void mostrarCartelera() {
        try {
            java.net.URL url = getClass().getResource("/sigmacine/ui/views/cartelera.fxml");
            if (url == null) {
                content.getChildren().setAll(new Label("Error: No se encontró cartelera.fxml"));
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent carteleraView = loader.load();

            try {
                Object ctrl = loader.getController();
                if (ctrl != null) {
                    try {
                        var m = ctrl.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                        if (m != null) m.invoke(ctrl, this.usuario);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        var m2 = ctrl.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                        if (m2 != null) m2.invoke(ctrl, this.coordinador);
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        var rf = ctrl.getClass().getMethod("refreshSessionUI");
                        if (rf != null) rf.invoke(ctrl);
                    } catch (NoSuchMethodException ignore) {}
                }
            } catch (Exception ignore) {}

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(carteleraView, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de cartelera."));
        }
    }

    private void cargarPeliculasInicio() {
    try {
        DatabaseConfig db = new DatabaseConfig();
        PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
        List<Pelicula> peliculas = repo.buscarPorTitulo(""); // todas

        if (peliculas == null || peliculas.isEmpty()) {
            if (peliculasBox != null) {
                peliculasBox.getChildren().setAll(new Label("No hay películas para mostrar"));
            }
            return;
        }
        construirCarruselPeliculas(peliculas);
    } catch (Exception ex) {
        ex.printStackTrace();
        if (peliculasBox != null) peliculasBox.getChildren().setAll(new Label("Error cargando películas."));
       }
}
private Node buildMovieCard(Pelicula p) {
    ImageView poster = new ImageView();
    poster.setPreserveRatio(true);
    poster.setSmooth(true);
    poster.setFitWidth(136);          // más compacto
    poster.setFitHeight(204);         // mantiene proporción “poster”

    Image img = (p.getPosterUrl() != null && !p.getPosterUrl().isBlank())
            ? resolveImage(p.getPosterUrl()) : null;
    if (img == null) {
        java.net.URL res = getClass().getResource("/Images/placeholder.jpg");
        if (res != null) img = new Image(res.toExternalForm(), false);
    }
    if (img != null) poster.setImage(img);

    Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin título");
    titulo.setWrapText(true);
    titulo.setMaxWidth(136);
    titulo.setAlignment(javafx.geometry.Pos.CENTER);
    titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");

    Button verMas = new Button("Ver más");
    verMas.setPrefWidth(84);
    verMas.setPrefHeight(28);
    verMas.setOnAction(e -> abrirDetallePelicula(p));
    verMas.setStyle("-fx-background-color:#8A2F24; -fx-background-radius:12; -fx-font-weight:bold; -fx-text-fill:#fff;");

    VBox card = new VBox(6, poster, titulo, verMas);
    card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    card.setPrefWidth(156);
    card.setMinWidth(156);
    card.setMaxWidth(156);
    card.setStyle("-fx-padding:6 6 8 6;");
    card.setUserData(p);
    return card;
}


     
private void startMoviesAutoplay(double seconds) {
    if (peliculasScroll == null || peliculasBox == null) return;
    if (moviesAuto != null) moviesAuto.stop();

    stepFraction = computeStepFraction();
    if (stepFraction <= 0) stepFraction = 0.2;

    moviesAuto = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> advanceMovies(1)));
    moviesAuto.setCycleCount(Timeline.INDEFINITE);
    moviesAuto.play();

    if (peliculasPane != null) {
        peliculasPane.setOnMouseEntered(ev -> moviesAuto.pause());
        peliculasPane.setOnMouseExited(ev  -> moviesAuto.play());
    }
}



    @SuppressWarnings("unused")
    private void abrirDetallePelicula(Pelicula p) {
        if (p == null) return;
        try {
            var url = getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml");
            if (url == null) throw new IllegalStateException("No se encontró verdetallepelicula.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent rootDetalle = loader.load();

            VerDetallePeliculaController ctrl = loader.getController();
            try { ctrl.setCoordinador(this.coordinador); } catch (Exception ignore) {}
            try { ctrl.setUsuario(this.usuario); } catch (Exception ignore) {}
            ctrl.setPelicula(p);

            Stage stage = null;
            if (content != null && content.getScene() != null) stage = (Stage) content.getScene().getWindow();
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

            // Try to resolve poster for the given title and pass it to the AsientosController so the
            // poster appears in the right-hand panel. We search the Pelicula repository by title
            // and use the first match if available.
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