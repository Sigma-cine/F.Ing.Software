package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;

import java.util.List;
import java.util.stream.Collectors;

public class CarteleraController {
    private ControladorControlador coordinador;
    private UsuarioDTO usuario;
    private List<Pelicula> todasLasPeliculas;

    @FXML private TextField txtBuscar;
    @FXML private javafx.scene.layout.FlowPane gridPeliculas;
    @FXML private ComboBox<String> cbGenero;

    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }

    @FXML
    private void initialize() {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("cartelera");
        }
        
        // Configurar ComboBox de géneros
        if (cbGenero != null) {
            cbGenero.getItems().addAll(
                "Todos",
                "Acción",
                "Animación",
                "Aventura",
                "Ciencia ficción",
                "Comedia",
                "Drama",
                "Fantasía",
                "Musical",
                "Romance",
                "Superhéroes",
                "Thriller psicológico"
            );
            cbGenero.setValue("Todos");
            cbGenero.setOnAction(e -> filtrarPorGenero());
        }
        
        try {
            DatabaseConfig db = new DatabaseConfig();
            PeliculaRepositoryJdbc repo = new PeliculaRepositoryJdbc(db);
            todasLasPeliculas = repo.buscarTodas();
            renderPeliculas(todasLasPeliculas);
        } catch (Exception ex) {
            if (gridPeliculas != null) gridPeliculas.getChildren().add(new Label("Error cargando cartelera: " + ex.getMessage()));
            ex.printStackTrace();
        }
    }
    
    private void filtrarPorGenero() {
        if (todasLasPeliculas == null || cbGenero == null) return;
        
        String generoSeleccionado = cbGenero.getValue();
        if (generoSeleccionado == null || generoSeleccionado.equals("Todos")) {
            renderPeliculas(todasLasPeliculas);
            return;
        }
        
        List<Pelicula> filtradas = todasLasPeliculas.stream()
            .filter(p -> {
                String genero = p.getGenero();
                if (genero == null) return false;
                // Manejar géneros múltiples separados por comas
                String[] generos = genero.split(",");
                for (String g : generos) {
                    if (g.trim().equalsIgnoreCase(generoSeleccionado.trim())) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
        
        renderPeliculas(filtradas);

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
            
            // Primero intentar el método más rápido: recursos internos
            if (ref.contains("src\\main\\resources\\Images\\") || ref.contains("src/main/resources/Images/")) {
                String fileName = ref.substring(Math.max(ref.lastIndexOf('\\'), ref.lastIndexOf('/')) + 1);
                java.net.URL res = getClass().getResource("/Images/" + fileName);
                if (res != null) return new Image(res.toExternalForm(), true); // carga en background
            }
            
            // Intentar como nombre de archivo directo en recursos
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) return new Image(res.toExternalForm(), true); // carga en background
            
            // Solo si es una URL externa, intentar cargarla
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true); // carga en background
            }
        } catch (Exception ignore) {}
        return null;
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