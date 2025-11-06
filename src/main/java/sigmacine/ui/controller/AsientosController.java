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
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Solo mantener el Singleton para marcar la página activa
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
            accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));
        }
        if (lblTitulo != null)   lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
    }

    @FXML private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }

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
        // Implementar lógica para continuar con la compra
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
}