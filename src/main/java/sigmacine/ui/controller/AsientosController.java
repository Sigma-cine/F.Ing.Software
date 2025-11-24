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

import java.io.File;
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

    @FXML private ImageView imgConvencion;

    private final int FILAS = 8;
    private final int[][] configFilas = {
            {3, 6},
            {2, 8},
            {2, 8},
            {2, 8},
            {1,10},
            {1,10},
            {1,10},
            {1,10}
    };

    private final Set<String> ocupados   = new HashSet<>();
    private final Set<String> accesibles = new HashSet<>();
    private final Set<String> seleccion  = new HashSet<>();
    private final Map<String, ToggleButton> seatByCode = new HashMap<>();

    private String titulo;
    private String hora;
    private String ciudad;
    private String sede;
    private Image poster;
    private String posterResourceName = null;
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

    // ================================================================
    //      SISTEMA DE IMÁGENES CORREGIDO Y ROBUSTO
    // ================================================================

    /**
     * Intenta cargar un InputStream desde resources para la ruta dada (por ejemplo:
     * "/Images/Posters/avatar.png" o "/Images/Asientos/convencion.png").
     * Devuelve null si no existe.
     */
    private Image cargarDesdeResources(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return null;

        String candidate = resourcePath.startsWith("/") ? resourcePath : ("/" + resourcePath);
        try (InputStream is = getClass().getResourceAsStream(candidate)) {
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Normaliza una entrada de poster/trailer/imagen que puede venir en muchas formas:
     * - "avatar.png"
     * - "Posters/avatar.png"
     * - "/Images/Posters/avatar.png"
     * - "F.Ing.Software/src/main/resources/Images/Posters/avatar.png"
     *
     * Resultado: intenta devolver el nombre de archivo "avatar.png" y también puede devolver
     * la ruta "/Images/Posters/avatar.png" si detecta que el valor ya apunta dentro de resources.
     *
     * No imprime ni lanza excepciones.
     */
    private String normalizePosterReference(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replace("\\", "/");

        // Si ya es una URL remota o file:, devolver tal cual (para fallback)
        if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:/")) {
            return s;
        }

        // Si contiene src/main/resources, eliminar el prefijo hasta Images/...
        int idx = s.indexOf("src/main/resources");
        if (idx >= 0) {
            int imagesIdx = s.indexOf("Images/", idx);
            if (imagesIdx >= 0) {
                return "/" + s.substring(imagesIdx); // devolver "/Images/Posters/..."
            } else {
                // intentar recuperar filename
                int lastSlash = s.lastIndexOf('/');
                return lastSlash >= 0 ? s.substring(lastSlash + 1) : s;
            }
        }

        // Si contiene "/Images/" ya es una ruta dentro del jar
        if (s.contains("/Images/")) {
            // asegurar que comience con '/'
            if (!s.startsWith("/")) return "/" + s;
            return s;
        }

        // Si contiene "Images/" sin leading slash
        if (s.startsWith("Images/")) {
            return "/" + s;
        }

        // Si contiene "Posters/" o "Asientos/" devolver con /Images/ prefijo
        if (s.startsWith("Posters/") || s.startsWith("Asientos/") || s.startsWith("Asientos\\")) {
            return "/Images/" + s;
        }

        // Si viene con path que incluye folders, quitar folders y devolver solo filename
        if (s.contains("/")) {
            String filename = s.substring(s.lastIndexOf('/') + 1);
            return filename;
        }

        // Por defecto devolver el nombre (filename)
        return s;
    }

    /**
     * Resolver imagen de póster con varios intentos:
     * 1) Si poster (Image) ya definido, retornarlo.
     * 2) Si posterResourceName define ruta o nombre, intentar:
     *    - si normalize devuelve "/Images/Posters/<file>", intentar cargar directo.
     *    - si normalize devuelve "file.png", intentar "/Images/Posters/file.png".
     *    - si normalize devuelve url remota, intentar cargar como URL.
     *    - fallback: intentar en "/Images/<file>".
     * 3) Si todo falla, devolver null.
     *
     * No imprime ni lanza errores.
     */
    private Image resolvePosterImage() {
        // 1) Image ya definida
        if (this.poster != null) return this.poster;

        if (this.posterResourceName == null || this.posterResourceName.isBlank()) return null;

        String normalized = normalizePosterReference(this.posterResourceName);

        // Si normalization devolvió una URL remota (starts with http or file)
        if (normalized != null && (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("file:/"))) {
            try {
                return new Image(normalized, true);
            } catch (Exception ignored) { /* fallback below */ }
        }

        // Si normalization devolvió algo tipo "/Images/Posters/avatar.png", probar directo
        if (normalized != null && normalized.startsWith("/Images/Posters/")) {
            Image img = cargarDesdeResources(normalized);
            if (img != null) return img;
        }

        // Si normalization devolvió solo filename o "avatar.png"
        if (normalized != null && !normalized.startsWith("/")) {
            Image img = cargarDesdeResources("/Images/Posters/" + normalized);
            if (img != null) return img;

            img = cargarDesdeResources("/Images/" + normalized);
            if (img != null) return img;
        }

        // último intento: si posterResourceName es una ruta absoluta en FS, intentar cargar File
        try {
            File f = new File(this.posterResourceName);
            if (f.exists()) {
                return new Image(f.toURI().toString(), false);
            }
        } catch (Exception ignored) {}

        // no se encontró
        return null;
    }

    public void setPoster(Image poster) {
        this.poster = poster;
        this.posterResourceName = null;
        if (imgPoster != null && poster != null) imgPoster.setImage(poster);
    }

    /**
     * Acepta valores como:
     *  - "avatar.png"
     *  - "Posters/avatar.png"
     *  - "/Images/Posters/avatar.png"
     *  - "F.Ing.Software/src/main/resources/Images/Posters/avatar.png"
     *
     * y los normaliza / intenta resolver.
     */
    public void setPosterResource(String nombreArchivo) {
        this.posterResourceName = nombreArchivo;
        cargarPosterSiPosible();
    }

    public void setConvencionResource(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank() || imgConvencion == null) return;

        // normalize reference like poster
        String normalized = normalizePosterReference(nombreArchivo);

        // Preferir /Images/Asientos/<file>
        Image img = null;
        if (normalized != null && normalized.startsWith("/Images/Asientos/")) {
            img = cargarDesdeResources(normalized);
        }
        if (img == null) {
            // si viene solo filename
            String candidate = normalized != null && !normalized.startsWith("/") ? "/Images/Asientos/" + normalized : normalized;
            img = cargarDesdeResources(candidate);
        }
        if (img == null) {
            // fallback /Images/<name>
            String filename = normalized != null && normalized.contains("/") ? normalized.substring(normalized.lastIndexOf('/') + 1) : normalized;
            if (filename != null) img = cargarDesdeResources("/Images/" + filename);
        }
        if (img != null) imgConvencion.setImage(img);
    }

    private void cargarPosterSiPosible() {
        if (imgPoster == null) return;

        // 1) si el Image ya fue provisto
        if (this.poster != null) {
            imgPoster.setImage(this.poster);
            return;
        }

        // 2) intentar resolver por nombre/ruta
        Image resolved = resolvePosterImage();
        if (resolved != null) {
            imgPoster.setImage(resolved);
            return;
        }

        // 3) si no se encontró nada, dejar como estaba (no setear null)
    }

    // ================================================================

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

        cargarPosterSiPosible();

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

    // ================================================================
    //                       GRILLA DE ASIENTOS
    // ================================================================
    private void poblarGrilla() {
        if (gridSala == null) return;

        gridSala.getChildren().clear();
        seatByCode.clear();

        int maxColumn = 0;
        for (int[] fila : configFilas) {
            int inicio = fila[0];
            int cant   = fila[1];
            maxColumn = Math.max(maxColumn, inicio + cant - 1);
        }

        for (int col = 1; col <= maxColumn; col++) {
            Label lbl = new Label(String.valueOf(col));
            lbl.getStyleClass().add("seat-number");
            lbl.setMinWidth(Region.USE_PREF_SIZE);
            lbl.setPrefWidth(48);
            lbl.setAlignment(Pos.CENTER);

            GridPane.setHalignment(lbl, HPos.CENTER);
            GridPane.setValignment(lbl, VPos.CENTER);

            gridSala.add(lbl, col, 0);
        }

        for (int f = 0; f < FILAS; f++) {

            char letra = (char)('A' + f);
            Label lblFila = new Label(String.valueOf(letra));
            lblFila.getStyleClass().add("seat-letter");
            lblFila.setMinWidth(Region.USE_PREF_SIZE);
            lblFila.setPrefWidth(40);
            lblFila.setAlignment(Pos.CENTER);
            GridPane.setHalignment(lblFila, HPos.CENTER);
            gridSala.add(lblFila, 0, f + 1);

            int inicio = configFilas[f][0];
            int cant   = configFilas[f][1];

            for (int i = 0; i < cant; i++) {

                int colReal = inicio + i;
                String code = generarCodigo(f, colReal);

                ToggleButton seat = new ToggleButton();
                seat.setUserData(code);
                seat.setTooltip(new Tooltip(code));
                seat.setFocusTraversable(false);
                seat.getStyleClass().add("seat");

                boolean isAcc = accesibles.contains(code);
                seat.getProperties().put("accessible", isAcc);

                if (ocupados.contains(code)) {
                    setSeatState(seat, SeatState.UNAVAILABLE);
                    seat.setDisable(true);
                    if (isAcc) seat.getStyleClass().add("seat--accessible");
                } else {
                    if (seleccion.contains(code)) {
                        if (isAcc) setSeatState(seat, SeatState.SELECTED_ACCESSIBLE);
                        else setSeatState(seat, SeatState.SELECTED);
                        seat.setSelected(true);
                    } else {
                        if (isAcc) setSeatState(seat, SeatState.AVAILABLE_ACCESSIBLE);
                        else setSeatState(seat, SeatState.AVAILABLE);
                    }

                    seat.setOnAction(e -> {
                        boolean acc = Boolean.TRUE.equals(seat.getProperties().get("accessible"));
                        if (seat.isSelected()) {
                            seleccion.add(code);
                            if (acc) setSeatState(seat, SeatState.SELECTED_ACCESSIBLE);
                            else setSeatState(seat, SeatState.SELECTED);
                        } else {
                            seleccion.remove(code);
                            if (acc) setSeatState(seat, SeatState.AVAILABLE_ACCESSIBLE);
                            else setSeatState(seat, SeatState.AVAILABLE);
                        }
                        actualizarResumen();
                    });
                }

                seatByCode.put(code, seat);
                GridPane.setHalignment(seat, HPos.CENTER);
                gridSala.add(seat, colReal, f + 1);
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

    private String generarCodigo(int filaIdx, int colReal) {
        return (char)('A' + filaIdx) + Integer.toString(colReal);
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

        for (String code : seleccion.stream().sorted().toList()) {

            StringBuilder nombre = new StringBuilder("Asiento ")
                    .append(code).append(" - ").append(titulo);

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
                cartStage.setResizable(false);
                cartStage.setTitle("Carrito");
                cartStage.setScene(new Scene(root));
            }

            Stage owner = (Stage) gridSala.getScene().getWindow();
            cartStage.setX(owner.getX() + owner.getWidth() - 650);
            cartStage.setY(owner.getY() + 100);

            cartStage.show();
            cartStage.toFront();

        } catch (Exception ignored) {}
    }

    // ================================================================
    //             CARGA INFO FUNCIÓN / BD
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
        if (accesibles != null) this.accesibles.addAll(accesibles);

        if (lblTitulo != null) lblTitulo.setText(titulo);
        if (lblHoraPill != null) lblHoraPill.setText(hora);

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
                    else setSeatState(btn, SeatState.SELECTED);
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

        } catch (Exception ignored) {}
    }
}