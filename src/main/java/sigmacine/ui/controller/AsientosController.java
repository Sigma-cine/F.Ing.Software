package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;

import java.net.URL;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AsientosController implements Initializable {

    @FXML private GridPane gridSala;
    @FXML private Label lblResumen;

    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private Button btnCart;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;
    @FXML private Label  lblUserName;
    @FXML private MenuButton menuPerfil;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;

    @FXML private Label lblTitulo;
    @FXML private Label lblHoraPill;
    @FXML private ImageView imgPoster;
    @FXML private Button btnContinuar;

    private int filas = 8;
    private int columnas = 12;

    private final Set<String> ocupados   = new HashSet<>();
    private final Set<String> accesibles = new HashSet<>();
    private final Set<String> seleccion  = new HashSet<>();
    private final Map<String, ToggleButton> seatByCode = new HashMap<>();

    private String titulo = "Película";
    private String hora   = "1:10 pm";
    private Image poster;
    private Long funcionId; // ID de la función seleccionada

    // --- Carrito ---
    private final sigmacine.aplicacion.service.CarritoService carrito = sigmacine.aplicacion.service.CarritoService.getInstance();
    private final List<sigmacine.aplicacion.data.CompraProductoDTO> asientoItems = new ArrayList<>();
    private static final BigDecimal PRECIO_ASIENTO = new BigDecimal("12.00"); // Precio base por asiento

    // Popup del carrito (implementación ligera reutilizando verCarrito.fxml)
    private Stage cartStage;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    public void setUsuario(UsuarioDTO u) { this.usuario = u; refreshSessionUI(); }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (btnCartelera != null) btnCartelera.setOnAction(e -> goCartelera());
        if (btnConfiteria != null) btnConfiteria.setOnAction(e -> {});
        if (btnCart != null) btnCart.setOnAction(e -> toggleCartPopup());
        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> {
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) return; // ya logueado
            if (coordinador != null) { coordinador.mostrarLogin(); refreshSessionUI(); }
            else onIniciarSesion();
        });
        if (btnRegistrarse != null) btnRegistrarse.setOnAction(e -> {
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) return; // no permitir si ya está logueado
            if (coordinador != null) coordinador.mostrarRegistro();
            else onRegistrarse();
        });
        if (btnRegistrarse != null) btnRegistrarse.setOnAction(e -> onRegistrarse());
        if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> { sigmacine.aplicacion.session.Session.clear(); refreshSessionUI(); });
        if (miHistorial != null) miHistorial.setOnAction(e -> onVerHistorial());
        if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> onIniciarSesion());
        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
            if (btnBuscar != null) btnBuscar.setOnAction(e -> doSearch(txtBuscar.getText()));
        }

        // Demo si nadie setea función
        if (ocupados.isEmpty()) {
            for (int c = 3; c <= columnas; c += 2) ocupados.add("D" + c);
            for (int c = 2; c <= columnas; c += 3) ocupados.add("E" + c);
            for (int c = 1; c <= columnas; c += 4) ocupados.add("F" + c);
        }
        if (accesibles.isEmpty()) {
            accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));
        }
        if (lblTitulo != null)   lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
        refreshSessionUI();
    }

    @FXML private void onBrandClick() { goHome(); }
    @FXML private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

    private void goHome() {
        try {
            Stage stage = (Stage) (gridSala != null ? gridSala.getScene().getWindow() : btnContinuar.getScene().getWindow());
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof ClienteController c) {
                c.setCoordinador(this.coordinador);
                c.init(this.usuario);
            }
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void goCartelera() {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/cartelera.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            try {
                var su = ctrl.getClass().getMethod("setUsuario", sigmacine.aplicacion.data.UsuarioDTO.class);
                if (su != null) su.invoke(ctrl, this.usuario);
            } catch (NoSuchMethodException ignore) {}
            try {
                var sc = ctrl.getClass().getMethod("setCoordinador", sigmacine.ui.controller.ControladorControlador.class);
                if (sc != null) sc.invoke(ctrl, this.coordinador);
            } catch (NoSuchMethodException ignore) {}
            try {
                var rf = ctrl.getClass().getMethod("refreshSessionUI");
                if (rf != null) rf.invoke(ctrl);
            } catch (NoSuchMethodException ignore) {}

            Stage stage = (Stage) gridSala.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Sigma Cine - Cartelera");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void onIniciarSesion() {
        if (sigmacine.aplicacion.session.Session.isLoggedIn()) return;
        if (coordinador != null) { coordinador.mostrarLogin(); refreshSessionUI(); return; }
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            Parent root = loader.load();
            var dialog = new javafx.stage.Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.initOwner(gridSala.getScene().getWindow());
            var ctrl = loader.getController();
            if (ctrl instanceof LoginController lc) {
                try { ControladorControlador global = ControladorControlador.getInstance(); if (global != null) { lc.setCoordinador(global); lc.setAuthFacade(global.getAuthFacade()); } } catch (Throwable ignore) {}
                lc.setOnSuccess(() -> { try { dialog.close(); } catch (Exception ignore) {} refreshSessionUI(); });
            }
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void onRegistrarse() {
        if (sigmacine.aplicacion.session.Session.isLoggedIn()) return;
        if (coordinador != null) { coordinador.mostrarRegistro(); return; }
    }

    private void onVerHistorial() {
        if (!sigmacine.aplicacion.session.Session.isLoggedIn()) return;
        try {
            var db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var usuarioRepo = new sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc(db);
            var service = new sigmacine.aplicacion.service.VerHistorialService(usuarioRepo);
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            var controller = new VerHistorialController(service);
            // permitir volver a la escena actual
            javafx.scene.Scene prev = null;
            try { prev = gridSala != null && gridSala.getScene() != null ? gridSala.getScene() : null; } catch (Exception ignore) {}
            try { controller.setPreviousScene(prev); } catch (Exception ignore) {}
            if (this.usuario != null) controller.setUsuarioEmail(this.usuario.getEmail());
            else {
                var cur = sigmacine.aplicacion.session.Session.getCurrent();
                if (cur != null && cur.getEmail() != null && !cur.getEmail().isBlank()) controller.setUsuarioEmail(cur.getEmail());
            }
            System.out.println("[AsientosController] opening historial view...");
            loader.setControllerFactory(cls -> {
                if (cls == sigmacine.ui.controller.VerHistorialController.class) return controller;
                try { return cls.getDeclaredConstructor().newInstance(); } catch (Exception ex) { throw new RuntimeException(ex); }
            });
            Parent root = loader.load();
            Stage stage = (Stage) gridSala.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Historial de compras");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void doSearch(String texto) {
        if (texto == null) texto = "";
        try {
            var db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            var controller = loader.getController();
            if (controller instanceof ResultadosBusquedaController rbc) {
                rbc.setCoordinador(this.coordinador);
                rbc.setUsuario(this.usuario);
                rbc.setResultados(resultados, texto);
            }
            Stage stage = (Stage) gridSala.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Resultados de búsqueda");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshSessionUI() {
        boolean logged = sigmacine.aplicacion.session.Session.isLoggedIn();
        var u = sigmacine.aplicacion.session.Session.getCurrent();
        if (lblUserName != null) { lblUserName.setVisible(logged); lblUserName.setManaged(logged); lblUserName.setText(logged && u != null ? (u.getNombre() != null ? u.getNombre() : "") : ""); }
        if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(!logged); btnIniciarSesion.setManaged(!logged); }
        if (btnRegistrarse  != null) btnRegistrarse.setDisable(logged);
        if (menuPerfil != null) { menuPerfil.setVisible(logged); menuPerfil.setManaged(logged); }
    }

    private void poblarGrilla() {
        gridSala.getChildren().clear();
        seatByCode.clear();
        seleccion.clear();

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                String code = code(f, c);

                ToggleButton seat = new ToggleButton();
                seat.getStyleClass().add("seat");
                seat.setUserData(code);
                seat.setFocusTraversable(false);
                seat.setTooltip(new Tooltip(code));

                seat.getProperties().put("accessible", accesibles.contains(code));

                if (ocupados.contains(code)) {
                    setSeatState(seat, SeatState.UNAVAILABLE);
                    seat.setDisable(true);
                } else {
                    setSeatState(seat, SeatState.AVAILABLE);
                    seat.setOnAction(e -> {
                        if (seat.isSelected()) {
                            setSeatState(seat, SeatState.SELECTED);
                            seleccion.add(code);
                        } else {
                            setSeatState(seat, SeatState.AVAILABLE);
                            seleccion.remove(code);
                        }
                        actualizarResumen();
                        sincronizarAsientosConCarrito();
                    });
                }

                seatByCode.put(code, seat);
                gridSala.add(seat, c, f);
            }
        }
    }

    private enum SeatState { AVAILABLE, SELECTED, UNAVAILABLE }

    private void setSeatState(ToggleButton b, SeatState st) {
        b.getStyleClass().removeAll("seat--available", "seat--selected", "seat--unavailable", "seat--accessible");
        boolean isAccessible = Boolean.TRUE.equals(b.getProperties().get("accessible"));

        switch (st) {
            case AVAILABLE -> {
                b.getStyleClass().add("seat--available");
                if (isAccessible) b.getStyleClass().add("seat--accessible");
            }
            case SELECTED -> {
                b.getStyleClass().add("seat--selected");
                if (isAccessible) b.getStyleClass().add("seat--accessible");
            }
            case UNAVAILABLE -> b.getStyleClass().add("seat--unavailable");
        }
        b.setSelected(st == SeatState.SELECTED);
    }

    private String code(int filaIdx, int colIdx) {
        char fila = (char) ('A' + filaIdx);
        return fila + String.valueOf(colIdx + 1);
    }

    private void actualizarResumen() {
        int n = seleccion.size();
        if (lblResumen != null) {
            lblResumen.setText(n + (n == 1 ? " Silla seleccionada" : " Sillas seleccionadas"));
        }
        if (btnContinuar != null) btnContinuar.setDisable(n == 0);
    }

    private void sincronizarAsientosConCarrito() {
        // Quitar del observable los items anteriores vinculados a la selección de asientos actual
        if (!asientoItems.isEmpty()) {
            for (var dto : asientoItems) {
                carrito.removeItem(dto);
            }
            asientoItems.clear();
        }
        if (seleccion.isEmpty()) return;
        for (String code : seleccion.stream().sorted().toList()) {
            String nombre = "Asiento " + code + " - " + (titulo != null ? titulo : "Película") + (hora != null ? " (" + hora + ")" : "");
            var dto = new sigmacine.aplicacion.data.CompraProductoDTO(null, this.funcionId, nombre, 1, PRECIO_ASIENTO, code);
            carrito.addItem(dto);
            asientoItems.add(dto);
        }
    }

    public void setFuncion(String titulo,
                        String hora,
                        java.util.Set<String> ocupados,
                        java.util.Set<String> accesibles,
                        Long funcionId) {
        if (titulo != null) this.titulo = titulo;
        if (hora   != null) this.hora   = hora;
        this.funcionId = funcionId;

        this.ocupados.clear();
        if (ocupados != null) this.ocupados.addAll(ocupados);

        this.accesibles.clear();
        if (accesibles != null && !accesibles.isEmpty()) {
            this.accesibles.addAll(shiftAccesiblesToFirstRowPlus2(accesibles));
        } else {
            this.accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));
        }

        if (lblTitulo != null)   lblTitulo.setText(this.titulo);
        if (lblHoraPill != null) lblHoraPill.setText(this.hora);
        if (gridSala != null) { poblarGrilla(); actualizarResumen(); }
    }

    public void setFuncion(String titulo,
                        String hora,
                        java.util.Set<String> ocupados,
                        java.util.Set<String> accesibles) {
        setFuncion(titulo, hora, ocupados, accesibles, null);
    }

    public Long getFuncionId() { return funcionId; }

    public void setPoster(Image poster) {
        this.poster = poster;
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);
    }

    public void setFuncionConPoster(String titulo, String hora, Collection<String> ocupados, Image poster) {
        setFuncion(titulo, hora,
                ocupados == null ? Collections.emptySet() : new HashSet<>(ocupados),
                null,
                null);
        setPoster(poster);
    }

    public void configurarTamanoSala(int filas, int columnas) {
        this.filas = filas; this.columnas = columnas;
        if (gridSala != null) { poblarGrilla(); actualizarResumen(); }
    }

    public List<String> getSeleccionados() {
        return seleccion.stream().sorted().collect(Collectors.toList());
    }

    @FXML
    private void onContinuar() {
    // Continuar pressed - no action (kept silent)
    }

    private Set<String> shiftAccesiblesToFirstRowPlus2(Set<String> entrada) {
        Set<String> out = new HashSet<>();
        for (String code : entrada) {
            if (code == null || code.isBlank()) continue;
            try {
                int col = Integer.parseInt(code.substring(1));
                int nueva = Math.min(Math.max(col + 2, 1), columnas);
                out.add("A" + nueva);
            } catch (NumberFormatException ignore) {}
        }
        return out;
    }

    // ---------------- Carrito popup ----------------
    private void toggleCartPopup() {
        if (cartStage != null && cartStage.isShowing()) {
            cartStage.close();
        } else {
            openCartPopup();
        }
    }

    private void openCartPopup() {
        try {
            if (cartStage == null) {
                var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
                Parent root = loader.load();
                cartStage = new Stage();
                cartStage.initOwner(gridSala.getScene().getWindow());
                cartStage.initModality(javafx.stage.Modality.NONE); // no bloquea
                cartStage.setResizable(false);
                cartStage.setTitle("Carrito");
                cartStage.setScene(new Scene(root));
                // Cerrar con ESC
                cartStage.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                    if (ev.getCode() == KeyCode.ESCAPE) cartStage.close();
                });
            }
            if (btnCart != null && btnCart.getScene() != null) {
                javafx.geometry.Bounds b = btnCart.localToScreen(btnCart.getBoundsInLocal());
                if (b != null) {
                    cartStage.setX(b.getMaxX() - 600);
                    cartStage.setY(b.getMaxY() + 8);
                }
            }
            cartStage.show();
            cartStage.toFront();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir el carrito: " + ex.getMessage()).showAndWait();
        }
    }
}
