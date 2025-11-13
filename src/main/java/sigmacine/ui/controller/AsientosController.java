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
    private String ciudad = "";
    private String sede   = "";
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

    // --- RESTAURADOS: setters usados por otros controladores ---
    public void setFuncionId(Long funcionId) { this.funcionId = funcionId; }

    public void setPoster(Image poster) {
        this.poster = poster;
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        if (lblTitulo != null) lblTitulo.setText(titulo);
    }

    public void setHora(String hora) {
        this.hora = hora;
        if (lblHoraPill != null) lblHoraPill.setText(hora);
    }

    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public void setSede(String sede) { this.sede = sede; }

    // ------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) barraController.marcarBotonActivo("asientos");

        if (txtBuscar != null)
            txtBuscar.setOnKeyPressed(ev -> { if (ev.getCode() == KeyCode.ENTER) doSearch(txtBuscar.getText()); });

        if (ocupados.isEmpty()) {
            for (int c = 3; c <= columnas; c += 2) ocupados.add("D" + c);
            for (int c = 2; c <= columnas; c += 3) ocupados.add("E" + c);
            for (int c = 1; c <= columnas; c += 4) ocupados.add("F" + c);
        }
        if (accesibles.isEmpty()) accesibles.addAll(Arrays.asList("A5", "A6", "A7", "A8"));

        if (lblTitulo != null) lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
    }

    private void poblarGrilla() {
        gridSala.getChildren().clear();
        seatByCode.clear();
        seleccion.clear();

        // Diseño irregular para que se vea más real
        // Algunas filas con menos asientos y espacios de pasillo
        int[][] filasConfig = {
            {2, 10}, // fila A
            {1, 11}, // fila B
            {0, 12}, // fila C
            {0, 12}, // fila D
            {1, 11}, // fila E
            {2, 10}, // fila F
            {2, 10}, // fila G
            {3, 9}   // fila H
        };

        for (int f = 0; f < filas; f++) {
            int inicio = filasConfig[f][0];
            int fin = filasConfig[f][1];

            for (int c = inicio; c < fin; c++) {
                String code = code(f, c - inicio + 1);
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
        return fila + String.valueOf(colIdx);
    }

    private void actualizarResumen() {
        int n = seleccion.size();
        if (lblResumen != null)
            lblResumen.setText(n + (n == 1 ? " Silla seleccionada" : " Sillas seleccionadas"));
        if (btnContinuar != null)
            btnContinuar.setDisable(n == 0);
    }

    // --- resto de métodos (búsqueda, carrito, navegación, etc.) siguen igual ---
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
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void setFuncion(String titulo2, String hora2, Set<String> ocupados2, Set<String> accesibles2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setFuncion'");
    }
}
