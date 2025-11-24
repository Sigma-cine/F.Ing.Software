package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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

import java.io.InputStream;
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

    // opcional, si decides añadir una ImageView para la convención en tu FXML
    @FXML private ImageView imgConvencion;

    private final int FILAS = 8;

    // (inicio, cantidad)
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

    private String titulo;
    private String hora;
    private String ciudad;
    private String sede;
    private Image poster;                      // si lo envían como Image
    private String posterResourceName = null;   // si lo envían como nombre de archivo o ruta en resources
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

    /**
     * Si el coordinador ya tiene un Image (por ejemplo convertido desde bytes), puede usar este.
     */
    public void setPoster(Image poster) {
        this.poster = poster;
        this.posterResourceName = null; // priorizar Image recibido directamente
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);
    }

    /**
     * Si tu BD devuelve solo el nombre del archivo (ej: "avengers.png") o la ruta en resources
     * usa esto. Intentará varias formas de resolverlo:
     *  - /Images/Posters/<nombreArchivo>
     *  - /Images/<posterResourceName> (por compatibilidad)
     *  - si el valor es una URL (http/https/file:/) creará Image con esa URL
     */
    public void setPosterResource(String nombreArchivo) {
        this.posterResourceName = nombreArchivo;
        cargarPosterSiPosible();
    }

    /**
     * Opcional: si quieres mostrar imagen de convención (sede), llama a este método con
     * el nombre del recurso dentro de resources (por ejemplo "Asientos/convencion.png")
     * o con la ruta relativa en resources.
     */
    public void setConvencionResource(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return;
        // intentar cargar en caso de que el ImageView exista
        if (imgConvencion != null) {
            Image img = resolveResourceImageFlexible(nombreArchivo, "Images/Asientos/");
            if (img != null) imgConvencion.setImage(img);
        }
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

        // No sobreescribir información aquí
        // Solo enlazar eventos y preparar UI

        BarraController barraController = BarraController.getInstance();
        if (barraController != null)
            barraController.marcarBotonActivo("asientos");

        if (txtBuscar != null)
            txtBuscar.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ENTER)
                    doSearch(txtBuscar.getText());
            });

        // intentar cargar poster si el setter se llamó antes de initialize()
        cargarPosterSiPosible();

        // si poster (Image) fue seteado antes, mostrarlo
        if (poster != null && imgPoster != null) {
            imgPoster.setImage(poster);
        }
    }

    @FXML private void onBuscarTop() { doSearch(txtBuscar != null ? txtBuscar.getText() : ""); }
    @FXML private void onCarritoTop() { toggleCartPopup(); }

    @FXML
    private void onSigmaCardTop() {
        try {
            Stage stage = (gridSala.getScene() != null ? (Stage) gridSala.getScene().getWindow() : null);
            if (stage != null) SigmaCardController.openAsScene(stage);
        } catch (Exception ignored) {}
    }


    // =====================================================================================
    //         GRILLA — NUMERACIÓN ARRIBA (1..N) Y LETRAS A LA IZQUIERDA (A..)
    // =====================================================================================
    private void poblarGrilla() {
        if (gridSala == null) return;

        gridSala.getChildren().clear();
        seatByCode.clear();
        // no limpiamos seleccion aquí: sincronizarConCarritoExistente hará ese trabajo

        int maxColumn = 0;
        for (int[] fila : configFilas) {
            int inicio = fila[0];
            int cant   = fila[1];
            maxColumn = Math.max(maxColumn, inicio + cant - 1); // columna máxima real
        }

        // poner números arriba (centrados)
        for (int col = 1; col <= maxColumn; col++) {
            Label lbl = new Label(String.valueOf(col));
            lbl.getStyleClass().add("seat-number");

            // asegurar espacio y alineación centrada
            lbl.setMinWidth(Region.USE_PREF_SIZE);
            lbl.setPrefWidth(48); // ajusta según tu UI
            lbl.setAlignment(Pos.CENTER);

            GridPane.setHalignment(lbl, HPos.CENTER);
            GridPane.setValignment(lbl, VPos.CENTER);

            gridSala.add(lbl, col, 0);
        }

        // filas con letra a la izquierda
        for (int f = 0; f < FILAS; f++) {

            char letra = (char)('A' + f);
            Label lblFila = new Label(String.valueOf(letra));
            lblFila.getStyleClass().add("seat-letter");

            // Tamaño y centrado de la letra de fila
            lblFila.setMinWidth(Region.USE_PREF_SIZE);
            lblFila.setPrefWidth(40); // ajusta si lo necesitas
            lblFila.setAlignment(Pos.CENTER);

            GridPane.setHalignment(lblFila, HPos.CENTER);
            GridPane.setValignment(lblFila, VPos.CENTER);

            gridSala.add(lblFila, 0, f + 1);   // fila inicia en 1

            int inicio = configFilas[f][0];
            int cant   = configFilas[f][1];

            for (int i = 0; i < cant; i++) {

                int columnaReal = inicio + i;
                String code = generarCodigo(f, columnaReal); // usar columna real

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
                    // si es accesible pero ocupado, mantener la clase visual accesible + unavailable
                    if (isAccessible) seat.getStyleClass().add("seat--accessible");
                } else {
                    if (seleccion.contains(code)) {
                        if (isAccessible) setSeatState(seat, SeatState.SELECTED_ACCESSIBLE);
                        else             setSeatState(seat, SeatState.SELECTED);
                        seat.setSelected(true);
                    } else {
                        if (isAccessible) setSeatState(seat, SeatState.AVAILABLE_ACCESSIBLE);
                        else             setSeatState(seat, SeatState.AVAILABLE);
                    }

                    seat.setOnAction(e -> {
                        boolean acc = Boolean.TRUE.equals(seat.getProperties().get("accessible"));
                        if (seat.isSelected()) {
                            seleccion.add(code);
                            if (acc) setSeatState(seat, SeatState.SELECTED_ACCESSIBLE);
                            else     setSeatState(seat, SeatState.SELECTED);
                        } else {
                            seleccion.remove(code);
                            if (acc) setSeatState(seat, SeatState.AVAILABLE_ACCESSIBLE);
                            else     setSeatState(seat, SeatState.AVAILABLE);
                        }
                        actualizarResumen();
                    });
                }

                seatByCode.put(code, seat);

                // centrar el botón en la celda
                GridPane.setHalignment(seat, HPos.CENTER);
                GridPane.setValignment(seat, VPos.CENTER);

                gridSala.add(seat, columnaReal, f + 1);  // +1 porque la fila 0 es la numeración
            }
        }
    }


    private enum SeatState {
        AVAILABLE,
        AVAILABLE_ACCESSIBLE,
        SELECTED,
        SELECTED_ACCESSIBLE,
        UNAVAILABLE
    }

    private void setSeatState(ToggleButton b, SeatState st) {
        boolean acc = Boolean.TRUE.equals(b.getProperties().get("accessible"));

        b.getStyleClass().removeAll(
                "seat--available", "seat--selected", "seat--unavailable",
                "seat--accessible", "seat--accessible-selected"
        );

        switch (st) {
            case AVAILABLE -> {
                b.getStyleClass().add("seat--available");
                if (acc) b.getStyleClass().add("seat--accessible");
            }
            case AVAILABLE_ACCESSIBLE -> {
                b.getStyleClass().add("seat--available");
                b.getStyleClass().add("seat--accessible");
            }
            case SELECTED -> b.getStyleClass().add("seat--selected");
            case SELECTED_ACCESSIBLE -> {
                b.getStyleClass().add("seat--accessible-selected");
                b.getStyleClass().add("seat--accessible");
            }
            case UNAVAILABLE -> b.getStyleClass().add("seat--unavailable");
        }
    }

    private String generarCodigo(int filaIdx, int columnaReal) {
        return (char)('A' + filaIdx) + Integer.toString(columnaReal);
    }

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
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("No hay asientos seleccionados");
            a.setContentText("Selecciona al menos una silla.");
            a.showAndWait();
            return;
        }
        sincronizarAsientosConCarrito();
    }

    private void sincronizarAsientosConCarrito() {
        if (!asientoItems.isEmpty()) {
            for (var dto : asientoItems) carrito.removeItem(dto);
            asientoItems.clear();
        }

        if (!seleccion.isEmpty()) {
            for (String code : seleccion.stream().sorted().toList()) {

                StringBuilder nombre = new StringBuilder("Asiento ")
                        .append(code)
                        .append(" - ").append(titulo);

                if (sede != null && !sede.isBlank()) nombre.append(" - ").append(sede);
                else if (ciudad != null && !ciudad.isBlank()) nombre.append(" - ").append(ciudad);

                nombre.append(" (").append(hora).append(")");

                var dto = new sigmacine.aplicacion.data.CompraProductoDTO(
                        null, this.funcionId, nombre.toString(), 1, PRECIO_ASIENTO, code
                );

                carrito.addItem(dto);
                asientoItems.add(dto);
            }
        }
    }

    private void toggleCartPopup() {
        if (cartStage != null && cartStage.isShowing()) cartStage.close();
        else openCartPopup();
    }

    private void openCartPopup() {
        try {
            if (cartStage == null) {
                var loader = new javafx.fxml.FXMLLoader(getClass()
                        .getResource("/sigmacine/ui/views/verCarrito.fxml"));
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

            Stage owner = (Stage) gridSala.getScene().getWindow();
            cartStage.setX(owner.getX() + owner.getWidth() - 650);
            cartStage.setY(owner.getY() + 100);

            cartStage.show();
            cartStage.toFront();

        } catch (Exception ex) {
            // silencio: maneja con logger si quieres
        }
    }


    // ================================================================
    //   CARGA DE INFORMACIÓN DE PELÍCULA / FUNCIÓN DESDE BD
    // ================================================================
    public void setFuncion(String titulo,
                           String hora,
                           Set<String> ocupados,
                           Set<String> accesibles,
                           Long funcionId,
                           String ciudad,
                           String sede) {

        if (titulo != null) this.titulo = titulo;
        if (hora != null) this.hora = hora;
        if (ciudad != null) this.ciudad = ciudad;
        if (sede != null) this.sede = sede;
        this.funcionId = funcionId;

        this.ocupados.clear();
        if (ocupados != null) this.ocupados.addAll(ocupados);

        this.accesibles.clear();
        if (accesibles != null && !accesibles.isEmpty())
            this.accesibles.addAll(accesibles);

        // Cargar info visual (priorizar Image si está presente)
        if (lblTitulo != null) lblTitulo.setText(this.titulo);
        if (lblHoraPill != null) lblHoraPill.setText(this.hora);

        cargarPosterSiPosible();

        poblarGrilla();
        sincronizarConCarritoExistente();
        actualizarResumen();
    }

    public void setFuncion(String titulo,
                           String hora,
                           Set<String> ocupados,
                           Set<String> accesibles,
                           Long funcionId) {
        setFuncion(titulo, hora, ocupados, accesibles, funcionId, "", "");
    }

    private void sincronizarConCarritoExistente() {
        seleccion.clear();
        asientoItems.clear();

        for (var item : carrito.getItems()) {

            if (Objects.equals(item.getFuncionId(), this.funcionId)
                    && item.getAsiento() != null) {

                String code = item.getAsiento();
                seleccion.add(code);
                asientoItems.add(item);

                ToggleButton btn = seatByCode.get(code);
                if (btn != null) {
                    boolean acc = accesibles.contains(code);
                    btn.setSelected(true);
                    if (acc) setSeatState(btn, SeatState.SELECTED_ACCESSIBLE);
                    else     setSeatState(btn, SeatState.SELECTED);
                }
            }
        }
    }

    private void doSearch(String texto) {
        if (texto == null || texto.isBlank()) return;

        try {
            var db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
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

            Scene curr = stage.getScene();
            stage.setScene(new Scene(root,
                    curr != null ? curr.getWidth() : 900,
                    curr != null ? curr.getHeight() : 600));
            stage.setMaximized(true);

        } catch (Exception ex) {
            // silencio en errores de búsqueda (usa logger si quieres)
        }
    }

    /* ------------------ Helpers de recursos e imágenes ------------------ */

    /**
     * Intenta cargar el poster usando las diferentes estrategias posibles:
     * - si poster (Image) ya se pasó, usarlo
     * - si posterResourceName tiene valor, intentar resolver:
     *    1) /Images/Posters/<posterResourceName>
     *    2) /Images/<posterResourceName>
     *    3) posterResourceName (si es URL)
     */
    private void cargarPosterSiPosible() {
        if (imgPoster == null) return;

        if (this.poster != null) {
            imgPoster.setImage(this.poster);
            return;
        }

        if (this.posterResourceName == null || this.posterResourceName.isBlank()) return;

        Image img = resolveResourceImageFlexible(this.posterResourceName, "Images/Posters/");
        if (img == null) {
            // segunda opcion: buscar en /Images/
            img = resolveResourceImageFlexible(this.posterResourceName, "Images/");
        }
        if (img == null) {
            // si el string parece una URL remota o file:, intentar cargar directamente
            try {
                if (this.posterResourceName.startsWith("http://") || this.posterResourceName.startsWith("https://")
                        || this.posterResourceName.startsWith("file:/")) {
                    img = new Image(this.posterResourceName, true);
                }
            } catch (Exception ignored) {}
        }

        if (img != null) imgPoster.setImage(img);
    }

    private Image resolveResourceImageFlexible(String resourceName, String defaultFolderPrefix) {
        if (resourceName == null) return null;
        try {
            // 1) si viene como "/Images/Posters/avengers.png" o "/Images/avengers.png"
            String candidate = resourceName.startsWith("/") ? resourceName : ("/" + resourceName);

            // intentar directo
            InputStream is = getClass().getResourceAsStream(candidate);
            if (is != null) return new Image(is);

            // intentar con prefijo
            String withPrefix = resourceName.startsWith("/") ? ("/" + defaultFolderPrefix + resourceName.substring(1)) : ("/" + defaultFolderPrefix + resourceName);
            is = getClass().getResourceAsStream(withPrefix);
            if (is != null) return new Image(is);

            // intentar sin slash + default folder
            is = getClass().getResourceAsStream("/" + defaultFolderPrefix + resourceName);
            if (is != null) return new Image(is);

            // intentar tratar como URL
            if (resourceName.startsWith("http://") || resourceName.startsWith("https://") || resourceName.startsWith("file:/")) {
                return new Image(resourceName, true);
            }

            // intento final: buscar como resourceName tal cual (sin prefijos)
            is = getClass().getResourceAsStream(resourceName);
            if (is != null) return new Image(is);

        } catch (Exception ignored) {}
        return null;
    }
}
