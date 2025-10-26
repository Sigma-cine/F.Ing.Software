package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import sigmacine.dominio.entity.Pelicula;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;


public class DetallePeliculaController {

    @FXML private ImageView imgPoster;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private javafx.scene.layout.StackPane trailerContainer;
    @FXML private javafx.scene.layout.HBox trailerButtons;
    @FXML private Label lblTituloPelicula;
    @FXML private VBox panelFunciones;
    @FXML private Button btnComprar;
    @FXML private Button btnRegresarBusqueda;
    @FXML private Button btnRegresarHome;

    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;

    private Pelicula pelicula;
    private List<Pelicula> backResults;
    private String backTexto;

    public void setBackResults(List<Pelicula> results, String texto) {
        this.backResults = results;
        this.backTexto = texto;
    }

    @FXML
    private void initialize() {
        if (btnComprar != null) {
            btnComprar.setOnAction(e -> {
                // Navegar a la vista de contenidoCartelera para elegir función y asientos
                try {
                    var url = getClass().getResource("/sigmacine/ui/views/contenidoCartelera.fxml");
                    if (url == null) {
                        var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        a.setHeaderText("No se encontró la vista de funciones");
                        a.setContentText("Ruta esperada: /sigmacine/ui/views/contenidoCartelera.fxml");
                        a.showAndWait();
                        return;
                    }
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Object ctrl = loader.getController();
                    if (ctrl instanceof sigmacine.ui.controller.ContenidoCarteleraController) {
                        sigmacine.ui.controller.ContenidoCarteleraController c = (sigmacine.ui.controller.ContenidoCarteleraController) ctrl;
                        try { c.setPelicula(this.pelicula); } catch (Exception ignore) {}
                        try { c.setUsuario(sigmacine.aplicacion.session.Session.getCurrent()); } catch (Exception ignore) {}
                        try { c.setCoordinador(null); } catch (Exception ignore) {}
                    }

                    Stage stage = (Stage) btnComprar.getScene().getWindow();
                    Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                    stage.setTitle("Selecciona función y asientos");
                    stage.setMaximized(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    a.setHeaderText("Error al abrir funciones");
                    a.setContentText(String.valueOf(ex));
                    a.showAndWait();
                }
            });
        }
        if (btnRegresarBusqueda != null) {
            btnRegresarBusqueda.setOnAction(e -> {
                try {
                    var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
                    javafx.scene.Parent root = loader.load();
                    ResultadosBusquedaController ctrl = loader.getController();
                    if (this.backResults != null) {
                        ctrl.setResultados(this.backResults, this.backTexto != null ? this.backTexto : "");
                    }
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnRegresarBusqueda.getScene().getWindow();
                    javafx.scene.Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (btnRegresarHome != null) {
            btnRegresarHome.setOnAction(e -> {
                try {
                    var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnRegresarHome.getScene().getWindow();
                    javafx.scene.Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;

        // Poster
        String url = safe(p.getPosterUrl());
        if (!url.isEmpty()) {
            try {
                var res = getClass().getResourceAsStream("/Images/" + url);
                if (res != null) {
                    imgPoster.setImage(new Image(res));
                } else {
                    imgPoster.setImage(new Image(url, true));
                }
            } catch (Exception ex) {
                imgPoster.setImage(null);
            }
        } else {
            imgPoster.setImage(null);
        }

        lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));
        lblTituloPelicula.setText(safe(p.getTitulo(), "N/D"));

        lblGenero.setText(safe(p.getGenero(), "N/D"));
        lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");

        lblDirector.setText(safe(p.getDirector(), "N/D"));
    lblReparto.setText(safe(p.getReparto()));

        // Sinopsis
        txtSinopsis.setText(safe(p.getSinopsis()));
        
        try {
            trailerContainer.getChildren().clear();
            trailerButtons.getChildren().clear();
            List<String> trailers = new ArrayList<>();
            if (p.getTrailer() != null && !p.getTrailer().isBlank()) trailers.add(p.getTrailer());
            try (Connection cn = new DatabaseConfig().getConnection();
                PreparedStatement ps = cn.prepareStatement("SELECT URL FROM PELICULA_TRAILER WHERE PELICULA_ID = ?")) {
                ps.setInt(1, p.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String u = rs.getString("URL");
                        if (u != null && !u.isBlank() && !trailers.contains(u)) trailers.add(u);
                    }
                }
            } catch (Exception dbex) { }

            if (trailers.isEmpty()) {
                javafx.scene.control.Label none = new javafx.scene.control.Label("No hay trailer disponible");
                none.setStyle("-fx-text-fill: #cbd5e1;");
                trailerContainer.getChildren().add(none);
                return;
            }

            Object webView = null;
            Object engine = null;
            final boolean[] webAvailable = new boolean[] { true };
            try {
                Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
                webView = webViewClass.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method getEngine = webViewClass.getMethod("getEngine");
                engine = getEngine.invoke(webView);
            } catch (ClassNotFoundException cnf) {
                webAvailable[0] = false;
            }

            final Object finalWebView = webView;
            final Object finalEngine = engine;

            for (int i = 0; i < trailers.size(); i++) {
                final String trailerUrl = trailers.get(i);
                javafx.scene.control.Button b = new javafx.scene.control.Button("Trailer " + (i + 1));
                b.setOnAction(ev -> {
                    try {
                        trailerContainer.getChildren().clear();
                        if (webAvailable[0] && finalWebView != null && finalEngine != null) {
                            String html = null;
                            if (trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be")) {
                                String id = null;
                                if (trailerUrl.contains("v=")) {
                                    int ii = trailerUrl.indexOf("v=") + 2;
                                    int amp = trailerUrl.indexOf('&', ii);
                                    id = amp > 0 ? trailerUrl.substring(ii, amp) : trailerUrl.substring(ii);
                                } else if (trailerUrl.contains("youtu.be/")) {
                                    int ii = trailerUrl.indexOf("youtu.be/") + 9;
                                    int q = trailerUrl.indexOf('?', ii);
                                    id = q > 0 ? trailerUrl.substring(ii, q) : trailerUrl.substring(ii);
                                }
                                if (id != null && !id.isEmpty()) {
                                    html = "<html><body style='margin:0;background:#000;'><iframe width='100%' height='100%' src='https://www.youtube.com/embed/" + id + "' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe></body></html>";
                                }
                            }
                            if (html != null) {
                                java.lang.reflect.Method loadContent = finalEngine.getClass().getMethod("loadContent", String.class);
                                loadContent.invoke(finalEngine, html);
                            } else {
                                java.lang.reflect.Method load = finalEngine.getClass().getMethod("load", String.class);
                                load.invoke(finalEngine, trailerUrl.startsWith("http") ? trailerUrl : "data:text/html," + trailerUrl);
                            }
                            trailerContainer.getChildren().add((javafx.scene.Node) finalWebView);
                        } else {
                            javafx.scene.control.Button open = new javafx.scene.control.Button("Abrir trailer en navegador");
                            open.setOnAction(ae -> { try { java.awt.Desktop.getDesktop().browse(new java.net.URI(trailerUrl)); } catch (Exception e) { e.printStackTrace(); } });
                            trailerContainer.getChildren().add(open);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
                trailerButtons.getChildren().add(b);
                if (i == 0) b.fire();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private static String safe(String s) {
        return (s == null || s.trim().equalsIgnoreCase("null")) ? "" : s.trim();
    }

    private static String safe(String s, String alt) {
        String t = safe(s);
        return t.isEmpty() ? alt : t;
    }
}
