package sigmacine.ui.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import sigmacine.aplicacion.data.PeliculaDTO;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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
    @FXML private TextField campoGeneroPelicula;
    @FXML private TextField campoClasificacionPelicula;
    @FXML private TextField campoDuracionMinutos;
    @FXML private TextField campoDirectorPelicula;
    @FXML private TextField campoRepartoPelicula;
    @FXML private TextField campoUrlTrailer;
    @FXML private TextField campoUrlPoster;
    @FXML private TextArea  campoSinopsisPelicula;
    @FXML private ComboBox<String> comboEstado;

    @FXML private Label mensajeLabel;

    private GestionPeliculasService gestionPeliculasService;

    public void setGestionPeliculasService(GestionPeliculasService servicio) {
        this.gestionPeliculasService = servicio;
        inicializarDatos();
    }

    @FXML
    private void initialize() {
        // columnas de la tabla
        if (columnaId != null) {
            columnaId.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getIdPelicula()==null?0:c.getValue().getIdPelicula()).asObject());
        }
        if (columnaTitulo != null) columnaTitulo.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getTituloPelicula())));
        if (columnaGenero != null) columnaGenero.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getGeneroPelicula())));
        if (columnaClasificacion != null) columnaClasificacion.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getClasificacionPelicula())));
        if (columnaDuracion != null) {
            columnaDuracion.setCellValueFactory(c -> new SimpleIntegerProperty(
                c.getValue().getDuracionMinutos()==null?0:c.getValue().getDuracionMinutos()).asObject());
        }
        if (columnaEstado != null) columnaEstado.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getEstadoPelicula())));

        if (tablaPeliculas != null) {
            tablaPeliculas.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> cargarEnFormulario(b));
        }
        if (comboEstado != null) {
            comboEstado.setItems(FXCollections.observableArrayList("ACTIVA", "INACTIVA", "En Cartelera", "Próximamente"));
            comboEstado.setValue("ACTIVA");
        }
    }

    private void inicializarDatos() {
        if (gestionPeliculasService != null) {
            onListarTodas();
        }
    }

    // ====== Acciones de búsqueda/tabla ======
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
    }

    @FXML
    private void onListarTodas() {
        if (gestionPeliculasService == null) return;
        List<PeliculaDTO> lista = gestionPeliculasService.obtenerTodasLasPeliculas();
        if (tablaPeliculas != null) tablaPeliculas.setItems(FXCollections.observableArrayList(lista));
    }

    // ====== Acciones de formulario ======
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

    // ====== Helpers ======
    private PeliculaDTO recogerFormulario() {
        PeliculaDTO dto = new PeliculaDTO();
        dto.setTituloPelicula(texto(campoTituloPelicula));
        dto.setGeneroPelicula(texto(campoGeneroPelicula));
        dto.setClasificacionPelicula(texto(campoClasificacionPelicula));
        dto.setDuracionMinutos(parseEntero(campoDuracionMinutos));
        dto.setDirectorPelicula(texto(campoDirectorPelicula));
        dto.setRepartoPelicula(texto(campoRepartoPelicula));
        dto.setUrlTrailer(texto(campoUrlTrailer));
        dto.setUrlPoster(texto(campoUrlPoster));
        dto.setSinopsisPelicula(textoArea(campoSinopsisPelicula));
        // estado lo setea cada acción usando comboEstado
        return dto;
    }

    private void cargarEnFormulario(PeliculaDTO d) {
        if (d == null) { limpiarFormulario(); return; }
        setText(campoTituloPelicula, d.getTituloPelicula());
        setText(campoGeneroPelicula, d.getGeneroPelicula());
        setText(campoClasificacionPelicula, d.getClasificacionPelicula());
        setText(campoDuracionMinutos, d.getDuracionMinutos()==null? "" : String.valueOf(d.getDuracionMinutos()));
        setText(campoDirectorPelicula, d.getDirectorPelicula());
        setText(campoRepartoPelicula, d.getRepartoPelicula());
        setText(campoUrlTrailer, d.getUrlTrailer());
        setText(campoUrlPoster, d.getUrlPoster());
        setTextArea(campoSinopsisPelicula, d.getSinopsisPelicula());
        if (comboEstado != null) comboEstado.setValue(nvl(d.getEstadoPelicula(), "ACTIVA"));
    }

    private void limpiarFormulario() {
        setText(campoTituloPelicula, "");
        setText(campoGeneroPelicula, "");
        setText(campoClasificacionPelicula, "");
        setText(campoDuracionMinutos, "");
        setText(campoDirectorPelicula, "");
        setText(campoRepartoPelicula, "");
        setText(campoUrlTrailer, "");
        setText(campoUrlPoster, "");
        setTextArea(campoSinopsisPelicula, "");
        if (comboEstado != null) comboEstado.setValue("ACTIVA");
    }

    private List<PeliculaDTO> mergePorId(List<PeliculaDTO> a, List<PeliculaDTO> b) {
        LinkedHashMap<Integer, PeliculaDTO> mapa = new LinkedHashMap<Integer, PeliculaDTO>();
        if (a != null) for (int i=0;i<a.size();i++) { PeliculaDTO d = a.get(i);
            if (d!=null && d.getIdPelicula()!=null && !mapa.containsKey(d.getIdPelicula())) mapa.put(d.getIdPelicula(), d); }
        if (b != null) for (int i=0;i<b.size();i++) { PeliculaDTO d = b.get(i);
            if (d!=null && d.getIdPelicula()!=null && !mapa.containsKey(d.getIdPelicula())) mapa.put(d.getIdPelicula(), d); }
        return new ArrayList<PeliculaDTO>(mapa.values());
    }

    // util ui
    private String texto(TextField t) { return t==null||t.getText()==null?null:t.getText().trim(); }
    private String textoArea(TextArea t) { return t==null||t.getText()==null?null:t.getText().trim(); }
    private Integer parseEntero(TextField t) {
        if (t==null||t.getText()==null) return null;
        String s = t.getText().trim(); if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch(Exception e){ return null; }
    }
    private String nvl(String s){ return s==null? "": s; }
    private String nvl(String s,String def){ return (s==null||s.trim().isEmpty())?def:s; }
    private void setText(TextField t, String v){ if(t!=null) t.setText(v==null? "": v); }
    private void setTextArea(TextArea t, String v){ if(t!=null) t.setText(v==null? "": v); }

    // alerts
private void mostrarMensaje(String tipo, String texto) {
    if (mensajeLabel == null) return;

    switch (tipo) {
        case "OK":
            mensajeLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-padding: 5;");
            break;
        case "WARN":
            mensajeLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-padding: 5;");
            break;
        case "ERROR":
            mensajeLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-padding: 5;");
            break;
        default:
            mensajeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
    }

    mensajeLabel.setText(texto);

    // Borrar el mensaje después de unos segundos
    new Thread(() -> {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
        javafx.application.Platform.runLater(() -> mensajeLabel.setText(""));
    }).start();
}
}
