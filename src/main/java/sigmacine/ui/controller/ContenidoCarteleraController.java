package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.dominio.repository.FuncionRepository;
import sigmacine.aplicacion.service.SillaService;
import sigmacine.infraestructura.persistencia.jdbc.SillaRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.aplicacion.session.Session;
import sigmacine.infraestructura.persistencia.jdbc.FuncionRepositoryJdbc;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
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
    
    // Variables para reproducción de video
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    
    public void setHost(ClienteController host) { this.host = host; }
    
    private void limpiarReproductor() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (mediaView != null) {
            mediaView = null;
        }
    }

    @FXML
    private void initialize() {
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

            btnComprar.setOnAction(e -> {
                onComprarTickets();
            });
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
        // Limpiar reproductor anterior
        limpiarReproductor();
        
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

        cargarTrailers(p.getId());

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
                buildDayTabsFrom(funciones);
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
        // Para evitar duplicados de hora, usaremos un Map con clave compuesta
        Map<String, Map<String, Map<String, FuncionDisponibleDTO>>> porCiudad = new LinkedHashMap<>();
        for (FuncionDisponibleDTO f : funciones) {
            String ciudad = f.getCiudad();
            String sede = f.getSede();
            String horaKey = f.getHora().toString(); // Usar hora como clave única
            
            porCiudad.computeIfAbsent(ciudad, k -> new LinkedHashMap<>())
                    .computeIfAbsent(sede, k -> new LinkedHashMap<>())
                    .put(horaKey, f); // put sobrescribe si ya existe, manteniendo solo una función por hora
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

                Label lblHora = new Label("HORA");
                lblHora.setStyle("-fx-text-fill:#aaaaaa;-fx-font-size:12;-fx-font-weight:bold;");
                sedeBox.getChildren().add(lblHora);

                javafx.scene.layout.FlowPane fila = new javafx.scene.layout.FlowPane();
                fila.setHgap(8);
                fila.setVgap(4);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-background-color:transparent;");

                // ordenar por hora y tomar solo los valores únicos
                List<FuncionDisponibleDTO> ordenadas = sedeEntry.getValue().values().stream()
                        .sorted(java.util.Comparator.comparing(FuncionDisponibleDTO::getHora))
                        .collect(Collectors.toList());
                        
                for (FuncionDisponibleDTO f : ordenadas) {
                    String pillText = fmt.format(f.getHora()).toLowerCase();
                    Button b = new Button(pillText);
                    b.setStyle("-fx-background-color:transparent;-fx-border-color:#ffffff66;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:4 12 4 12;");
                    b.setOnAction(e -> seleccionarFuncion(f, b));
                    fila.getChildren().add(b);
                }
                sedeBox.getChildren().add(fila);
                ciudadBox.getChildren().add(sedeBox);
            }

            HBox wrapCity = new HBox(ciudadBox);
            wrapCity.setAlignment(Pos.CENTER);
            panelFunciones.getChildren().add(wrapCity);
        }
    }

    private String selectedFuncionText;
    private Long selectedFuncionId;
    private FuncionDisponibleDTO selectedFuncion; // Objeto completo de la función seleccionada
    private Button selectedHoraButton;
    
    private void seleccionarFuncion(FuncionDisponibleDTO f, Button sourceBtn) {
        this.selectedFuncionId = f.getFuncionId();
        this.selectedFuncion = f; // Guardar el objeto completo
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
            // Verificar si el usuario ha iniciado sesión usando Session global
            boolean isLoggedIn = sigmacine.aplicacion.session.Session.isLoggedIn();
            
            if (!isLoggedIn) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Iniciar Sesión Requerido");
                alert.setHeaderText("Debe iniciar sesión");
                alert.setContentText("Para comprar boletos debe iniciar sesión primero. ¿Desea ir a la pantalla de login?");
                
                // Agregar botones personalizados
                javafx.scene.control.ButtonType btnIrLogin = new javafx.scene.control.ButtonType("Ir a Login");
                javafx.scene.control.ButtonType btnCancelar = new javafx.scene.control.ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(btnIrLogin, btnCancelar);
                
                var result = alert.showAndWait();
                
                if (result.isPresent() && result.get() == btnIrLogin) {
                    // Usar el mismo patrón del botón iniciar sesión que ya funciona
                    try {
                        if (this.coordinador != null) {
                            // En lugar de usar coordinador.mostrarLogin() directamente, crearemos nuestro propio diálogo
                            // para tener control total del callback
                        }
                        
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
                        Parent root = loader.load();
                        Object ctrl = loader.getController();
                        Stage dialog = new Stage();
                        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        dialog.initOwner(btnComprar.getScene().getWindow());
                        
                        if (ctrl instanceof LoginController) {
                            LoginController lc = (LoginController) ctrl;
                            
                            // SÍ configurar dependencias para que funcione el login
                            try {
                                ControladorControlador global = ControladorControlador.getInstance();
                                if (global != null) {
                                    lc.setCoordinador(global);
                                    try {
                                        sigmacine.aplicacion.facade.AuthFacade af = global.getAuthFacade();
                                        if (af != null) lc.setAuthFacade(af);
                                    } catch (Throwable ignore) {}
                                }
                            } catch (Throwable ignore) {}

                            // Configurar callback personalizado que anula cualquier otro comportamiento
                            lc.setOnSuccess(() -> {
                                try { 
                                    dialog.close(); 
                                } catch (Exception ignore) {}
                                
                                // Ejecutar en el siguiente ciclo del hilo de JavaFX para asegurar que el diálogo se cierre
                                Platform.runLater(() -> {
                                    try {
                                        continuarConCompra();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            });
                        }
                        dialog.setScene(new javafx.scene.Scene(root));
                        dialog.setTitle("Iniciar Sesión - Sigma Cine");
                        dialog.showAndWait();
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return;
            }
            
            continuarConCompra();
        } catch (Exception ex) {
            ex.printStackTrace();
            var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            a.setHeaderText("Error abriendo Asientos");
            a.setContentText(String.valueOf(ex));
            a.showAndWait();
        }
    }

    private void continuarConCompra() {
        try {
            // Debe haber una función seleccionada
            String seleccion = selectedFuncionText;
            
            if (seleccion == null || seleccion.isBlank()) {
                var a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                a.setHeaderText("Selecciona una hora");
                a.setContentText("Primero elige una función (sede/hora) antes de continuar.");
                a.showAndWait();
                return;
            }
            if (selectedFuncionId == null) {

            }
            if (isEmbedded()) {
                String titulo = "Película"; // fallback por defecto
                
                // Intentar obtener el título de múltiples fuentes
                if (pelicula != null && pelicula.getTitulo() != null && !pelicula.getTitulo().isBlank()) {
                    titulo = pelicula.getTitulo(); // Primer intento: del objeto película
                } else if (lblTituloPelicula != null && lblTituloPelicula.getText() != null && !lblTituloPelicula.getText().isBlank()) {
                    titulo = lblTituloPelicula.getText(); // Segundo intento: del label
                }
                
                String hora = seleccion;

                // Obtener asientos ocupados y accesibles desde la base de datos
                Set<String> ocupados = obtenerAsientosOcupados(selectedFuncionId);
                Set<String> accesibles = obtenerAsientosAccesibles(selectedFuncionId);

                host.mostrarAsientos(titulo, hora, ocupados, accesibles);
                return;
            }

            String titulo = "Película"; // fallback por defecto
            
            // Intentar obtener el título de múltiples fuentes
            if (pelicula != null && pelicula.getTitulo() != null && !pelicula.getTitulo().isBlank()) {
                titulo = pelicula.getTitulo(); // Primer intento: del objeto película
            } else if (lblTituloPelicula != null && lblTituloPelicula.getText() != null && !lblTituloPelicula.getText().isBlank()) {
                titulo = lblTituloPelicula.getText(); // Segundo intento: del label
            }
            
            String hora = seleccion;

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
            
            // Obtener asientos ocupados y accesibles desde la base de datos
            Set<String> ocupados = obtenerAsientosOcupados(selectedFuncionId);
            Set<String> accesibles = obtenerAsientosAccesibles(selectedFuncionId);
            
            // Obtener información de ciudad y sede de la función seleccionada
            String ciudad = selectedFuncion != null ? selectedFuncion.getCiudad() : "";
            String sede = selectedFuncion != null ? selectedFuncion.getSede() : "";
            
            // Pasar información completa incluyendo ciudad y sede
            ctrl.setFuncion(titulo, hora, ocupados, accesibles);

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
            if (trailerContainer == null) {
                return;
            }
            
            trailerContainer.getChildren().clear();
            
            // Obtener URL del trailer directamente de la película
            String trailerUrl = null;
            if (pelicula != null && pelicula.getTrailer() != null && !pelicula.getTrailer().trim().isEmpty()) {
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
            
            // Detener reproductor anterior si existe
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            
            // Verificar que la URL no sea nula o vacía
            if (url == null || url.trim().isEmpty()) {
                mostrarMensajeVideoNoDisponible();
                return;
            }
            
            // Intentar encontrar el recurso
            java.net.URL resourceUrl = getClass().getResource(url);
            
            if (resourceUrl == null) {
                mostrarMensajeVideoNoDisponible();
                return;
            }
            
            // Crear MediaPlayer con el archivo local
            Media media = new Media(resourceUrl.toExternalForm());
            
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            
            // Configurar el MediaPlayer para que siempre inicie desde el principio
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.seek(javafx.util.Duration.ZERO);
                mediaPlayer.stop(); // Asegurar que esté detenido al cargar
            });
            
            // Configurar el MediaView
            mediaView.setFitWidth(600);
            mediaView.setFitHeight(360);
            mediaView.setPreserveRatio(true);
            
            // Crear controles personalizados
            VBox videoContainer = crearVideoConControles();
            
            trailerContainer.getChildren().add(videoContainer);
            
        } catch (Exception e) {
            mostrarMensajeError();
        }
    }
    
    private void mostrarMensajeNoTrailer() {
        trailerContainer.getChildren().clear();
        Label lblNoTrailer = new Label("No hay trailer disponible para esta película");
        lblNoTrailer.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
        trailerContainer.getChildren().add(lblNoTrailer);
    }
    
    private VBox crearVideoConControles() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-padding: 15;");
        
        // Añadir el MediaView
        container.getChildren().add(mediaView);
        
        // Crear controles
        HBox controles = new HBox(15);
        controles.setAlignment(Pos.CENTER);
        
        Button btnPlay = new Button("▶");
        Button btnPause = new Button("⏸");
        Button btnStop = new Button("⏹");
        
        // Estilo de los botones
        String estiloBoton = "-fx-background-color: #8A2F24; -fx-text-fill: white; " +
                           "-fx-font-size: 12; -fx-background-radius: 5; " +
                           "-fx-padding: 8 15 8 15; -fx-cursor: hand;";
        
        btnPlay.setStyle(estiloBoton);
        btnPause.setStyle(estiloBoton);
        btnStop.setStyle(estiloBoton);
        
        // Eventos de los botones
        btnPlay.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        });
        
        btnPause.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
        
        btnStop.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.seek(javafx.util.Duration.ZERO); // Volver al inicio
            }
        });
        
        controles.getChildren().addAll(btnPlay, btnPause, btnStop);
        container.getChildren().add(controles);
        
        return container;
    }
    
    private void mostrarMensajeVideoNoDisponible() {
        trailerContainer.getChildren().clear();
        
        Label lblError = new Label("Video no disponible");
        lblError.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        
        Label lblInfo = new Label("El trailer para esta película no está disponible localmente.");
        lblInfo.setStyle("-fx-text-fill: #8A2F24; -fx-font-size: 12; -fx-text-alignment: center;");
        
        VBox mensajeBox = new VBox(10);
        mensajeBox.setAlignment(Pos.CENTER);
        mensajeBox.getChildren().addAll(lblError, lblInfo);
        
        trailerContainer.getChildren().add(mensajeBox);
    }
    
    private void mostrarMensajeError() {
        trailerContainer.getChildren().clear();
        Label lblError = new Label("Error al cargar el trailer");
        lblError.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14;");
        trailerContainer.getChildren().add(lblError);
    }
    
    /**
     * Obtiene los asientos ocupados para una función específica desde la base de datos.
     */
    private Set<String> obtenerAsientosOcupados(Long funcionId) {
        if (funcionId == null) {
            // Datos por defecto si no hay función seleccionada
            return Set.of("B3", "B4", "C7", "E2", "F8");
        }
        
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            SillaRepositoryJdbc sillaRepo = new SillaRepositoryJdbc(dbConfig);
            SillaService sillaService = new SillaService(sillaRepo);
            return sillaService.obtenerAsientosOcupados(funcionId);
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, usar datos por defecto para no bloquear la UI
            return Set.of("B3", "B4", "C7", "E2", "F8");
        }
    }
    
    /**
     * Obtiene los asientos accesibles para una función específica desde la base de datos.
     */
    private Set<String> obtenerAsientosAccesibles(Long funcionId) {
        if (funcionId == null) {
            // Datos por defecto si no hay función seleccionada
            return Set.of("E3", "E4", "E5", "E6");
        }
        
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            SillaRepositoryJdbc sillaRepo = new SillaRepositoryJdbc(dbConfig);
            SillaService sillaService = new SillaService(sillaRepo);
            return sillaService.obtenerAsientosAccesibles(funcionId);
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, usar datos por defecto
            return Set.of("E3", "E4", "E5", "E6");
        }
    }
}
