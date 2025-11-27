package sigmacine.ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.VerHistorialService;
import sigmacine.aplicacion.session.Session;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.Locale;
import java.util.Set;

public class ClienteController {

    // ========= BARRA SUPERIOR =========
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private MenuButton menuPerfil;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;
    @FXML private Label  lblUserName;
    @FXML private TextField txtBuscar;
    @FXML private StackPane content;
    @FXML private ChoiceBox<String> cbCiudad;
    @FXML private Button btnSeleccionarCiudad;

    // ========= PUBLICIDAD (BANNERS) =========
    @FXML private StackPane promoPane;
    @FXML private ImageView promoImg;
    @FXML private HBox dotsBox;

    // ========= CARRUSEL PELÍCULAS =========
    @FXML private StackPane peliculasPane;
    @FXML private ScrollPane peliculasScroll;
    @FXML private HBox peliculasBox;
    @FXML private Button btnPrevMovies;
    @FXML private Button btnNextMovies;

    // ========= CARTELERA COMPLETA (GRID) =========
    @FXML private FlowPane gridPeliculas;

    private Timeline moviesAuto;
    private double stepFraction = 0.0;

    private static final int CARDS_PER_VIEW = 4;
    private static final double CARD_W = 156;
    private static final double SPACING = 12;
    private static final double HBOX_PADDING_X = 32;

    // ========= ESTADO GENERAL =========
    private final List<String> banners = List.of(
            "banner1.png",
            "banner2.png",
            "banner3.png",
            "banner4.png",
            "banner5.png"
    );
    private final List<Image> cache = new ArrayList<>();
    private int bannerIdx = 0;
    private Timeline promoAuto;

    private UsuarioDTO usuario;
    private String ciudadSeleccionada;
    private ControladorControlador coordinador;
    private boolean postersRequested = false;

    // ========= SET COORDINADOR =========
    public void setCoordinador(ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }

    // ========= INIT USUARIO / CIUDAD (para ControladorControlador) =========
    public void init(UsuarioDTO usuario) {
        this.usuario = usuario;
        postersRequested = false;

        String ciudad = Session.getSelectedCity();
        if (ciudad != null && !ciudad.isBlank()) {
            this.ciudadSeleccionada = ciudad;
        }

        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> cargarPeliculasInicio());
        pause.play();

        refreshSessionUI();
    }

    public void init(UsuarioDTO usuario, String ciudad) {
        this.usuario = usuario;
        this.ciudadSeleccionada = ciudad;
        Session.setSelectedCity(ciudad);
        postersRequested = false;

        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> cargarPeliculasInicio());
        pause.play();

        refreshSessionUI();
    }

    public void initCiudad(UsuarioDTO usuario) {
        this.usuario = usuario;
        if (cbCiudad != null) {
            cbCiudad.getItems().setAll("Bogotá", "Medellín", "Cali", "Barranquilla");
            cbCiudad.getSelectionModel().selectFirst();
        }
        refreshSessionUI();
    }

    // ========= INITIALIZE FXML =========
    @FXML
    private void initialize() {

        // Carrusel de banners
        initCarrusel();

        // Barra global
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("inicio");
        }

        // Ciudad
        if (btnSeleccionarCiudad != null) {
            btnSeleccionarCiudad.setOnAction(e -> onSeleccionarCiudad());
        }

        // Navegación top
        if (btnCartelera != null) btnCartelera.setOnAction(e -> mostrarCartelera());
        if (btnConfiteria != null) {
            btnConfiteria.setOnAction(e -> {
                try {
                    if (coordinador != null) {
                        coordinador.mostrarConfiteria();
                        return;
                    }
                    URL url = getClass().getResource("/sigmacine/ui/views/menu.fxml");
                    if (url == null) {
                        if (content != null) {
                            content.getChildren().setAll(new Label("Error: No se encontró menu.fxml"));
                        }
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
        }
        if (btnSigmaCard != null) btnSigmaCard.setOnAction(e -> onSigmaCardTop());
        if (btnCart != null)      btnCart.setOnAction(e -> toggleCarritoOverlay());

        // Menú perfil
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> onLogout());
        if (miHistorial    != null) miHistorial.setOnAction(e -> onVerHistorial());

        // Login / registro
        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        if (btnRegistrarse  != null) {
            btnRegistrarse.setOnAction(e -> onRegistrarse());
            btnRegistrarse.setDisable(Session.isLoggedIn());
        }

        // Buscador
        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    doSearch(txtBuscar.getText());
                }
            });
        }

        // Mostrar/ocultar publicidad según content
        if (content != null) {
            content.getChildren().addListener(
                    (ListChangeListener<Node>) c -> updatePublicidadVisibility()
            );
        }

        updateMenuPerfilState();
        refreshSessionUI();

        // Cargar carrusel de películas cuando la escena esté lista
        esperarYCargarPeliculas();
    }

    // ========= CARRUSEL DE BANNERS =========
    private void initCarrusel() {
        if (promoPane == null || promoImg == null || dotsBox == null) return;

        // 🔴 CAMBIO CLAVE: que el banner se ESTIRE de lado a lado
        promoImg.setPreserveRatio(false);
        promoImg.setSmooth(true);

        promoImg.fitWidthProperty().bind(promoPane.widthProperty());
        promoImg.fitHeightProperty().bind(promoPane.heightProperty());

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(promoPane.widthProperty());
        clip.heightProperty().bind(promoPane.heightProperty());
        promoPane.setClip(clip);
        StackPane.setAlignment(promoImg, Pos.CENTER);

        cache.clear();
        for (String f : banners) cache.add(loadRes("/Images/" + f));
        updateSlide(0);
        renderDots();

        promoAuto = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> nextSlide())
        );
        promoAuto.setCycleCount(Timeline.INDEFINITE);
        promoAuto.play();

        promoPane.setOnMouseEntered(e -> promoAuto.pause());
        promoPane.setOnMouseExited(e -> promoAuto.play());
        promoPane.setFocusTraversable(true);
        promoPane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT  -> prevSlide();
                case RIGHT -> nextSlide();
            }
        });
    }

    private Image loadRes(String path) {
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("Recurso no encontrado: " + path);
            return new Image(is, 0, 0, true, true);
        } catch (Exception ex) {
            System.err.println("Error cargando " + path + ": " + ex.getMessage());
            var ph = getClass().getResourceAsStream("/Images/placeholder.jpg");
            if (ph != null) return new Image(ph, 0, 0, true, true);
            return new Image((java.io.InputStream) null);
        }
    }

    private void renderDots() {
        dotsBox.getChildren().clear();
        for (int i = 0; i < banners.size(); i++) {
            final int at = i;
            Button dot = new Button();
            dot.setFocusTraversable(false);
            dot.getStyleClass().add("promo-dot");
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setOnAction(e -> updateSlide(at));
            dotsBox.getChildren().add(dot);
        }
        syncDots();
    }

    private void syncDots() {
        var kids = dotsBox.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            Node n = kids.get(i);
            n.getStyleClass().removeAll(List.of("active", "inactive"));
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

    public void disposeCarrusel() {
        if (promoAuto != null) promoAuto.stop();
    }

    // ========= MOSTRAR / OCULTAR PUBLICIDAD =========
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

    // ========= CIUDAD =========
    private void onSeleccionarCiudad() {
        String ciudad = (cbCiudad != null) ? cbCiudad.getValue() : null;
        if (ciudad == null || ciudad.isBlank()) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml")
            );
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.setCoordinador(this.coordinador);
            controller.init(this.usuario, ciudad);
            Session.setSelectedCity(ciudad);

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

    // ========= RESOLVER IMAGEN PELÍCULA =========
    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String r = ref.trim();
            String lower = r.toLowerCase(Locale.ROOT);

            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(r, true);
            }

            URL res = getClass().getResource("/Images/" + r);
            if (res != null) return new Image(res.toExternalForm(), false);

            int slash = Math.max(r.lastIndexOf('/'), r.lastIndexOf('\\'));
            String fileName = (slash >= 0) ? r.substring(slash + 1) : r;
            res = getClass().getResource("/Images/" + fileName);
            if (res != null) return new Image(res.toExternalForm(), false);

            res = getClass().getResource(r.startsWith("/") ? r : ("/" + r));
            if (res != null) return new Image(res.toExternalForm(), false);

            File f = new File(r);
            if (f.exists()) return new Image(f.toURI().toString(), false);
        } catch (Exception ignored) {}
        return null;
    }

    // ========= LOGIN / REGISTRO =========
    private void onIniciarSesion() {
        if (Session.isLoggedIn()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesión");
            a.setHeaderText("¿Desea cerrar sesión?");
            a.setContentText("Salir de la cuenta " +
                    (Session.getCurrent() != null ? Session.getCurrent().getEmail() : ""));
            var opt = a.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) onLogout();
            return;
        }
        if (coordinador != null) coordinador.mostrarLogin();
    }

    private void onRegistrarse() {
        if (Session.isLoggedIn()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Cierra sesión si deseas registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        if (coordinador != null) coordinador.mostrarRegistro();
    }

    private void onLogout() {
        Session.clear();
        this.usuario = null;

        if (lblUserName != null) {
            lblUserName.setText("");
            lblUserName.setVisible(false);
        }
        if (btnIniciarSesion != null) {
            btnIniciarSesion.setVisible(true);
            btnIniciarSesion.setText("Iniciar sesión");
        }
        if (btnRegistrarse != null) btnRegistrarse.setDisable(false);

        updateMenuPerfilState();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml")
            );
            Parent root = loader.load();
            ClienteController ctrl = loader.getController();
            ctrl.setCoordinador(this.coordinador);
            ctrl.init(null);

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            Scene current = stage.getScene();
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
            UsuarioDTO u = Session.getCurrent();
            String label = "";
            if (u != null) {
                if (u.getNombre() != null && !u.getNombre().isBlank()) {
                    label = u.getNombre();
                } else if (u.getEmail() != null) {
                    String e = u.getEmail();
                    int at = e.indexOf('@');
                    label = at > 0 ? e.substring(0, at) : e;
                }
            }
            if (lblUserName != null) {
                lblUserName.setText(label);
                lblUserName.setVisible(true);
            }
            if (btnIniciarSesion != null) btnIniciarSesion.setVisible(false);
            if (btnRegistrarse != null)  btnRegistrarse.setDisable(true);
        } else {
            if (lblUserName != null) {
                lblUserName.setText("");
                lblUserName.setVisible(false);
            }
            if (btnIniciarSesion != null) {
                btnIniciarSesion.setVisible(true);
                btnIniciarSesion.setText("Iniciar sesión");
            }
            if (btnRegistrarse != null) btnRegistrarse.setDisable(false);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ========= SIGMACARD =========
    @FXML
    private void onSigmaCardTop() {
        try {
            Stage stage = null;
            try {
                if (content != null && content.getScene() != null) {
                    stage = (Stage) content.getScene().getWindow();
                } else if (btnCartelera != null && btnCartelera.getScene() != null) {
                    stage = (Stage) btnCartelera.getScene().getWindow();
                }
            } catch (Exception ignore) {}
            if (stage != null) {
                SigmaCardController.openAsScene(stage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ========= CARRITO =========
    private void toggleCarritoOverlay() {
        System.out.println("Carrito: aquí iría el overlay del carrito");
    }

    // ========= HISTORIAL =========
    @FXML
    private void onVerHistorial() {
        if (!Session.isLoggedIn()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Acceso denegado");
            a.setHeaderText(null);
            a.setContentText("Debes iniciar sesión para ver tu historial de compras.");
            a.showAndWait();
            return;
        }
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            UsuarioRepositoryJdbc usuarioRepo = new UsuarioRepositoryJdbc(dbConfig);
            VerHistorialService historialService = new VerHistorialService(usuarioRepo);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sigmacine/ui/views/verCompras.fxml")
            );
            VerHistorialController historialController = new VerHistorialController(historialService);

            if (this.usuario != null) {
                historialController.setUsuarioEmail(this.usuario.getEmail());
            } else {
                UsuarioDTO current = Session.getCurrent();
                if (current != null && current.getEmail() != null && !current.getEmail().isBlank()) {
                    historialController.setUsuarioEmail(current.getEmail());
                }
            }

            loader.setControllerFactory(cls -> {
                if (cls == VerHistorialController.class) return historialController;
                try {
                    return cls.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            Scene prev = null;
            try {
                prev = (content != null && content.getScene() != null)
                        ? content.getScene()
                        : (btnCartelera != null ? btnCartelera.getScene() : null);
            } catch (Exception ignore) {}
            try {
                historialController.setPreviousScene(prev);
            } catch (Exception ignore) {}

            Parent historialView = loader.load();
            Stage stage = (Stage) (content != null && content.getScene() != null
                    ? content.getScene().getWindow()
                    : btnCartelera.getScene().getWindow());

            if (stage != null) {
                Scene current = stage.getScene();
                double w = current != null ? current.getWidth() : 900;
                double h = current != null ? current.getHeight() : 600;
                stage.setScene(new Scene(historialView, w > 0 ? w : 900, h > 0 ? h : 600));
                stage.setTitle("Historial de compras");
                stage.setMaximized(true);
            } else if (content != null) {
                content.getChildren().setAll(historialView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Label lab = new Label("Error cargando Historial: " + ex);
            lab.setStyle("-fx-text-fill: #fff;");
            TextArea ta = new TextArea();
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            ta.setText(sw.toString());
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(10);
            ta.setStyle("-fx-control-inner-background: #111; -fx-text-fill: #fff;");
            VBox box = new VBox(8, lab, ta);
            box.setStyle("-fx-padding: 12;");
            if (content != null) content.getChildren().setAll(box);
        }
    }

    // ========= MOSTRAR CARTELERA =========
    public void mostrarCartelera() {
        try {
            URL url = getClass().getResource("/sigmacine/ui/views/cartelera.fxml");
            if (url == null) {
                if (content != null)
                    content.getChildren().setAll(new Label("Error: No se encontró cartelera.fxml"));
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent carteleraView = loader.load();

            Object ctrl = loader.getController();
            if (ctrl != null) {
                try {
                    var m = ctrl.getClass().getMethod("setUsuario", UsuarioDTO.class);
                    if (m != null) m.invoke(ctrl, this.usuario);
                } catch (NoSuchMethodException ignore) {}
                try {
                    var m2 = ctrl.getClass().getMethod("setCoordinador", ControladorControlador.class);
                    if (m2 != null) m2.invoke(ctrl, this.coordinador);
                } catch (NoSuchMethodException ignore) {}
                try {
                    var rf = ctrl.getClass().getMethod("refreshSessionUI");
                    if (rf != null) rf.invoke(ctrl);
                } catch (NoSuchMethodException ignore) {}
            }

            Stage stage = (Stage) content.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(carteleraView, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            if (content != null)
                content.getChildren().setAll(new Label("Error: No se pudo cargar la vista de cartelera."));
        }
    }

    // ========= CARGAR PELÍCULAS (CAROUSEL + GRID) =========
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
        new Thread(() -> {
            try {
                Thread.sleep(800);
                Platform.runLater(this::cargarPeliculasInicio);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void cargarPeliculasInicio() {
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> peliculas = repo.buscarPorTitulo("");

            if (peliculas == null || peliculas.isEmpty()) {
                if (peliculasBox != null) {
                    peliculasBox.getChildren().setAll(new Label("No hay películas para mostrar"));
                }
                if (gridPeliculas != null) {
                    gridPeliculas.getChildren().setAll(new Label("No hay películas para mostrar"));
                }
                return;
            }

            // carrusel arriba
            construirCarruselPeliculas(peliculas);
            // 🔴 NUEVO: grid de cartelera abajo
            construirGridCartelera(peliculas);

        } catch (Exception ex) {
            ex.printStackTrace();
            if (peliculasBox != null)
                peliculasBox.getChildren().setAll(new Label("Error cargando películas."));
            if (gridPeliculas != null)
                gridPeliculas.getChildren().setAll(new Label("Error cargando cartelera."));
        }
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

        peliculasScroll.viewportBoundsProperty()
                .addListener((o, ov, nv) -> applyCenteringRules());
        peliculasBox.widthProperty()
                .addListener((o, ov, nv) -> applyCenteringRules());

        Platform.runLater(() -> {
            double cap = CARDS_PER_VIEW * CARD_W +
                    (CARDS_PER_VIEW - 1) * SPACING + HBOX_PADDING_X;

            peliculasScroll.setFitToWidth(false);
            peliculasScroll.setPrefViewportWidth(cap);
            peliculasScroll.setPrefWidth(cap);
            peliculasScroll.setMaxWidth(cap);

            peliculasScroll.setHvalue(0.0);
            applyCenteringRules();
            startMoviesAutoplay(4.5);
        });
    }

    // 🔴 NUEVO: llenar la Cartelera (FlowPane)
    private void construirGridCartelera(List<Pelicula> peliculas) {
        if (gridPeliculas == null) return;

        gridPeliculas.getChildren().clear();

        for (Pelicula p : peliculas) {
            Node card = buildMovieCard(p);
            gridPeliculas.getChildren().add(card);
        }
    }

    private Node buildMovieCard(Pelicula p) {
        ImageView poster = new ImageView();
        poster.setPreserveRatio(true);
        poster.setSmooth(true);
        poster.setFitWidth(136);
        poster.setFitHeight(204);

        Image img = (p.getPosterUrl() != null && !p.getPosterUrl().isBlank())
                ? resolveImage(p.getPosterUrl())
                : null;
        if (img == null) {
            URL res = getClass().getResource("/Images/placeholder.jpg");
            if (res != null) img = new Image(res.toExternalForm(), false);
        }
        if (img != null) poster.setImage(img);

        Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin título");
        titulo.setWrapText(true);
        titulo.setMaxWidth(136);
        titulo.setAlignment(Pos.CENTER);
        titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");

        Button verMas = new Button("Ver más");
        verMas.setPrefWidth(84);
        verMas.setPrefHeight(28);
        verMas.setOnAction(e -> abrirDetallePelicula(p));
        verMas.setStyle(
                "-fx-background-color:#8A2F24;" +
                        "-fx-background-radius:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#fff;"
        );

        VBox card = new VBox(6, poster, titulo, verMas);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(156);
        card.setMinWidth(156);
        card.setMaxWidth(156);
        card.setStyle("-fx-padding:6 6 8 6;");
        card.setUserData(p);
        return card;
    }

    // ========= CARRUSEL: LÓGICA SCROLL =========
    @FXML
    private void onPrevMovies() {
        if (moviesAuto != null) {
            moviesAuto.stop();
            moviesAuto.playFromStart();
        }
        advanceMovies(-1);
    }

    @FXML
    private void onNextMovies() {
        if (moviesAuto != null) {
            moviesAuto.stop();
            moviesAuto.playFromStart();
        }
        advanceMovies(1);
    }

    private double computeStepFraction() {
        if (peliculasScroll == null || peliculasScroll.getContent() == null) return 0.2;
        double viewport = peliculasScroll.getViewportBounds().getWidth();
        double content  = peliculasScroll.getContent().getBoundsInLocal().getWidth();
        if (content <= viewport || viewport <= 1) return 0.0;

        double card = 0;
        if (!peliculasBox.getChildren().isEmpty()) {
            Node first = peliculasBox.getChildren().get(0);
            card = first.getBoundsInParent().getWidth() + peliculasBox.getSpacing();
        }
        if (card > 0) {
            double denom = (content - viewport);
            double frac  = card / denom;
            return Math.max(0.05, Math.min(0.5, frac));
        }

        double page = 0.8 * (viewport / content);
        return Math.max(0.05, Math.min(0.5, page));
    }

    private void advanceMovies(int dir) {
        if (peliculasScroll == null) return;
        stepFraction = computeStepFraction();
        if (stepFraction <= 0.0) return;

        double now = peliculasScroll.getHvalue();
        double target = Math.max(0.0, Math.min(1.0, now + dir * stepFraction));

        Timeline tl = new Timeline(
                new KeyFrame(
                        Duration.millis(280),
                        new javafx.animation.KeyValue(
                                peliculasScroll.hvalueProperty(),
                                target,
                                javafx.animation.Interpolator.EASE_BOTH
                        )
                )
        );
        tl.setOnFinished(ev -> updateMovieButtons());
        tl.play();
    }

    private void applyCenteringRules() {
        if (peliculasScroll == null || peliculasBox == null) return;

        double viewport = peliculasScroll.getViewportBounds().getWidth();
        double contentW = peliculasBox.getBoundsInLocal().getWidth();
        boolean overflow = contentW > viewport + 1;

        peliculasScroll.setFitToWidth(!overflow);
        peliculasBox.setAlignment(!overflow ? Pos.CENTER : Pos.CENTER_LEFT);

        if (!overflow) {
            if (btnPrevMovies != null) btnPrevMovies.setDisable(true);
            if (btnNextMovies != null) btnNextMovies.setDisable(true);
            if (moviesAuto != null) moviesAuto.stop();
        } else {
            updateMovieButtons();
            if (moviesAuto == null) startMoviesAutoplay(4.5);
            else moviesAuto.play();
        }

        stepFraction = computeStepFraction();
    }

    private void startMoviesAutoplay(double seconds) {
        if (peliculasScroll == null || peliculasBox == null) return;
        if (moviesAuto != null) moviesAuto.stop();

        stepFraction = computeStepFraction();
        if (stepFraction <= 0) stepFraction = 0.2;

        moviesAuto = new Timeline(
                new KeyFrame(Duration.seconds(seconds), e -> advanceMovies(1))
        );
        moviesAuto.setCycleCount(Timeline.INDEFINITE);
        moviesAuto.play();

        if (peliculasPane != null) {
            peliculasPane.setOnMouseEntered(ev -> moviesAuto.pause());
            peliculasPane.setOnMouseExited(ev  -> moviesAuto.play());
        }
    }

    private void updateMovieButtons() {
        if (peliculasScroll == null || btnPrevMovies == null || btnNextMovies == null) return;
        double h = peliculasScroll.getHvalue();
        btnPrevMovies.setDisable(h <= 0.0001);
        btnNextMovies.setDisable(h >= 0.9999);
    }

    // ========= DETALLE PELÍCULA =========
    @SuppressWarnings("unused")
    private void abrirDetallePelicula(Pelicula p) {
        if (p == null) return;
        try {
            URL url = getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml");
            if (url == null) throw new IllegalStateException("No se encontró verdetallepelicula.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent rootDetalle = loader.load();

            VerDetallePeliculaController ctrl = loader.getController();
            try { ctrl.setCoordinador(this.coordinador); } catch (Exception ignore) {}
            try { ctrl.setUsuario(this.usuario); }       catch (Exception ignore) {}
            ctrl.setPelicula(p);

            Stage stage = null;
            if (content != null && content.getScene() != null) {
                stage = (Stage) content.getScene().getWindow();
            } else if (promoPane != null && promoPane.getScene() != null) {
                stage = (Stage) promoPane.getScene().getWindow();
            }
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

    // ========= BÚSQUEDA SUPERIOR =========
    @FXML
    private void onBuscarTop() {
        doSearch(txtBuscar != null ? txtBuscar.getText() : "");
    }

    private void doSearch(String texto) {
        if (texto == null) texto = "";

        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml")
            );
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
            stage.setTitle("Resultados de búsqueda");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (content != null) {
                content.getChildren().setAll(
                        new Label("Error cargando resultados: " + ex.getMessage())
                );
            }
        }
    }

    // ========= INTEGRACIÓN CON CONTENIDO DETALLE (ASIENTOS) =========
    public void mostrarAsientos(String titulo, String hora,
                                Set<String> ocupados, Set<String> accesibles) {
        try {
            URL url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AsientosController ctrl = loader.getController();
            ctrl.setFuncion(titulo, hora, ocupados, accesibles, null);

            // intentar pasar poster
            try {
                DatabaseConfig db = new DatabaseConfig();
                PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
                List<Pelicula> res = repo.buscarPorTitulo(titulo);
                if (res != null && !res.isEmpty()) {
                    Pelicula p = res.get(0);
                    String posterRef = p.getPosterUrl();
                    if (posterRef != null && !posterRef.isBlank()) {
                        Image img = resolveImage(posterRef);
                        if (img != null) ctrl.setPoster(img);
                    }
                }
            } catch (Exception ignore) {}

            Stage stage = null;
            if (content != null && content.getScene() != null) {
                stage = (Stage) content.getScene().getWindow();
            } else if (promoPane != null && promoPane.getScene() != null) {
                stage = (Stage) promoPane.getScene().getWindow();
            }
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
