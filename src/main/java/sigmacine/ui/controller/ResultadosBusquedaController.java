package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.ui.controller.ControladorControlador;
import java.util.List;
import java.io.File;

public class ResultadosBusquedaController {
    @FXML private javafx.scene.control.Button btnVolver;
    @FXML private Label lblTituloResultados;
    @FXML private Label lblTextoBuscado;
    @FXML private VBox panelPeliculas;

    private List<Pelicula> peliculas;
    private String textoBuscado;
    private ControladorControlador coordinador;
    private UsuarioDTO usuario;

    public void setCoordinador(ControladorControlador coordinador) {
        this.coordinador = coordinador;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public void setResultados(List<Pelicula> peliculas, String textoBuscado) {
        this.peliculas = peliculas;
        this.textoBuscado = textoBuscado;
        mostrarResultados();

        if (btnVolver != null) {
            btnVolver.setOnAction(e -> volverAInicio());
        }
    }

    private void volverAInicio() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
            // initialize controller with current session so the user remains logged in
            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof ClienteController) {
                    ClienteController c = (ClienteController) ctrl;
                    c.init(this.usuario);
                    c.setCoordinador(this.coordinador);
                }
            } catch (Exception ignore) {}
            // preserve current window size when switching
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine");
        } catch (Exception ex) {
            System.err.println("Error al volver a inicio: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarResultados() {
        if (lblTituloResultados != null) lblTituloResultados.setText("Resultados de búsqueda");
        if (lblTextoBuscado != null) lblTextoBuscado.setText(textoBuscado != null ? textoBuscado : "");
        if (panelPeliculas == null) return;
        panelPeliculas.getChildren().clear();
        if (peliculas == null || peliculas.isEmpty()) {
            // Mostrar mensaje amigable cuando no hay coincidencias
            Label msg = new Label("No hay coincidencias");
            msg.setStyle("-fx-text-fill: #ddd; -fx-font-size: 18px; -fx-font-weight: bold;");
            // Centrar el mensaje dentro del panel
            panelPeliculas.setAlignment(Pos.CENTER);
            panelPeliculas.getChildren().add(msg);
            return;
        }
        for (Pelicula p : peliculas) {
            // Card container
            VBox tarjeta = new VBox(6);
            tarjeta.setAlignment(Pos.TOP_LEFT);
            // Fixed card height (slightly larger so sinopsis can display fully)
            tarjeta.setPrefHeight(300);
            tarjeta.prefWidthProperty().bind(panelPeliculas.widthProperty().subtract(40));
            tarjeta.setStyle("-fx-background-color: #222; -fx-background-radius: 10; -fx-padding: 8; -fx-effect: dropshadow(gaussian, #000, 4, 0.12, 0, 1);");

            // Poster column (left) - reserve 1/3 width
            javafx.scene.layout.StackPane posterPane = new javafx.scene.layout.StackPane();
            posterPane.prefWidthProperty().bind(tarjeta.widthProperty().multiply(1.0/3.0));
            // Let poster pane height be determined by the info column; keep sensible minimums
            posterPane.setMinWidth(80);
            posterPane.setMinHeight(80);
            posterPane.setStyle("-fx-background-color: transparent;");

            ImageView poster = new ImageView();
            poster.setPreserveRatio(true);
            // we will bind poster fitHeight later to match the info column height

            javafx.scene.control.Label posterPlaceholder = new javafx.scene.control.Label("No image");
            posterPlaceholder.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            posterPlaceholder.setWrapText(true);
            posterPane.getChildren().addAll(poster, posterPlaceholder);
            javafx.scene.layout.StackPane.setAlignment(posterPlaceholder, Pos.CENTER);
            javafx.scene.layout.StackPane.setAlignment(poster, Pos.CENTER);
            poster.visibleProperty().bind(poster.imageProperty().isNotNull());
            posterPlaceholder.visibleProperty().bind(poster.imageProperty().isNull());

            // Try load poster (remote URL or classpath resource under /Images)
            try {
                String posterRef = p.getPosterUrl();
                if (posterRef != null && !posterRef.isBlank()) {
                    Image img = resolveImage(posterRef);
                    if (img != null) {
                        poster.setImage(img);
                    } else {
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Info column (center) - reserve 1/3 width
            VBox infoCol = new VBox(6);
            infoCol.setAlignment(Pos.TOP_LEFT);
            infoCol.prefWidthProperty().bind(tarjeta.widthProperty().multiply(1.0/3.0));
            infoCol.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(infoCol, javafx.scene.layout.Priority.ALWAYS);

            Label titulo = new Label(p.getTitulo() != null ? p.getTitulo() : "Título desconocido");
            titulo.setStyle("-fx-font-size: 22px; -fx-text-fill: #fff; -fx-font-weight: bold;");
            titulo.setWrapText(true);
            Label genero = new Label("Género: " + (p.getGenero() != null ? p.getGenero() : "-"));
            genero.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label clasificacion = new Label("Clasificación: " + (p.getClasificacion() != null ? p.getClasificacion() : "-"));
            clasificacion.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label duracion = new Label("Duración: " + (p.getDuracion() > 0 ? p.getDuracion() + " min" : "-"));
            duracion.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            Label director = new Label("Director: " + (p.getDirector() != null ? p.getDirector() : "-"));
            director.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            String repartoText = (p.getReparto() != null && !p.getReparto().isBlank()) ? p.getReparto() : "No disponible";
            Label reparto = new Label("Reparto: " + repartoText);
            reparto.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            reparto.setWrapText(true);
            String sinopsisText = p.getSinopsis() != null ? p.getSinopsis() : "No disponible";
            Label sinopsis = new Label("Sinopsis: " + sinopsisText);
            // use same size/color as other info labels
            sinopsis.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
            sinopsis.setWrapText(true);

            infoCol.getChildren().addAll(titulo, genero, clasificacion, duracion, director, reparto, sinopsis);

            // Ensure wrapping respects the info column width
            titulo.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));
            titulo.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            reparto.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));
            sinopsis.maxWidthProperty().bind(infoCol.widthProperty().subtract(10));

            // Make posterPane follow a portion of the tarjeta height
            posterPane.prefHeightProperty().bind(tarjeta.heightProperty().multiply(0.9));

            // Bind poster fitHeight to the posterPane so the image scales properly
            poster.fitHeightProperty().bind(posterPane.heightProperty().multiply(0.95));
            poster.fitWidthProperty().bind(posterPane.widthProperty().multiply(0.9));

            // Button column (right) - reserve 1/3 width
            javafx.scene.layout.StackPane btnPane = new javafx.scene.layout.StackPane();
            btnPane.prefWidthProperty().bind(tarjeta.widthProperty().multiply(1.0/3.0));
            btnPane.setMaxWidth(Double.MAX_VALUE);
            btnPane.setAlignment(Pos.CENTER);
            javafx.scene.layout.HBox.setHgrow(btnPane, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.control.Button btnDetalle = new javafx.scene.control.Button("Ver detalle película");
            btnDetalle.setStyle("-fx-background-color: #993726; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 30;");
            btnDetalle.setOnAction(e -> mostrarDetallePelicula(p));
            btnPane.getChildren().add(btnDetalle);

            javafx.scene.layout.HBox fila = new javafx.scene.layout.HBox(12);
            fila.setAlignment(Pos.TOP_LEFT);
            fila.setStyle("-fx-padding: 6 0 6 0;");
            fila.getChildren().addAll(posterPane, infoCol, btnPane);

            tarjeta.getChildren().addAll(fila);
            panelPeliculas.getChildren().add(tarjeta);
        }
        javafx.application.Platform.runLater(() -> {
            try {
                panelPeliculas.applyCss();
                panelPeliculas.layout();
            } catch (Exception ignored) {}
        });
    }
private void mostrarDetallePelicula(Pelicula p) {
    try {
        var url = getClass().getResource("/sigmacine/ui/views/verdetallepelicula.fxml");
        if (url == null) throw new IllegalStateException("No se encontró verdetallepelicula.fxml");

        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
        javafx.scene.Parent rootDetalle = loader.load();

    VerDetallePeliculaController ctrl = loader.getController();
    // pass session info so detail can preserve and return with the same user
    try {
        ctrl.setCoordinador(this.coordinador);
    } catch (Exception ignore) {}
    try {
        ctrl.setUsuario(this.usuario);
    } catch (Exception ignore) {}
    // Ensure the controller refreshes its session-aware UI
    try {
        ctrl.refreshSessionUI();
    } catch (Exception ignore) {}
    ctrl.setBackResults(this.peliculas, this.textoBuscado);
    ctrl.setPelicula(p);

        javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
        javafx.scene.Scene current = stage.getScene();
        double w = current != null ? current.getWidth() : 900;
        double h = current != null ? current.getHeight() : 600;
        javafx.scene.Scene scene = new javafx.scene.Scene(rootDetalle, w > 0 ? w : 900, h > 0 ? h : 600);
        stage.setScene(scene);
        stage.setTitle(p.getTitulo() != null ? p.getTitulo() : "Detalle de Película");
        
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) {
            return null;
        }
        try {
            String lower = ref.toLowerCase();
            // 1. URLs externas
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            
            // 2. Si contiene la ruta completa "src\main\resources\Images\", extraer solo el nombre
            if (ref.contains("src\\main\\resources\\Images\\") || ref.contains("src/main/resources/Images/")) {
                String fileName = ref.substring(ref.lastIndexOf("\\") + 1);
                if (fileName.isEmpty()) fileName = ref.substring(ref.lastIndexOf("/") + 1);
                
                java.net.URL res = getClass().getResource("/Images/" + fileName);
                if (res != null) {
                    return new Image(res.toExternalForm(), false);
                } else {
                }
            }
            
            // 3. Probar como recurso directo /Images/...
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
            
            // 4. Probar como archivo local
            File f = new File(ref);
            if (f.exists()) {
                return new Image(f.toURI().toString(), false);
            }
            
            // 5. Probar con / al inicio
            res = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
            
        } catch (Exception ex) {
                System.err.println("Error resolviendo imagen: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    @FXML
    private void onBrandClick() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            javafx.scene.Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof ClienteController) {
                ClienteController c = (ClienteController) ctrl;
                c.setCoordinador(this.coordinador);
                c.init(this.usuario);
            }
            javafx.stage.Stage stage = (javafx.stage.Stage) panelPeliculas.getScene().getWindow();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}