package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;

import java.util.List;

public class CarteleraController {
    private ControladorControlador coordinador;
    private UsuarioDTO usuario;

    @FXML private Button btnIniciarSesion;
    @FXML private Button btnRegistrarse;
    @FXML private Label lblUserName;
    @FXML private MenuButton menuPerfil;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miHistorial;
    @FXML private TextField txtBuscar;

    @FXML private javafx.scene.layout.FlowPane gridPeliculas;

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }

    @FXML
    private void initialize() {
            // Marcar la página activa usando Singleton
            BarraController barraController = BarraController.getInstance();
            if (barraController != null) {
                barraController.marcarBotonActivo("cartelera");
            }
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            List<Pelicula> todas = repo.buscarTodas();
            renderPeliculas(todas);
            wireTopbar();
        } catch (Exception ex) {
            if (gridPeliculas != null) gridPeliculas.getChildren().add(new Label("Error cargando cartelera: " + ex.getMessage()));
            ex.printStackTrace();
        }
    }

    private void renderPeliculas(List<Pelicula> peliculas) {
        if (gridPeliculas == null) return;
        gridPeliculas.getChildren().clear();
        for (Pelicula p : peliculas) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: #161616; -fx-background-radius: 8; -fx-padding: 10; -fx-pref-width: 220;");
            card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

            ImageView poster = new ImageView();
            poster.setFitWidth(200);
            poster.setPreserveRatio(true);
            try {
                Image img = resolveImage(p.getPosterUrl());
                if (img != null) poster.setImage(img);
            } catch (Exception ignore) {}

            Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Sin título");
            titulo.setStyle("-fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14; -fx-wrap-text: true;" );
            titulo.setMaxWidth(200);

            Label gen = new Label(safe(p.getGenero(), "N/D")); gen.setStyle("-fx-text-fill: #cbd5e1;");
            Label dur = new Label((p.getDuracion() > 0 ? p.getDuracion() + " min" : "N/D")); dur.setStyle("-fx-text-fill: #cbd5e1;");

            Button btnDetalle = new Button("Ver detalle película");
            btnDetalle.getStyleClass().add("primary-btn");
            btnDetalle.setOnAction(e -> abrirDetalle(p));

            card.getChildren().addAll(poster, titulo, gen, dur, btnDetalle);
            gridPeliculas.getChildren().add(card);
        }
    }

    private static String safe(String s, String alt) {
        if (s == null || s.trim().isEmpty() || s.trim().equalsIgnoreCase("null")) return alt;
        return s.trim();
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String lower = ref.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            if (ref.contains("src\\main\\resources\\Images\\") || ref.contains("src/main/resources/Images/")) {
                String fileName = ref.substring(Math.max(ref.lastIndexOf('\\'), ref.lastIndexOf('/')) + 1);
                java.net.URL res = getClass().getResource("/Images/" + fileName);
                if (res != null) return new Image(res.toExternalForm(), false);
            }
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) return new Image(res.toExternalForm(), false);
        } catch (Exception ignore) {}
        return null;
    }

    @FXML
    private void onBrandClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof ClienteController) {
                ClienteController c = (ClienteController) ctrl;
                c.setCoordinador(this.coordinador);
                c.init(this.usuario);
            }
            Stage stage = (Stage) gridPeliculas.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onBuscarTop() {
        try {
            String texto = txtBuscar != null ? txtBuscar.getText() : "";

            DatabaseConfig db = new DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            Parent root = loader.load();
            ResultadosBusquedaController controller = loader.getController();
            controller.setCoordinador(this.coordinador);
            controller.setUsuario(this.usuario);
            controller.setResultados(resultados, texto);

            Stage stage = (Stage) gridPeliculas.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle("Resultados de búsqueda");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void wireTopbar() {
        try {
            boolean logged = sigmacine.aplicacion.session.Session.isLoggedIn();
            if (btnIniciarSesion != null) { btnIniciarSesion.setVisible(!logged); btnIniciarSesion.setManaged(!logged); }
            if (btnRegistrarse != null) { btnRegistrarse.setVisible(!logged); btnRegistrarse.setManaged(!logged); }
            if (lblUserName != null) { lblUserName.setVisible(logged); lblUserName.setManaged(logged); lblUserName.setText(logged && sigmacine.aplicacion.session.Session.getCurrent()!=null ? sigmacine.aplicacion.session.Session.getCurrent().getNombre() : ""); }
            if (menuPerfil != null) { menuPerfil.setVisible(logged); menuPerfil.setManaged(logged); }

            if (btnIniciarSesion != null) btnIniciarSesion.setOnAction(e -> {
                try { if (this.coordinador != null) this.coordinador.mostrarLogin(); } catch (Exception ex) { ex.printStackTrace(); }
            });
            if (miCerrarSesion != null) miCerrarSesion.setOnAction(e -> { sigmacine.aplicacion.session.Session.clear(); wireTopbar(); });
            if (miHistorial != null) miHistorial.setOnAction(e -> {
                try {
                    System.out.println("[CarteleraController] miHistorial clicked, preparing historial view...");
                    if (!sigmacine.aplicacion.session.Session.isLoggedIn()) {
                        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        a.setTitle("Acceso denegado"); a.setHeaderText(null); a.setContentText("Debes iniciar sesión para ver tu historial de compras."); a.showAndWait(); return;
                    }
                    sigmacine.infraestructura.configDataBase.DatabaseConfig db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
                    sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc usuarioRepo = new sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc(db);
                    sigmacine.aplicacion.service.VerHistorialService historialService = new sigmacine.aplicacion.service.VerHistorialService(usuarioRepo);
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
                    var controller = new VerHistorialController(historialService);
                    var cur = sigmacine.aplicacion.session.Session.getCurrent();
                    if (cur != null && cur.getEmail()!=null) controller.setUsuarioEmail(cur.getEmail());
                    // capturar escena actual para permitir volver
                    javafx.scene.Scene prev = null;
                    try { prev = gridPeliculas != null && gridPeliculas.getScene() != null ? gridPeliculas.getScene() : null; } catch (Exception ignore) {}
                    try { controller.setPreviousScene(prev); } catch (Exception ignore) {}
                    // use controller factory to avoid NPEs when FXML expects injections
                    loader.setControllerFactory(cls -> {
                        if (cls == sigmacine.ui.controller.VerHistorialController.class) return controller;
                        try { return cls.getDeclaredConstructor().newInstance(); } catch (Exception ex) { throw new RuntimeException(ex); }
                    });
                    javafx.scene.Parent root = loader.load();
                    Stage stage = (Stage) gridPeliculas.getScene().getWindow();
                    Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 1000; double h = current != null ? current.getHeight() : 700;
                    stage.setScene(new Scene(root, w, h)); stage.setTitle("Historial de compras"); stage.setMaximized(true);
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirDetalle(Pelicula p) {
        try {
            var url = getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml");
            if (url == null) throw new IllegalStateException("No se encontró verdetallepelicula.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof VerDetallePeliculaController) {
                VerDetallePeliculaController c = (VerDetallePeliculaController) ctrl;
                try { c.setCoordinador(this.coordinador); } catch (Exception ignore) {}
                try { c.setUsuario(this.usuario); } catch (Exception ignore) {}
                try { c.refreshSessionUI(); } catch (Exception ignore) {}
                c.setPelicula(p);
            }

            Stage stage = (Stage) gridPeliculas.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
