package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.entity.PeliculaTrailer;
import sigmacine.dominio.repository.FuncionRepository;
import sigmacine.dominio.repository.PeliculaTrailerRepository;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.aplicacion.session.Session;
import sigmacine.infraestructura.persistencia.jdbc.FuncionRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaTrailerRepositoryJdbc;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class ContenidoCarteleraController {

    @FXML private Button btnBack;
    @FXML private TextArea txtSinopsis;
    @FXML private Label lblSinopsisTitulo;
    @FXML private Label lblTituloPelicula;
    @FXML private VBox panelFunciones;
    @FXML private Button btnComprar;
    @FXML private Label lblGenero, lblClasificacion, lblDuracion, lblDirector, lblReparto;
    @FXML private ImageView imgPoster;
    @FXML private StackPane trailerContainer;
    @FXML private ScrollPane spCenter;
    @FXML private VBox detalleRoot;
    @FXML private HBox dayTabs;

    private Pelicula pelicula;
    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    private ClienteController host;
    
    public void setHost(ClienteController host) { this.host = host; }

    @FXML
    private void initialize() {
        // Solo mantener el Singleton para marcar la página activa
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("detalle");
        }
        
        if (trailerContainer != null) {
            trailerContainer.setMouseTransparent(false);
            trailerContainer.setPickOnBounds(true);
        }
        if (spCenter != null) spCenter.setPannable(false);

        if (btnComprar != null) {
            btnComprar.setDisable(false);
            btnComprar.setMouseTransparent(false);
            btnComprar.setPickOnBounds(true);
            btnComprar.setFocusTraversable(true);
            btnComprar.setViewOrder(-1000);
            btnComprar.toFront();
            if (btnComprar.getParent() != null) btnComprar.getParent().toFront();

            btnComprar.setOnAction(e -> onComprarTickets());
        }
    }

    @FXML
    private void onVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();

            try {
                Object ctrl = loader.getController();
                if (ctrl instanceof ClienteController c) {
                    c.init(this.usuario);
                    c.setCoordinador(this.coordinador);
                }
            } catch (Exception ignore) {}

            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setTitle("Sigma Cine");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPelicula(Pelicula p) {
        this.pelicula = p;
        if (p == null) return;

        String posterRef = safe(p.getPosterUrl());
        if (!posterRef.isEmpty()) {
            try {
                Image resolved = resolveImage(posterRef);
                if (imgPoster != null) imgPoster.setImage(resolved);
            } catch (Exception ignored) {
                if (imgPoster != null) imgPoster.setImage(null);
            }
        } else {
            if (imgPoster != null) imgPoster.setImage(null);
        }

        if (lblSinopsisTitulo != null) lblSinopsisTitulo.setText("SINOPSIS — " + safe(p.getTitulo(), "N/D"));
        if (lblTituloPelicula != null) lblTituloPelicula.setText(safe(p.getTitulo(), "N/D"));
        if (lblGenero != null) lblGenero.setText(safe(p.getGenero(), "N/D"));
        if (lblClasificacion != null) lblClasificacion.setText(safe(p.getClasificacion(), "N/D"));
        int dur = p.getDuracion();
        if (lblDuracion != null) lblDuracion.setText(dur > 0 ? dur + " min" : "N/D");
        if (lblDirector != null) lblDirector.setText(safe(p.getDirector(), "N/D"));
        if (lblReparto != null) lblReparto.setText(safe(p.getReparto(), ""));
        if (txtSinopsis != null) txtSinopsis.setText(safe(p.getSinopsis()));

        // Cargar trailers
        cargarTrailers(p.getId());

        // Cargar funciones
        cargarFunciones();
    }

    private void cargarFunciones() {
        if (pelicula == null) return;
        
        try {
            if (panelFunciones != null) {
                panelFunciones.getChildren().clear();
                DatabaseConfig db = new DatabaseConfig();
                FuncionRepository repo = new FuncionRepositoryJdbc(db);
                List<FuncionDisponibleDTO> funciones = repo.listarPorPelicula(pelicula.getId());
                String city = Session.getSelectedCity();
                if (city != null && !city.isBlank()) {
                    funciones = funciones.stream()
                            .filter(f -> city.equalsIgnoreCase(f.getCiudad()))
                            .toList();
                }
                // construir tabs de día en base a fechas disponibles
                buildDayTabsFrom(funciones);
                // aplicar filtro por día si hay pestaña seleccionada (fall back a la primera fecha disponible)
                java.time.LocalDate selected = getSelectedDay();
                if (selected == null) {
                    java.time.LocalDate first = funciones.stream().map(FuncionDisponibleDTO::getFecha)
                            .sorted()
                            .findFirst().orElse(null);
                    setSelectedDay(first);
                }
                if (getSelectedDay() != null) {
                    java.time.LocalDate d = getSelectedDay();
                    funciones = funciones.stream().filter(f -> d.equals(f.getFecha())).toList();
                }
                renderFunciones(funciones);
            }
        } catch (Exception ignored) {}
    }

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    private static String safe(String s) {
        return (s == null || s.trim().equalsIgnoreCase("null")) ? "" : s.trim();
    }
    
    private static String safe(String s, String alt) {
        String t = safe(s);
        return t.isEmpty() ? alt : t;
    }

    private Image resolveImage(String ref) {
        if (ref == null || ref.isBlank()) return null;
        try {
            String lower = ref.toLowerCase();
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file:/")) {
                return new Image(ref, true);
            }
            java.net.URL res = getClass().getResource("/Images/" + ref);
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
            File f = new File(ref);
            if (f.exists()) {
                return new Image(f.toURI().toString(), false);
            }
            res = getClass().getResource(ref.startsWith("/") ? ref : ("/" + ref));
            if (res != null) {
                return new Image(res.toExternalForm(), false);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void renderFunciones(List<FuncionDisponibleDTO> funciones) {
        panelFunciones.getChildren().clear();
        try { panelFunciones.setAlignment(Pos.CENTER); } catch (Exception ignore) {}
        if (funciones == null || funciones.isEmpty()) return;

        // Agrupar por ciudad -> sede y acumular horas únicas por sede
        Map<String, Map<String, Set<FuncionDisponibleDTO>>> porCiudad = new LinkedHashMap<>();
        for (FuncionDisponibleDTO f : funciones) {
            porCiudad.computeIfAbsent(f.getCiudad(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(f.getSede(), k -> new LinkedHashSet<>())
                    .add(f);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

        for (var ciudadEntry : porCiudad.entrySet()) {
            VBox ciudadBox = new VBox(8);
            try { ciudadBox.setAlignment(Pos.TOP_LEFT); } catch (Exception ignore) {}
            Label lblCiudad = new Label(ciudadEntry.getKey());
            lblCiudad.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:16; -fx-alignment:center;");
            ciudadBox.getChildren().add(lblCiudad);

            for (var sedeEntry : ciudadEntry.getValue().entrySet()) {
                VBox sedeBox = new VBox(4);
                try { sedeBox.setAlignment(Pos.TOP_LEFT); } catch (Exception ignore) {}
                Label lblSede = new Label(sedeEntry.getKey());
                lblSede.setStyle("-fx-text-fill:#e5e7eb;-fx-font-weight:bold;-fx-font-size:14; -fx-alignment:center;");
                sedeBox.getChildren().add(lblSede);

                // Etiqueta HORA
                Label lblHora = new Label("HORA");
                lblHora.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12;-fx-font-weight:bold;");
                sedeBox.getChildren().add(lblHora);

                // Contenedor que se envuelve cuando no hay espacio suficiente
                javafx.scene.layout.FlowPane fila = new javafx.scene.layout.FlowPane();
                fila.setHgap(8);
                fila.setVgap(4);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-background-color:transparent;");

                // ordenar por hora
                List<FuncionDisponibleDTO> ordenadas = sedeEntry.getValue().stream()
                        .sorted(java.util.Comparator.comparing(FuncionDisponibleDTO::getHora))
                        .collect(Collectors.toList());

                // construir pills solo con la hora en formato am/pm
                for (FuncionDisponibleDTO f : ordenadas) {
                    String pillText = fmt.format(f.getHora()).toLowerCase(); // ej: 12:50pm
                    Button b = new Button(pillText);
                    b.setStyle("-fx-background-color:transparent;-fx-border-color:#ffffff66;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:4 12 4 12;");
                    // Guardar selección exacta de función
                    b.setOnAction(e -> seleccionarFuncion(f, b));
                    fila.getChildren().add(b);
                }
                sedeBox.getChildren().add(fila);
                ciudadBox.getChildren().add(sedeBox);
            }

            // Envolver cada bloque de ciudad en un contenedor centrado horizontalmente
            HBox wrapCity = new HBox(ciudadBox);
            wrapCity.setAlignment(Pos.CENTER);
            panelFunciones.getChildren().add(wrapCity);
        }
    }

    private String selectedFuncionText;
    private Long selectedFuncionId;
    private Button selectedHoraButton;
    
    private void seleccionarFuncion(FuncionDisponibleDTO f, Button sourceBtn) {
        this.selectedFuncionId = f.getFuncionId();
        String texto = java.time.format.DateTimeFormatter.ofPattern("h:mma", java.util.Locale.ENGLISH)
                .format(f.getHora()).toLowerCase();
        seleccionarFuncionPill(texto);
        try {
            if (selectedHoraButton != null) {
                selectedHoraButton.setStyle("-fx-background-color:transparent;-fx-border-color:#ffffff66;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:4 12 4 12;");
            }
            selectedHoraButton = sourceBtn;
            if (selectedHoraButton != null) {
                selectedHoraButton.setStyle("-fx-background-color:#8A2F24;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:4 12 4 12;");
            }
        } catch (Exception ignore) {}
    }

    private void seleccionarFuncionPill(String texto) {
        selectedFuncionText = texto;
    }

    @FXML
    private void onComprarTickets() {
        try {
            if (selectedFuncionText == null || selectedFuncionText.isBlank()) {
                var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                a.setHeaderText("Selecciona una hora");
                a.setContentText("Primero elige una función (sede/hora) antes de continuar.");
                a.showAndWait();
                return;
            }

            if (isEmbedded()) {
                String titulo = (lblTituloPelicula != null && !lblTituloPelicula.getText().isBlank())
                        ? lblTituloPelicula.getText() : "Película";
                String hora = selectedFuncionText;

                Set<String> ocupados   = Set.of("B3","B4","C7","E2","F8");
                Set<String> accesibles = Set.of("E3","E4","E5","E6");

                host.mostrarAsientos(titulo, hora, ocupados, accesibles);
                return;
            }

            String titulo = (lblTituloPelicula != null && !lblTituloPelicula.getText().isBlank())
                    ? lblTituloPelicula.getText() : "Película";
            String hora = selectedFuncionText;

            URL url = getClass().getResource("/sigmacine/ui/views/asientos.fxml");
            if (url == null) {
                var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setHeaderText("No se encontró asientos.fxml");
                a.setContentText("Ruta esperada: /sigmacine/ui/views/asientos.fxml");
                a.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AsientosController ctrl = loader.getController();
            Set<String> ocupados   = Set.of("B3","B4","C7","E2","F8");
            Set<String> accesibles = Set.of("E3","E4","E5","E6");
            ctrl.setFuncion(titulo, hora, ocupados, accesibles, selectedFuncionId);

            String posterResource = (pelicula != null && pelicula.getPosterUrl() != null && !pelicula.getPosterUrl().isBlank())
                    ? pelicula.getPosterUrl() : null;
            if (posterResource != null) {
                var is = getClass().getResourceAsStream(posterResource);
                if (is != null) ctrl.setPoster(new Image(is));
            }

            Stage stage = (Stage) btnComprar.getScene().getWindow();
            Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1100;
            double h = current != null ? current.getHeight() : 620;

            stage.setScene(new Scene(root, w, h));
            stage.setMaximized(true);
            stage.setTitle("Selecciona tus asientos");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            a.setHeaderText("Error abriendo Asientos");
            a.setContentText(String.valueOf(ex));
            a.showAndWait();
        }
    }

    private void buildDayTabsFrom(List<FuncionDisponibleDTO> funciones) {
        if (dayTabs == null) return;
        dayTabs.getChildren().clear();
        if (funciones == null || funciones.isEmpty()) return;

        List<java.time.LocalDate> fechas = funciones.stream()
                .map(FuncionDisponibleDTO::getFecha)
                .distinct()
                .sorted()
                .limit(5)
                .toList();

        int idx = 0;
        for (java.time.LocalDate d : fechas) {
            String text = d.getMonth().toString().substring(0,3).toUpperCase() + " " + d.getDayOfMonth();
            Button tab = new Button(text);
            tab.getStyleClass().add("day-tab");
            final int tabIndex = idx;
            tab.setOnAction(e -> {
                setSelectedDay(d);
                cargarFunciones(); // Solo actualizar funciones, no toda la película
                updateDayTabStyles(tabIndex);
            });
            dayTabs.getChildren().add(tab);
            idx++;
        }

        if (getSelectedDay() == null && !fechas.isEmpty()) {
            setSelectedDay(fechas.get(0));
            updateDayTabStyles(0);
        }
    }

    private void updateDayTabStyles(int selectedIdx) {
        if (dayTabs == null) return;
        for (int i = 0; i < dayTabs.getChildren().size(); i++) {
            var n = dayTabs.getChildren().get(i);
            if (n instanceof Button b) {
                if (i == selectedIdx) b.setStyle("-fx-background-color:#8A2F24;-fx-text-fill:white;-fx-background-radius:10;" );
                else b.setStyle("-fx-background-color:transparent;-fx-border-color:#ffffff22;-fx-text-fill:#ddd;-fx-background-radius:10;-fx-border-radius:10;");
            }
        }
    }

    private java.time.LocalDate selectedDay;
    private void setSelectedDay(java.time.LocalDate d) { this.selectedDay = d; }
    private java.time.LocalDate getSelectedDay() { return this.selectedDay; }

    private boolean isEmbedded() {
        try {
            return host != null
                    && btnComprar != null
                    && host.isSameScene(btnComprar);
        } catch (Throwable t) {
            return false;
        }
    }

    private void cargarTrailers(int peliculaId) {
        try {
            if (trailerContainer == null) return;
            
            // Limpiar contenido anterior
            trailerContainer.getChildren().clear();
            
            // Obtener trailers de la base de datos
            DatabaseConfig db = new DatabaseConfig();
            PeliculaTrailerRepository trailerRepo = new PeliculaTrailerRepositoryJdbc(db);
            List<PeliculaTrailer> trailers = trailerRepo.obtenerTrailersPorPelicula(peliculaId);
            
            String trailerUrl = null;
            
            if (!trailers.isEmpty()) {
                // Usar solo el primer trailer de la lista
                trailerUrl = trailers.get(0).getUrl();
            } else if (pelicula != null && pelicula.getTrailer() != null && !pelicula.getTrailer().trim().isEmpty()) {
                // Si no hay trailers en la tabla PELICULA_TRAILER, usar el trailer principal de la película
                trailerUrl = pelicula.getTrailer();
            }
            
            if (trailerUrl != null && !trailerUrl.trim().isEmpty()) {
                cargarTrailerEnWebView(trailerUrl);
            } else {
                mostrarMensajeNoTrailer();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeNoTrailer();
        }
    }
    
    private void cargarTrailerEnWebView(String url) {
        try {
            if (trailerContainer == null) return;
            
            trailerContainer.getChildren().clear();
            
            // WebView súper simple - configuración mínima
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.setPrefSize(600, 360);
            
            // Convertir URL y cargar directamente
            String embedUrl = convertirUrlYouTubeAEmbed(url);
            webView.getEngine().load(embedUrl);
            
            trailerContainer.getChildren().add(webView);
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensajeError();
        }
    }
    
    private void mostrarMensajeNoTrailer() {
        trailerContainer.getChildren().clear();
        Label lblNoTrailer = new Label("No hay trailer disponible para esta película");
        lblNoTrailer.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
        trailerContainer.getChildren().add(lblNoTrailer);
    }
    
    private void mostrarMensajeError() {
        trailerContainer.getChildren().clear();
        Label lblError = new Label("Error al cargar el trailer");
        lblError.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14;");
        trailerContainer.getChildren().add(lblError);
    }
    
    private String convertirUrlYouTubeAEmbed(String url) {
        if (url == null || url.trim().isEmpty()) return url;
        
        try {
            // Patrones comunes de URL de YouTube
            if (url.contains("youtube.com/watch?v=")) {
                String videoId = url.substring(url.indexOf("v=") + 2);
                if (videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
                return "https://www.youtube.com/embed/" + videoId + "?controls=1&modestbranding=1&rel=0&enablejsapi=1";
            } else if (url.contains("youtu.be/")) {
                String videoId = url.substring(url.lastIndexOf("/") + 1);
                if (videoId.contains("?")) {
                    videoId = videoId.substring(0, videoId.indexOf("?"));
                }
                return "https://www.youtube.com/embed/" + videoId + "?controls=1&modestbranding=1&rel=0&enablejsapi=1";
            } else if (url.contains("youtube.com/embed/")) {
                // Si ya está en formato embed, agregar parámetros
                if (url.contains("?")) {
                    return url + "&controls=1&modestbranding=1&rel=0&enablejsapi=1";
                } else {
                    return url + "?controls=1&modestbranding=1&rel=0&enablejsapi=1";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return url; // Devolver URL original si no se puede convertir
    }
}