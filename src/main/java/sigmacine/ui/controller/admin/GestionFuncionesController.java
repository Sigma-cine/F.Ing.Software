package sigmacine.ui.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.aplicacion.data.FuncionFormDTO;
import sigmacine.aplicacion.service.admi.GestionFuncionesService;
import sigmacine.dominio.entity.Funcion;
import sigmacine.dominio.entity.Pelicula;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GestionFuncionesController {

    private final GestionFuncionesService service;
    private final PeliculaRepositoryJdbc peliculaRepo;

    // --- Top / búsqueda ---
    @FXML private TextField tfBuscar;

    // --- Tabla ---
    @FXML private TableView<FuncionDisponibleDTO> tblFunciones;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colId;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colPelicula;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colSala;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colFecha;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colHora;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colDuracion;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colEstado;
    @FXML private TableColumn<FuncionDisponibleDTO, String> colEstadoBool;

    private final ObservableList<FuncionDisponibleDTO> modelo = FXCollections.observableArrayList();

    // --- Formulario ---
    @FXML private ComboBox<Pelicula> cbPelicula;
    @FXML private ComboBox<IdNombre> cbSala;
    @FXML private DatePicker dpFecha;
    @FXML private TextField tfHora;
    @FXML private TextField tfDuracion;
    @FXML private ComboBox<String> cbEstado;
    @FXML private CheckBox chkEstadoBool;
    @FXML private TextField tfIdEdicion;

    // --- Barra de estado (sin popups) ---
    @FXML private Label lblStatus;

    // --- Cachés para renderizado sin cambiar DTO ---
    private final Map<Long, String> tituloPelicula = new HashMap<>(); // peliculaId -> titulo
    private final Map<Long, Funcion> detallesFuncion = new HashMap<>(); // funcionId -> Funcion

    // DTO simple para sala (id + etiqueta)
    public static class IdNombre {
        public long id;
        public String nombre;
        public IdNombre(long id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    public GestionFuncionesController(GestionFuncionesService service, PeliculaRepositoryJdbc peliculaRepo) {
        if (service == null) throw new IllegalArgumentException("GestionFuncionesService nulo");
        if (peliculaRepo == null) throw new IllegalArgumentException("PeliculaRepositoryJdbc nulo");
        this.service = service;
        this.peliculaRepo = peliculaRepo;
    }

    @FXML
    public void initialize() {
        // ------ Tabla ------
        tblFunciones.setItems(modelo);

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getFuncionId())));

        colPelicula.setCellValueFactory(c -> {
            long pid = c.getValue().getPeliculaId();
            String titulo = tituloPelicula.getOrDefault(pid, pid == 0 ? "" : String.valueOf(pid));
            return new SimpleStringProperty(titulo);
        });

        colSala.setCellValueFactory(c -> {
            FuncionDisponibleDTO dto = c.getValue();
            String sala = (dto.getNumeroSala() > 0 ? "Sala " + dto.getNumeroSala() : "");
            String sede = (dto.getSede() != null && !dto.getSede().isBlank()) ? " (" + dto.getSede() + ")" : "";
            return new SimpleStringProperty(sala + sede);
        });

        colFecha.setCellValueFactory(c -> {
            LocalDate f = c.getValue().getFecha();
            return new SimpleStringProperty(f != null ? f.format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        });

        colHora.setCellValueFactory(c -> {
            LocalTime h = c.getValue().getHora();
            return new SimpleStringProperty(h != null ? h.toString() : "");
        });

        colDuracion.setCellValueFactory(c ->
                new SimpleStringProperty(formatDuracion(detallesFuncion.get(c.getValue().getFuncionId()))));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(formatEstado(detallesFuncion.get(c.getValue().getFuncionId()))));
        colEstadoBool.setCellValueFactory(c ->
                new SimpleStringProperty(formatActivo(detallesFuncion.get(c.getValue().getFuncionId()))));

        // Sincronizar selección -> formulario
        tblFunciones.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            tfIdEdicion.setText(String.valueOf(sel.getFuncionId()));
            dpFecha.setValue(sel.getFecha());
            tfHora.setText(sel.getHora() != null ? sel.getHora().toString() : "");

            if (sel.getPeliculaId() > 0) {
                for (Pelicula p : cbPelicula.getItems()) {
                    if (p.getId() == sel.getPeliculaId()) {
                        cbPelicula.setValue(p);
                        break;
                    }
                }
            }
            seleccionarSalaDesdeDTO(sel);

            Funcion f = detallesFuncion.get(sel.getFuncionId());
            if (f != null) {
                if (f.getDuracion() != null) tfDuracion.setText(toHHmm(f.getDuracion().toLocalTime()));
                if (f.getEstado() != null) cbEstado.getSelectionModel().select(f.getEstado());
                if (f.getEstadoBool() != null) chkEstadoBool.setSelected(f.getEstadoBool());
            }
        });

        // ------ Form ------
        ObservableList<Pelicula> peliculas =
                FXCollections.observableArrayList(peliculaRepo.buscarTodas());
        cbPelicula.setItems(peliculas);

        // FIX de genéricos: construir Map<Long,String> explícito y luego putAll
        Map<Long, String> cacheTitulos =
                peliculas.stream().collect(Collectors.toMap(
                        p -> Long.valueOf(p.getId()),
                        Pelicula::getTitulo,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        tituloPelicula.clear();
        tituloPelicula.putAll(cacheTitulos);

        cbPelicula.setCellFactory(list -> new ListCell<Pelicula>() {
            @Override protected void updateItem(Pelicula item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitulo());
            }
        });
        cbPelicula.setButtonCell(new ListCell<Pelicula>() {
            @Override protected void updateItem(Pelicula item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitulo());
            }
        });
        cbPelicula.setConverter(new StringConverter<Pelicula>() {
            @Override public String toString(Pelicula p) { return p == null ? "" : p.getTitulo(); }
            @Override public Pelicula fromString(String s) {
                for (Pelicula p : cbPelicula.getItems()) {
                    if (Objects.equals(p.getTitulo(), s)) return p;
                }
                return null;
            }
        });

        cbEstado.setItems(FXCollections.observableArrayList("Activa", "Inactiva"));
        cbEstado.getSelectionModel().select("Activa");

        cbSala.setItems(FXCollections.observableArrayList(
                new IdNombre(1,  "Sala 1 (Salitre Plaza)"),
                new IdNombre(2,  "Sala 2 (Salitre Plaza 3D)"),
                new IdNombre(3,  "Sala 3 (Salitre VIP)"),
                new IdNombre(4,  "Sala 1 (Gran Estación)"),
                new IdNombre(5,  "Sala 2 (Gran Estación 3D)"),
                new IdNombre(6,  "Sala 1 (Parque La Colina)"),
                new IdNombre(7,  "Sala 2 (Parque La Colina VIP)"),
                new IdNombre(8,  "Sala 1 (Viva Envigado)"),
                new IdNombre(9,  "Sala 2 (Viva Envigado 3D)"),
                new IdNombre(10, "Sala 1 (El Tesoro)")
        ));

        dpFecha.setValue(LocalDate.now());
        tfHora.setText("13:00");
        tfDuracion.setText("02:00");
        chkEstadoBool.setSelected(true);

        onListarTodas();
        setStatusInfo("Lista cargada.");
    }

    // -------- Acciones de la barra superior --------
    @FXML
    public void onBuscar() {
        String q = safe(tfBuscar.getText());
        try {
            List<FuncionDisponibleDTO> res = q.isEmpty() ? service.listarTodas() : service.buscar(q);
            cargarModeloYDetalles(res);
            setStatusInfo(res.isEmpty() ? "Sin resultados." : ("Resultados: " + res.size()));
        } catch (Exception e) {
            setStatusError("Error al buscar: " + e.getMessage());
        }
    }

    @FXML
    public void onListarTodas() {
        try {
            List<FuncionDisponibleDTO> res = service.listarTodas();
            cargarModeloYDetalles(res);
            setStatusInfo("Se listaron " + modelo.size() + " funciones.");
        } catch (Exception e) {
            setStatusError("Error listando: " + e.getMessage());
        }
    }

    // -------- Acciones del panel de gestión --------
    @FXML
    public void onNuevo() {
        tblFunciones.getSelectionModel().clearSelection();
        tfIdEdicion.clear();
        tfHora.clear();
        tfDuracion.setText("02:00");
        dpFecha.setValue(LocalDate.now());
        cbEstado.getSelectionModel().select("Activa");
        chkEstadoBool.setSelected(true);
        setStatusInfo("Formulario limpio.");
    }

    @FXML
    public void onCrear() {
        try {
            FuncionFormDTO dto = buildFormDTO(0);
            service.crear(dto);
            onListarTodas();
            onNuevo();
            setStatusSuccess("Función creada correctamente.");
        } catch (Exception e) {
            setStatusError("No se pudo crear: " + e.getMessage());
        }
    }

    @FXML
    public void onActualizar() {
        try {
            long id = parseLongOrZero(safe(tfIdEdicion.getText()));
            if (id <= 0) {
                setStatusInfo("Selecciona una fila o ingresa un ID para actualizar.");
                return;
            }
            FuncionFormDTO dto = buildFormDTO(id);
            service.actualizar(dto);
            onListarTodas();
            setStatusSuccess("Función actualizada (ID " + id + ").");
        } catch (Exception e) {
            setStatusError("No se pudo actualizar: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        FuncionDisponibleDTO sel = tblFunciones.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatusInfo("Selecciona una fila para eliminar."); return; }
        long id = sel.getFuncionId();
        try {
            boolean ok = service.eliminar(id);
            if (ok) {
                modelo.remove(sel);
                detallesFuncion.remove(id);
                onNuevo();
                setStatusSuccess("Función eliminada (ID " + id + ").");
            } else {
                setStatusInfo("No se eliminó (revisar restricciones).");
            }
        } catch (Exception e) {
            setStatusError("Error eliminando ID " + id + ": " + e.getMessage());
        }
    }

    // -------- Helpers --------
    private void cargarModeloYDetalles(List<FuncionDisponibleDTO> res) {
        modelo.setAll(res);
        detallesFuncion.clear();
        for (FuncionDisponibleDTO dto : res) {
            try {
                Funcion f = service.buscarPorId(dto.getFuncionId());
                if (f != null) detallesFuncion.put(dto.getFuncionId(), f);
            } catch (Exception ignored) {}
        }
        tblFunciones.refresh();
    }

    private FuncionFormDTO buildFormDTO(long id) {
        Pelicula peli = cbPelicula.getValue();
        IdNombre sala = cbSala.getValue();

        if (peli == null) throw new RuntimeException("Selecciona una película.");
        if (sala == null) throw new RuntimeException("Selecciona una sala.");
        if (dpFecha.getValue() == null) throw new RuntimeException("Selecciona una fecha.");

        String hora = safe(tfHora.getText());
        String dur  = safe(tfDuracion.getText());
        if (hora.isEmpty()) throw new RuntimeException("Ingresa la hora (HH:mm).");
        if (dur.isEmpty())  throw new RuntimeException("Ingresa la duración (HH:mm).");

        FuncionFormDTO dto = new FuncionFormDTO();
        dto.id = id; // 0 => crear; >0 => actualizar
        dto.peliculaId = peli.getId();
        dto.salaId = sala.id;
        dto.fecha = dpFecha.getValue().toString();
        dto.hora = hora;
        dto.duracion = dur;
        dto.estado = cbEstado.getValue();
        dto.estadoBool = chkEstadoBool.isSelected();
        return dto;
    }

    private void seleccionarSalaDesdeDTO(FuncionDisponibleDTO sel) {
        if (sel.getNumeroSala() <= 0 && (sel.getSede() == null || sel.getSede().isBlank())) return;
        for (IdNombre opt : cbSala.getItems()) {
            if (opt.nombre.contains("Sala " + sel.getNumeroSala())
                    && (sel.getSede() == null || opt.nombre.contains(sel.getSede()))) {
                cbSala.setValue(opt);
                break;
            }
        }
    }

    private static String toHHmm(LocalTime t) {
        return t == null ? "" : String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private String formatDuracion(Funcion f) {
        if (f == null || f.getDuracion() == null) return "";
        return toHHmm(f.getDuracion().toLocalTime());
    }

    private String formatEstado(Funcion f) {
        return (f == null || f.getEstado() == null) ? "" : f.getEstado();
    }

    private String formatActivo(Funcion f) {
        if (f == null || f.getEstadoBool() == null) return "";
        return Boolean.TRUE.equals(f.getEstadoBool()) ? "Sí" : "No";
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private long parseLongOrZero(String s) { try { return Long.parseLong(s); } catch (Exception e) { return 0L; } }

    // ----- barra de estado -----
    private void setStatus(String msg, String style) {
        if (lblStatus == null) return;
        lblStatus.setText(msg);
        lblStatus.setStyle(style);
    }
    private void setStatusInfo(String m){ setStatus(m, "-fx-text-fill: #d0d0d0;"); }
    private void setStatusSuccess(String m){ setStatus(m, "-fx-text-fill: #6ad46a;"); }
    private void setStatusError(String m){ setStatus(m, "-fx-text-fill: #ff6868;"); }
}
