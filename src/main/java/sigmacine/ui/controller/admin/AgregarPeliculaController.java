package sigmacine.ui.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.data.PeliculaDTO;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AgregarPeliculaController {

    // ====== Barra de búsqueda y tabla ======
    @FXML private TextField campoBusqueda;
    @FXML private TableView<PeliculaDTO> tablaPeliculas;
    @FXML private TableColumn<PeliculaDTO, Integer> columnaId;
    @FXML private TableColumn<PeliculaDTO, String>  columnaTitulo;
    @FXML private TableColumn<PeliculaDTO, String>  columnaGenero;
    @FXML private TableColumn<PeliculaDTO, String>  columnaClasificacion;
    @FXML private TableColumn<PeliculaDTO, Integer> columnaDuracion;
    @FXML private TableColumn<PeliculaDTO, String>  columnaEstado;

    // ====== Formulario ======
    @FXML private TextField campoTituloPelicula;
    // Género ahora es ComboBox editable (autocomplete básico)
    @FXML private ComboBox<String> comboGeneroPelicula;
    @FXML private TextField campoClasificacionPelicula;
    @FXML private TextField campoDuracionMinutos;
    @FXML private TextField campoDirectorPelicula;
    @FXML private TextField campoRepartoPelicula;

    // Ruta (string) del trailer (archivo mp4/mp3...), no URL
    @FXML private TextField campoUrlTrailer;

    // Ruta (string) del póster, **sin previsualización**
    @FXML private TextField campoUrlPoster;

    @FXML private TextArea  campoSinopsisPelicula;
    @FXML private ComboBox<String> comboEstado;

    // Indicador de estado de imagen (pequeño texto al lado del botón)
    @FXML private Label lblImgStatus;

    @FXML private Label mensajeLabel;

    private GestionPeliculasService gestionPeliculasService;

    public void setGestionPeliculasService(GestionPeliculasService servicio) {
        this.gestionPeliculasService = servicio;
        inicializarDatos();
    }

    @FXML
    private void initialize() {
        // columnas
        if (columnaId != null) {
            columnaId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                    c.getValue().getIdPelicula()==null?0:c.getValue().getIdPelicula()).asObject());
        }
        if (columnaTitulo != null)
            columnaTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(nvl(c.getValue().getTituloPelicula())));
        if (columnaGenero != null)
            columnaGenero.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(nvl(c.getValue().getGeneroPelicula())));
        if (columnaClasificacion != null)
            columnaClasificacion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(nvl(c.getValue().getClasificacionPelicula())));
        if (columnaDuracion != null) {
            columnaDuracion.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                    c.getValue().getDuracionMinutos()==null?0:c.getValue().getDuracionMinutos()).asObject());
        }
        if (columnaEstado != null)
            columnaEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(nvl(c.getValue().getEstadoPelicula())));

        if (tablaPeliculas != null) {
            tablaPeliculas.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> cargarEnFormulario(b));
        }
        if (comboEstado != null) {
            comboEstado.setItems(FXCollections.observableArrayList("ACTIVA", "INACTIVA", "En Cartelera", "Próximamente"));
            comboEstado.setValue("ACTIVA");
        }
        if (lblImgStatus != null) lblImgStatus.setText("Sin imagen");

        if (comboGeneroPelicula != null) {
            comboGeneroPelicula.setEditable(true); // permite escribir nuevos géneros
        }
    }

    private void inicializarDatos() {
        if (gestionPeliculasService != null) {
            onListarTodas();
        }
    }

    // ========= Imagen =========
    @FXML
    private void onElegirImagen() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar imagen de póster");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.webp")
            );
            File file = fc.showOpenDialog(null);
            if (file == null) return;

            Path projectRoot = Path.of("").toAbsolutePath();
            Path imagesDir   = projectRoot.resolve("src/main/resources/Images");
            if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);

            String ext = extension(file.getName());
            String newName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty()?".png":ext);
            Path target = imagesDir.resolve(newName);

            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(target.toFile())) {
                in.transferTo(out);
            }

            // guardamos una ruta RELATIVA
            String relative = "src\\main\\resources\\Images\\" + newName;
            if (campoUrlPoster != null) campoUrlPoster.setText(relative);

            if (lblImgStatus != null) lblImgStatus.setText(newName);
            mostrarMensaje("OK", "Imagen copiada.");
        } catch (Exception ex) {
            mostrarMensaje("ERROR", "No se pudo cargar la imagen: " + ex.getMessage());
        }
    }

    // ========= Trailer (archivo video/audio) =========
    @FXML
    private void onElegirTrailer() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar archivo de trailer");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Video / Audio",
                            "*.mp4", "*.mkv", "*.avi", "*.mov", "*.mp3")
            );
            File file = fc.showOpenDialog(null);
            if (file == null) return;

            Path projectRoot = Path.of("").toAbsolutePath();
            Path videosDir   = projectRoot.resolve("src/main/resources/videos");
            if (!Files.exists(videosDir)) Files.createDirectories(videosDir);

            String ext = extension(file.getName());
            String newName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty()?".mp4":ext);
            Path target = videosDir.resolve(newName);

            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(target.toFile())) {
                in.transferTo(out);
            }

            // ruta relativa al proyecto
            String relative = "src\\main\\resources\\videos\\" + newName;
            if (campoUrlTrailer != null) campoUrlTrailer.setText(relative);

            mostrarMensaje("OK", "Trailer copiado.");
        } catch (Exception ex) {
            mostrarMensaje("ERROR", "No se pudo cargar el trailer: " + ex.getMessage());
        }
    }

    private static String extension(String name) {
        int p = name.lastIndexOf('.');
        return (p >= 0 ? name.substring(p).toLowerCase() : "");
    }

    // ===================== Acciones de búsqueda/tabla =====================
    @FXML
    private void onBuscar() {
        if (gestionPeliculasService == null) return;
        String q = campoBusqueda == null ? "" : Optional.ofNullable(campoBusqueda.getText()).orElse("").trim();
        List<PeliculaDTO> lista;
        if (q.isEmpty()) {
            lista = gestionPeliculasService.obtenerTodasLasPeliculas();
        } else {
            List<PeliculaDTO> a = gestionPeliculasService.buscarPeliculasPorTitulo(q);
            List<PeliculaDTO> b = gestionPeliculasService.buscarPeliculasPorGenero(q);
            lista = mergePorId(a, b);
        }
        if (tablaPeliculas != null) tablaPeliculas.setItems(FXCollections.observableArrayList(lista));
        actualizarListaGeneros(lista); // actualiza los géneros del combo
    }

    @FXML
    private void onListarTodas() {
        if (gestionPeliculasService == null) return;
        List<PeliculaDTO> lista = gestionPeliculasService.obtenerTodasLasPeliculas();
        if (tablaPeliculas != null) tablaPeliculas.setItems(FXCollections.observableArrayList(lista));
        actualizarListaGeneros(lista); // actualiza géneros al listar
    }

    // ===================== Acciones de formulario =====================
    @FXML
    private void onNuevo() {
        limpiarFormulario();
        if (tablaPeliculas != null) tablaPeliculas.getSelectionModel().clearSelection();
    }

    @FXML
    private void onCrearNuevaPelicula() {
        if (gestionPeliculasService == null) {
            mostrarMensaje("ERROR", "Servicio no configurado.");
            return;
        }

        PeliculaDTO dto = recogerFormulario();
        dto.setEstadoPelicula(comboEstado.getValue()==null?"ACTIVA":comboEstado.getValue());

        try {
            PeliculaDTO creada = gestionPeliculasService.crearNuevaPelicula(dto);
            mostrarMensaje("OK", "Película creada (ID " + creada.getIdPelicula() + ").");
            onListarTodas();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje("WARN", ex.getMessage());
        } catch (Exception ex) {
            mostrarMensaje("ERROR", "Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onActualizarSeleccion() {
        if (gestionPeliculasService == null) return;
        PeliculaDTO seleccion = tablaPeliculas == null ? null : tablaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccion == null || seleccion.getIdPelicula() == null) {
            mostrarMensaje("WARN", "Selecciona una película en la tabla.");
            return;
        }

        PeliculaDTO cambios = recogerFormulario();
        cambios.setEstadoPelicula(comboEstado.getValue()==null?"ACTIVA":comboEstado.getValue());

        try {
            gestionPeliculasService.actualizarPeliculaExistente(seleccion.getIdPelicula(), cambios);
            mostrarMensaje("OK", "Película actualizada.");
            onListarTodas();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje("WARN", ex.getMessage());
        } catch (Exception ex) {
            mostrarMensaje("ERROR", "Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onEliminarSeleccion() {
        if (gestionPeliculasService == null) return;
        PeliculaDTO seleccion = tablaPeliculas == null ? null : tablaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccion == null || seleccion.getIdPelicula() == null) {
            mostrarMensaje("WARN", "Selecciona una película en la tabla.");
            return;
        }

        boolean ok = gestionPeliculasService.eliminarPeliculaPorId(seleccion.getIdPelicula());
        if (!ok) {
            mostrarMensaje("WARN", "No se puede eliminar: existen funciones futuras programadas.");
        } else {
            mostrarMensaje("OK", "Película eliminada correctamente.");
            onListarTodas();
            limpiarFormulario();
        }
    }

    // ===================== Helpers =====================
    private PeliculaDTO recogerFormulario() {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setTituloPelicula(texto(campoTituloPelicula));
        dto.setGeneroPelicula(textoCombo(comboGeneroPelicula));
        dto.setClasificacionPelicula(texto(campoClasificacionPelicula));
        dto.setDuracionMinutos(parseEntero(campoDuracionMinutos));
        dto.setDirectorPelicula(texto(campoDirectorPelicula));
        dto.setRepartoPelicula(texto(campoRepartoPelicula));

        // se persiste en BD
        dto.setUrlTrailer(texto(campoUrlTrailer));   // ahora ruta de archivo

        // ruta relativa del póster
        dto.setUrlPoster(texto(campoUrlPoster));

        dto.setSinopsisPelicula(textoArea(campoSinopsisPelicula));
        return dto;
    }

    private void cargarEnFormulario(PeliculaDTO d) {
        if (d == null) { limpiarFormulario(); return; }
        setText(campoTituloPelicula, d.getTituloPelicula());
        setComboGenero(d.getGeneroPelicula());
        setText(campoClasificacionPelicula, d.getClasificacionPelicula());
        setText(campoDuracionMinutos, d.getDuracionMinutos()==null? "" : String.valueOf(d.getDuracionMinutos()));
        setText(campoDirectorPelicula, d.getDirectorPelicula());
        setText(campoRepartoPelicula, d.getRepartoPelicula());
        setText(campoUrlTrailer, d.getUrlTrailer());
        setText(campoUrlPoster, d.getUrlPoster());
        setTextArea(campoSinopsisPelicula, d.getSinopsisPelicula());
        if (comboEstado != null) comboEstado.setValue(nvl(d.getEstadoPelicula(), "ACTIVA"));
        if (lblImgStatus != null) lblImgStatus.setText((d.getUrlPoster()==null||d.getUrlPoster().isBlank())?"Sin imagen":"Asignada");
    }

    private void limpiarFormulario() {
        setText(campoTituloPelicula, "");
        setComboGenero("");
        setText(campoClasificacionPelicula, "");
        setText(campoDuracionMinutos, "");
        setText(campoDirectorPelicula, "");
        setText(campoRepartoPelicula, "");
        setText(campoUrlTrailer, "");
        setText(campoUrlPoster, "");
        setTextArea(campoSinopsisPelicula, "");
        if (comboEstado != null) comboEstado.setValue("ACTIVA");
        if (lblImgStatus != null) lblImgStatus.setText("Sin imagen");
    }

    private List<PeliculaDTO> mergePorId(List<PeliculaDTO> a, List<PeliculaDTO> b) {
        LinkedHashMap<Integer, PeliculaDTO> mapa = new LinkedHashMap<>();
        if (a != null) for (PeliculaDTO d : a) {
            if (d!=null && d.getIdPelicula()!=null && !mapa.containsKey(d.getIdPelicula())) mapa.put(d.getIdPelicula(), d);
        }
        if (b != null) for (PeliculaDTO d : b) {
            if (d!=null && d.getIdPelicula()!=null && !mapa.containsKey(d.getIdPelicula())) mapa.put(d.getIdPelicula(), d);
        }
        return new ArrayList<>(mapa.values());
    }

    // llena el combo de géneros con los existentes en la lista
    private void actualizarListaGeneros(List<PeliculaDTO> peliculas) {
        if (comboGeneroPelicula == null || peliculas == null) return;
        Set<String> generos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PeliculaDTO p : peliculas) {
            if (p == null) continue;
            String g = p.getGeneroPelicula();
            if (g != null) {
                g = g.trim();
                if (!g.isEmpty()) generos.add(g);
            }
        }
        comboGeneroPelicula.setItems(FXCollections.observableArrayList(generos));
    }

    // util ui
    private String texto(TextField t) { return t==null||t.getText()==null?null:t.getText().trim(); }
    private String textoArea(TextArea t) { return t==null||t.getText()==null?null:t.getText().trim(); }
    private Integer parseEntero(TextField t) {
        if (t==null||t.getText()==null) return null;
        String s = t.getText().trim(); if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch(Exception e){ return null; }
    }
    private String textoCombo(ComboBox<String> c) {
        if (c == null) return null;
        // si es editable, el texto puede estar en el editor
        if (c.getEditor() != null && c.getEditor().getText() != null && !c.getEditor().getText().isBlank()) {
            return c.getEditor().getText().trim();
        }
        String v = c.getValue();
        return v==null?null:v.trim();
    }
    private String nvl(String s){ return s==null? "": s; }
    private String nvl(String s,String def){ return (s==null||s.trim().isEmpty())?def:s; }
    private void setText(TextField t, String v){ if(t!=null) t.setText(v==null? "": v); }
    private void setTextArea(TextArea t, String v){ if(t!=null) t.setText(v==null? "": v); }
    private void setComboGenero(String valor) {
        if (comboGeneroPelicula == null) return;
        if (valor == null) valor = "";
        comboGeneroPelicula.setValue(valor);
        if (comboGeneroPelicula.getEditor() != null) {
            comboGeneroPelicula.getEditor().setText(valor);
        }
    }

    private void mostrarMensaje(String tipo, String texto) {
        if (mensajeLabel == null) return;
        switch (tipo) {
            case "OK"    -> mensajeLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-padding: 5;");
            case "WARN"  -> mensajeLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-padding: 5;");
            case "ERROR" -> mensajeLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-padding: 5;");
            default      -> mensajeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
        }
        mensajeLabel.setText(texto);
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> mensajeLabel.setText(""));
        }).start();
    }
}
