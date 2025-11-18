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

public class AsientosController implements Initializable {

    @FXML private GridPane gridSala;
    @FXML private Label lblResumen;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTitulo;
    @FXML private Label lblHoraPill;
    @FXML private ImageView imgPoster;
    @FXML private Button btnContinuar;

    private final int FILAS = 8;

    // FILAS REALISTAS
    private final int[][] configFilas = {
            {3, 6},  // A
            {2, 8},  // B
            {2, 8},  // C
            {2, 8},  // D
            {1,10},  // E
            {1,10},  // F
            {1,10},  // G
            {1,10}   // H
    };

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

    private final sigmacine.aplicacion.service.CarritoService carrito =
            sigmacine.aplicacion.service.CarritoService.getInstance();

    private final List<sigmacine.aplicacion.data.CompraProductoDTO> asientoItems =
            new ArrayList<>();

    private static final BigDecimal PRECIO_ASIENTO = new BigDecimal("12.00");

    private Stage cartStage;
    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public void setCoordinador(ControladorControlador c) { this.coordinador = c; }
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        BarraController barraController = BarraController.getInstance();
        if (barraController != null)
            barraController.marcarBotonActivo("asientos");

        if (txtBuscar != null)
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER)
                    doSearch(txtBuscar.getText());
            });

        if (ocupados.isEmpty()) {
            for (int c = 3; c <= 12; c += 2) ocupados.add("D" + c);
            for (int c = 2; c <= 12; c += 3) ocupados.add("E" + c);
            for (int c = 1; c <= 12; c += 4) ocupados.add("F" + c);
        }

        if (accesibles.isEmpty())
            accesibles.addAll(Arrays.asList("A5", "A6", "A7", "A8"));

        if (lblTitulo != null) lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);

        poblarGrilla();
        actualizarResumen();
    }

    @FXML private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }
    @FXML private void onCarritoTop() { toggleCartPopup(); }

    @FXML
    private void onSigmaCardTop() {
        try {
            Stage stage = null;
            try { stage = gridSala != null ? (Stage) gridSala.getScene().getWindow() : null; } catch (Exception ignore) {}
            if (stage != null) SigmaCardController.openAsScene(stage);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ---------------------------------------------------------------
    //       GENERA LA GRILLA DE ASIENTOS (REALISTA + CENTRADA)
    // ---------------------------------------------------------------
    private void poblarGrilla() {

        gridSala.getChildren().clear();
        seatByCode.clear();
        seleccion.clear();

        for (int f = 0; f < FILAS; f++) {

            int inicio = configFilas[f][0];
            int cant   = configFilas[f][1];

            for (int i = 0; i < cant; i++) {

                int columnaReal = inicio + i;
                String code = generarCodigo(f, i + 1);

                ToggleButton seat = new ToggleButton();
                seat.setUserData(code);
                seat.setTooltip(new Tooltip(code));
                seat.setFocusTraversable(false);

                seat.getStyleClass().add("seat");

                boolean isAccessible = accesibles.contains(code);
                seat.getProperties().put("accessible", isAccessible);

                if (ocupados.contains(code)) {
                    setSeatState(seat, SeatState.UNAVAILABLE);
                    seat.setDisable(true);
                } else {
                    setSeatState(seat, seleccion.contains(code) ? SeatState.SELECTED : SeatState.AVAILABLE);

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
                gridSala.add(seat, columnaReal, f);
            }
        }
    }

    private enum SeatState { AVAILABLE, SELECTED, UNAVAILABLE }

    private void setSeatState(ToggleButton b, SeatState st) {

        boolean isAccessible = Boolean.TRUE.equals(b.getProperties().get("accessible"));

        b.getStyleClass().removeAll(
                "seat--available", "seat--selected",
                "seat--unavailable", "seat--accessible"
        );

        switch (st) {
            case AVAILABLE -> {
                b.getStyleClass().add("seat--available");
                if (isAccessible) b.getStyleClass().add("seat--accessible");
                b.setSelected(false);
            }
            case SELECTED -> {
                b.getStyleClass().add("seat--selected");
                if (isAccessible) b.getStyleClass().add("seat--accessible");
                b.setSelected(true);
            }
            case UNAVAILABLE -> {
                b.getStyleClass().add("seat--unavailable");
                b.setSelected(false);
            }
        }
    }

    private String generarCodigo(int filaIdx, int colIdx) {
        char fila = (char) ('A' + filaIdx);
        return fila + Integer.toString(colIdx);
    }

    // ---------------------------------------------------------------
    //         RESUMEN Y CONTINUAR
    // ---------------------------------------------------------------
    private void actualizarResumen() {
        int n = seleccion.size();
        if (lblResumen != null)
            lblResumen.setText(n + (n == 1 ? " Silla seleccionada" : " Sillas seleccionadas"));
        if (btnContinuar != null)
            btnContinuar.setDisable(n == 0);
    }

    @FXML
    private void onContinuar() {

        if (seleccion.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Selección requerida");
            alerta.setHeaderText("No hay asientos seleccionados");
            alerta.setContentText("Selecciona al menos una silla para continuar.");
            alerta.showAndWait();
            return;
        }

        sincronizarAsientosConCarrito();
    }

    // ---------------------------------------------------------------
    //                     CARRITO
    // ---------------------------------------------------------------
    private void sincronizarAsientosConCarrito() {

        if (!asientoItems.isEmpty()) {
            for (var dto : asientoItems) carrito.removeItem(dto);
            asientoItems.clear();
        }

        if (!seleccion.isEmpty()) {
            for (String code : seleccion.stream().sorted().toList()) {

                StringBuilder nombre = new StringBuilder("Asiento ").append(code);

                if (titulo != null && !titulo.isBlank()) nombre.append(" - ").append(titulo);
                if (sede != null && !sede.isBlank()) nombre.append(" - ").append(sede);
                else if (ciudad != null && !ciudad.isBlank()) nombre.append(" - ").append(ciudad);
                if (hora != null) nombre.append(" (").append(hora).append(")");

                var dto = new sigmacine.aplicacion.data.CompraProductoDTO(
                        null, this.funcionId, nombre.toString(),
                        1, PRECIO_ASIENTO, code
                );

                carrito.addItem(dto);
                asientoItems.add(dto);
            }
        }
    }

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
                cartStage.initModality(javafx.stage.Modality.NONE);
                cartStage.setResizable(false);
                cartStage.setTitle("Carrito");
                cartStage.setScene(new Scene(root));
                cartStage.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
                        ev -> { if (ev.getCode() == KeyCode.ESCAPE) cartStage.close(); });
            }

            if (gridSala != null && gridSala.getScene() != null) {
                Stage owner = (Stage) gridSala.getScene().getWindow();
                cartStage.setX(owner.getX() + owner.getWidth() - 650);
                cartStage.setY(owner.getY() + 100);
            }

            cartStage.show();
            cartStage.toFront();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    //       FUNCIÓN / POSTER
    // ---------------------------------------------------------------
    public void setFuncion(String titulo,
                           String hora,
                           Set<String> ocupados,
                           Set<String> accesibles,
                           Long funcionId) {

        setFuncion(titulo, hora, ocupados, accesibles, funcionId, "", "");
    }

    public void setFuncion(String titulo,
                           String hora,
                           Set<String> ocupados,
                           Set<String> accesibles,
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
        if (accesibles != null && !accesibles.isEmpty())
            this.accesibles.addAll(accesibles);
        else
            this.accesibles.addAll(Arrays.asList("A5","A6","A7","A8"));

        if (lblTitulo != null) lblTitulo.setText(this.titulo);
        if (lblHoraPill != null) lblHoraPill.setText(this.hora);

        if (gridSala != null) {
            poblarGrilla();
            sincronizarConCarritoExistente();
            actualizarResumen();
        }
    }

    private void sincronizarConCarritoExistente() {

        seleccion.clear();
        asientoItems.clear();

        var itemsCarrito = carrito.getItems();

        for (var item : itemsCarrito) {

            if (item.getFuncionId() != null &&
                item.getAsiento()    != null &&
                item.getFuncionId().equals(this.funcionId)) {

                String code = item.getAsiento();

                seleccion.add(code);
                asientoItems.add(item);

                ToggleButton btn = seatByCode.get(code);
                if (btn != null) {
                    btn.setSelected(true);
                    setSeatState(btn, SeatState.SELECTED);
                }
            }
        }
    }

    // ---------------------------------------------------------------
    //                     BÚSQUEDA
    // ---------------------------------------------------------------
    private void doSearch(String texto) {

        if (texto == null || texto.isBlank()) return;

        try {
            var db  = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto);

            var loader = new javafx.fxml.FXMLLoader(getClass()
                    .getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));

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
            stage.setMaximized(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}