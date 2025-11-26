package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

    @FXML private TextField txtBuscar;

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
    private String ciudad = "";
    private String sede = "";
    private Image poster;
    private Long funcionId;

    private final sigmacine.aplicacion.service.CarritoService carrito = sigmacine.aplicacion.service.CarritoService.getInstance();
    private final List<sigmacine.aplicacion.data.CompraProductoDTO> asientoItems = new ArrayList<>();
    private static final BigDecimal PRECIO_ASIENTO = new BigDecimal("12.00");

    private Stage cartStage;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("asientos");
        }
        
        if (txtBuscar != null) {
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });
        }

        // Demo si nadie setea función
        if (ocupados.isEmpty()) {
            for (int c = 3; c <= columnas; c += 2) ocupados.add("D" + c);
            for (int c = 2; c <= columnas; c += 3) ocupados.add("E" + c);
            for (int c = 1; c <= columnas; c += 4) ocupados.add("F" + c);
        }
        if (accesibles.isEmpty()) {
            // Cambiado a A2..A5 por defecto
            accesibles.addAll(Arrays.asList("A2","A3","A4","A5"));
        }
        if (lblTitulo != null)   lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
    }

    @FXML private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

    @FXML
    private void onCarritoTop() {
        toggleCartPopup();
    }

    @FXML
    private void onSigmaCardTop() {
        try {
            javafx.stage.Stage stage = null;
            try { stage = gridSala != null && gridSala.getScene() != null ? (javafx.stage.Stage) gridSala.getScene().getWindow() : null; } catch (Exception ignore) {}
            if (stage != null) SigmaCardController.openAsScene(stage);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

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
        if (coordinador != null) { 
            coordinador.mostrarLogin(); 
            return; 
        }
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

    private void poblarGrilla() {
        gridSala.getChildren().clear();
        seatByCode.clear();
        seleccion.clear();

        // Centrar la grilla
        gridSala.setAlignment(Pos.CENTER);

        // Calculamos el máximo de columnas visual (10 para E-H)
        int maxCols = 10;

        // Numeración superior (fila 0 en GridPane). Dejamos la columna 0 para letras.
        for (int i = 0; i < maxCols; i++) {
            Label lblNum = new Label(String.valueOf(i + 1));
            lblNum.getStyleClass().add("seat-number");
            gridSala.add(lblNum, i + 1, 0);
        }

        // Recorremos filas 0..filas-1 (A..H), pero colocamos en GridPane en la fila +1
        for (int f = 0; f < filas; f++) {

            int colsThisRow = getColumnCountForRowIdx(f);
            // letra
            char filaChar = (char) ('A' + f);

            // Label con la letra en la columna 0, fila f+1
            Label lblLetra = new Label(String.valueOf(filaChar));
            lblLetra.getStyleClass().add("seat-letter");
            gridSala.add(lblLetra, 0, f + 1);

            for (int c = 0; c < colsThisRow; c++) {
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
                    });
                }

                seatByCode.put(code, seat);
                // añadimos +1 en columna para dejar la columna 0 a las letras
                gridSala.add(seat, c + 1, f + 1);
            }
        }
    }

    private int getColumnCountForRowIdx(int filaIdxZeroBased) {
        // filaIdxZeroBased: 0 -> A, 1 -> B, ...
        if (filaIdxZeroBased == 0) return 6;      // A
        if (filaIdxZeroBased >= 1 && filaIdxZeroBased <= 3) return 8; // B,C,D
        return 10; // E..H
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
        if (!asientoItems.isEmpty()) {
            for (var dto : asientoItems) {
                carrito.removeItem(dto);
            }
            asientoItems.clear();
        }
        
        if (seleccion.isEmpty()) {
            return;
        }
        
        for (String code : seleccion.stream().sorted().toList()) {
            StringBuilder nombreBuilder = new StringBuilder();
            nombreBuilder.append("Asiento ").append(code);
            
            if (titulo != null && !titulo.isBlank()) {
                nombreBuilder.append(" - ").append(titulo);
            }
            
            if (sede != null && !sede.isBlank()) {
                nombreBuilder.append(" - ").append(sede);
            } else if (ciudad != null && !ciudad.isBlank()) {
                nombreBuilder.append(" - ").append(ciudad);
            }
            
            if (hora != null && !hora.isBlank()) {
                nombreBuilder.append(" (").append(hora).append(")");
            }
            
            String nombre = nombreBuilder.toString();
            var dto = new sigmacine.aplicacion.data.CompraProductoDTO(null, this.funcionId, nombre, 1, PRECIO_ASIENTO, code);
            carrito.addItem(dto);
            asientoItems.add(dto);
        }
        
        if (!seleccion.isEmpty()) {
            javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Asientos añadidos al carrito");
            confirmacion.setHeaderText("¡Asientos confirmados!");
            
            String mensaje = seleccion.size() == 1 
                ? "Se añadió 1 asiento al carrito:\n" + seleccion.iterator().next()
                : "Se añadieron " + seleccion.size() + " asientos al carrito:\n" + 
                  String.join(", ", seleccion.stream().sorted().toList());
            
            confirmacion.setContentText(mensaje);
            
            try {
                confirmacion.getDialogPane().getStylesheets().add(
                    getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
                );
            } catch (Exception ignore) {}
            
            javafx.scene.control.ButtonType btnIrConfiteria = new javafx.scene.control.ButtonType("Ir a la confitería");
            javafx.scene.control.ButtonType btnIrCarrito = new javafx.scene.control.ButtonType("Ir al carrito");
            javafx.scene.control.ButtonType btnCerrar = new javafx.scene.control.ButtonType("", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmacion.getButtonTypes().setAll(btnIrConfiteria, btnIrCarrito, btnCerrar);
            
            javafx.application.Platform.runLater(() -> {
                confirmacion.getDialogPane().lookupButton(btnCerrar).setVisible(false);
                confirmacion.getDialogPane().lookupButton(btnCerrar).setManaged(false);
            });
            
            var resultado = confirmacion.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == btnIrCarrito) {
                try {
                    openCartPopup();
                } catch (Exception ex) {
                    System.err.println("Error abriendo carrito: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else if (resultado.isPresent() && resultado.get() == btnIrConfiteria) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/menu.fxml"));
                    javafx.scene.Parent root = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnContinuar.getScene().getWindow();
                    stage.setTitle("Sigma Cine - Confitería");
                    javafx.scene.Scene current = stage.getScene();
                    double w = current != null ? current.getWidth() : 900;
                    double h = current != null ? current.getHeight() : 600;
                    stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                    stage.setMaximized(true);
                    stage.show();
                } catch (Exception ex) {
                    System.err.println("Error navegando a confitería: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public void setFuncion(String titulo,
                        String hora,
                        java.util.Set<String> ocupados,
                        java.util.Set<String> accesibles,
                        Long funcionId) {
        setFuncion(titulo, hora, ocupados, accesibles, funcionId, "", "");
    }

    public void setFuncion(String titulo,
                        String hora,
                        java.util.Set<String> ocupados,
                        java.util.Set<String> accesibles,
                        Long funcionId,
                        String ciudad,
                        String sede) {
        if (titulo != null) this.titulo = titulo;
        if (hora   != null) this.hora   = hora;
        if (ciudad != null) this.ciudad = ciudad;
        if (sede   != null) this.sede   = sede;
        this.funcionId = funcionId;

        this.ocupados.clear();
        if (ocupados != null) this.ocupados.addAll(ocupados);

        this.accesibles.clear();
        if (accesibles != null && !accesibles.isEmpty()) {
            this.accesibles.addAll(shiftAccesiblesToFirstRowPlus2(accesibles));
        } else {
            this.accesibles.addAll(Arrays.asList("A2","A3","A4","A5"));
        }

        if (lblTitulo != null)   lblTitulo.setText(this.titulo);
        if (lblHoraPill != null) lblHoraPill.setText(this.hora);
        if (gridSala != null) { 
            poblarGrilla(); 
            sincronizarConCarritoExistente();
            actualizarResumen(); 
        }

        if ((this.poster == null || imgPoster == null || imgPoster.getImage() == null) && this.titulo != null && !this.titulo.isBlank()) {
            try {
                var db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
                var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
                var resultados = repo.buscarPorTitulo(this.titulo);
                if (resultados != null && !resultados.isEmpty()) {
                    var p = resultados.get(0);
                    String posterRef = p.getPosterUrl();
                    if (posterRef != null && !posterRef.isBlank()) {
                        try {
                            java.io.InputStream is = getClass().getResourceAsStream(posterRef.startsWith("/") ? posterRef : ("/" + posterRef));
                            javafx.scene.image.Image img = null;
                            if (is != null) img = new javafx.scene.image.Image(is);
                            else {
                                java.net.URL res = getClass().getResource("/Images/" + posterRef);
                                if (res != null) img = new javafx.scene.image.Image(res.toExternalForm(), false);
                                else {
                                    java.io.File f = new java.io.File(posterRef);
                                    if (f.exists()) img = new javafx.scene.image.Image(f.toURI().toString(), false);
                                }
                            }
                            if (img != null) setPoster(img);
                        } catch (Exception ignore) {}
                    }
                }
            } catch (Exception ignore) {}
        }

        tryFindPosterInScene();

        if ((imgPoster == null || imgPoster.getImage() == null) && this.funcionId != null) {
            try {
                var db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
                try (var cn = db.getConnection();
                    var ps = cn.prepareStatement("SELECT PELICULA_ID FROM FUNCION WHERE ID = ?")) {
                    ps.setLong(1, this.funcionId);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long peliculaId = rs.getLong("PELICULA_ID");
                            var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
                            var todas = repo.buscarTodas();
                            for (var p : todas) {
                                try {
                                    if (p.getId() == peliculaId) {
                                        String posterRef = p.getPosterUrl();
                                        if (posterRef != null && !posterRef.isBlank()) {
                                            java.net.URL res = getClass().getResource((posterRef.startsWith("/") ? posterRef : ("/" + posterRef)));
                                            javafx.scene.image.Image img = null;
                                            if (res != null) img = new javafx.scene.image.Image(res.toExternalForm(), false);
                                            else {
                                                java.net.URL res2 = getClass().getResource("/Images/" + posterRef);
                                                if (res2 != null) img = new javafx.scene.image.Image(res2.toExternalForm(), false);
                                                else {
                                                    java.io.File f = new java.io.File(posterRef);
                                                    if (f.exists()) img = new javafx.scene.image.Image(f.toURI().toString(), false);
                                                }
                                            }
                                            if (img != null) imgPoster.setImage(img);
                                        }
                                        if (lblTitulo != null && (lblTitulo.getText() == null || lblTitulo.getText().equals("Película"))) {
                                            lblTitulo.setText(p.getTitulo() != null ? p.getTitulo() : lblTitulo.getText());
                                        }
                                        break;
                                    }
                                } catch (Exception ignore) {}
                            }
                        }
                    }
                }
            } catch (Exception ignore) {}
        }
    }

    private void tryFindPosterInScene() {
        try {
            if (imgPoster == null) return;
            if (imgPoster.getImage() != null) return;
            javafx.scene.Scene sc = null;
            try { sc = gridSala != null ? gridSala.getScene() : null; } catch (Exception ignore) {}
            if (sc == null) return;

            String[] candidateIds = new String[] {"#imgPoster", "#imgCard1", "#imgCard2", "#imgCard3", "#imgCard4", "#imgPublicidad"};
            for (String id : candidateIds) {
                try {
                    javafx.scene.Node n = sc.lookup(id);
                    if (n instanceof javafx.scene.image.ImageView iv) {
                        javafx.scene.image.Image i = iv.getImage();
                        if (i != null) { imgPoster.setImage(i); return; }
                    }
                } catch (Exception ignore) {}
            }

            String[] titleIds = new String[] {"#lblTituloPelicula", "#lblTitulo"};
            for (String id : titleIds) {
                try {
                    javafx.scene.Node n = sc.lookup(id);
                    if (n instanceof javafx.scene.control.Label l) {
                        String t = l.getText();
                        if (t != null && !t.isBlank() && (lblTitulo == null || lblTitulo.getText() == null || lblTitulo.getText().equals("Película"))) {
                            if (lblTitulo != null) lblTitulo.setText(t);
                            return;
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
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
        if (seleccion.isEmpty()) {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alerta.setTitle("Selección requerida");
            alerta.setHeaderText("No hay asientos seleccionados");
            alerta.setContentText("Por favor selecciona al menos un asiento antes de continuar.");
            alerta.showAndWait();
            return;
        }
        
        sincronizarAsientosConCarrito();
        
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
                cartStage.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                    if (ev.getCode() == KeyCode.ESCAPE) cartStage.close();
                });
            }
            if (gridSala != null && gridSala.getScene() != null && gridSala.getScene().getWindow() != null) {
                javafx.stage.Window owner = gridSala.getScene().getWindow();
                cartStage.setX(owner.getX() + owner.getWidth() - 650);
                cartStage.setY(owner.getY() + 100);
            }
            cartStage.show();
            cartStage.toFront();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir el carrito: " + ex.getMessage()).showAndWait();
        }
    }

    private void sincronizarConCarritoExistente() {
        seleccion.clear();
        asientoItems.clear();
        
        var itemsCarrito = carrito.getItems();
        for (var item : itemsCarrito) {
            if (item.getFuncionId() != null && 
                item.getAsiento() != null && 
                item.getFuncionId().equals(this.funcionId)) {
                
                String codigoAsiento = item.getAsiento();
                
                seleccion.add(codigoAsiento);
                asientoItems.add(item);
                
                ToggleButton boton = seatByCode.get(codigoAsiento);
                if (boton != null) {
                    boton.setSelected(true);
                    setSeatState(boton, SeatState.SELECTED);
                }
            }
        }
    }
}
