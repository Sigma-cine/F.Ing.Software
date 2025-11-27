package sigmacine.ui.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.service.admi.GestionFuncionesService;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.aplicacion.service.admi.GestionProductosService;
import sigmacine.aplicacion.data.PeliculaDTO;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.aplicacion.data.ProductoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

public class ReportesController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<String> comboEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private TableView<ItemReporte> tablaReporte;
    @FXML private TableColumn<ItemReporte, String>  colTipo;
    @FXML private TableColumn<ItemReporte, String>  colNombre;
    @FXML private TableColumn<ItemReporte, String>  colDetalle;
    @FXML private TableColumn<ItemReporte, String>  colEstado;

    @FXML private Label lblResumen;

    private final GestionPeliculasService  gestionPeliculasService;
    private final GestionFuncionesService  gestionFuncionesService;
    private final GestionProductosService  gestionProductosService;

    private final ObservableList<ItemReporte> datosBase      = FXCollections.observableArrayList();
    private final ObservableList<ItemReporte> datosFiltrados = FXCollections.observableArrayList();

    // Estados por tipo
    private static final List<String> ESTADOS_PELICULA = List.of(
            "ACTIVA", "INACTIVA", "En cartelera", "Próximamente"
    );
    private static final List<String> ESTADOS_FUNCION = List.of(
            "PROGRAMADA", "FINALIZADA"
    );
    private static final List<String> ESTADOS_PRODUCTO = List.of(
            "EN INVENTARIO"
    );

    public ReportesController(GestionPeliculasService peliculasService,
                              GestionFuncionesService funcionesService,
                              GestionProductosService productosService) {
        this.gestionPeliculasService  = peliculasService;
        this.gestionFuncionesService  = funcionesService;
        this.gestionProductosService  = productosService;
    }

    @FXML
    private void initialize() {
        // Mapeo de columnas
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo()));
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colDetalle.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDetalle()));
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado()));

        // Combo de tipo
        comboTipo.setItems(FXCollections.observableArrayList(
                "Todos", "Películas", "Funciones", "Productos"
        ));
        comboTipo.setValue("Todos");

        // Cuando cambie el tipo, refrescamos los estados disponibles
        comboTipo.valueProperty().addListener((obs, oldV, newV) -> refrescarEstadosPorTipo());

        // Estados iniciales (para "Todos")
        refrescarEstadosPorTipo();

        cargarDatosBase();
        aplicarFiltros();
    }

    /** Rellena el comboEstado según el tipo seleccionado */
    private void refrescarEstadosPorTipo() {
        String tipoSel = comboTipo.getValue();
        ObservableList<String> estados = FXCollections.observableArrayList();
        estados.add("Todos");

        if (tipoSel == null || "Todos".equalsIgnoreCase(tipoSel)) {
            estados.addAll(ESTADOS_PELICULA);
            estados.addAll(ESTADOS_FUNCION);
            estados.addAll(ESTADOS_PRODUCTO);
        } else if ("Películas".equalsIgnoreCase(tipoSel)) {
            estados.addAll(ESTADOS_PELICULA);
        } else if ("Funciones".equalsIgnoreCase(tipoSel)) {
            estados.addAll(ESTADOS_FUNCION);
        } else if ("Productos".equalsIgnoreCase(tipoSel)) {
            estados.addAll(ESTADOS_PRODUCTO);
        }

        comboEstado.setItems(estados);
        comboEstado.setValue("Todos");
    }

    private void cargarDatosBase() {
        datosBase.clear();

        // ===== PELÍCULAS =====
        if (gestionPeliculasService != null) {
            List<PeliculaDTO> peliculas = gestionPeliculasService.obtenerTodasLasPeliculas();
            for (PeliculaDTO p : peliculas) {
                if (p == null) continue;

                ItemReporte item = new ItemReporte();
                item.setTipo("Película");
                item.setNombre(nvl(p.getTituloPelicula()));
                item.setDetalle(
                        "Género: " + nvl(p.getGeneroPelicula()) +
                        " | Clasificación: " + nvl(p.getClasificacionPelicula())
                );
                item.setEstado(nvl(p.getEstadoPelicula(), "ACTIVA"));

                // Campos de ventas/inventario se dejan en cero/null, pero ya no se muestran
                item.setCantidadInventario(0);
                item.setCantidadVendida(0);
                item.setIngresos(null);
                item.setFechaReferencia(null); // si luego agregas fecha de estreno al DTO, la usas aquí

                datosBase.add(item);
            }
        }

        // ===== FUNCIONES =====
        if (gestionFuncionesService != null) {
            List<FuncionDisponibleDTO> funciones = gestionFuncionesService.listarTodas();
            LocalDate hoy = LocalDate.now();

            for (FuncionDisponibleDTO f : funciones) {
                if (f == null) continue;

                ItemReporte item = new ItemReporte();
                item.setTipo("Función");

                item.setNombre("Función de película #" + f.getPeliculaId());

                String detalle = "Fecha: " + (f.getFecha() != null ? f.getFecha().toString() : "") +
                        " | Hora: " + (f.getHora() != null ? f.getHora().toString() : "") +
                        " | Ciudad: " + nvl(f.getCiudad()) +
                        " | Sede: " + nvl(f.getSede()) +
                        " | Sala: " + f.getNumeroSala() +
                        " (" + nvl(f.getTipoSala()) + ")";
                item.setDetalle(detalle);

                LocalDate fecha = f.getFecha();
                String estadoFuncion;
                if (fecha == null) {
                    estadoFuncion = "PROGRAMADA";
                } else if (fecha.isBefore(hoy)) {
                    estadoFuncion = "FINALIZADA";
                } else {
                    estadoFuncion = "PROGRAMADA";
                }
                item.setEstado(estadoFuncion);

                item.setCantidadInventario(0);
                item.setCantidadVendida(0);
                item.setIngresos(null);
                item.setFechaReferencia(fecha);

                datosBase.add(item);
            }
        }

        // ===== PRODUCTOS / CONFITERÍA =====
        if (gestionProductosService != null) {
            List<ProductoDTO> productos = gestionProductosService.listarTodas();
            for (ProductoDTO p : productos) {
                if (p == null) continue;

                ItemReporte item = new ItemReporte();
                item.setTipo("Producto");
                item.setNombre(nvl(p.getNombreProducto()));
                item.setDetalle("Producto de confitería");

                // De momento todos como EN INVENTARIO; se puede refinar cuando haya campos de stock/ventas
                item.setEstado("EN INVENTARIO");
                item.setCantidadInventario(0);
                item.setCantidadVendida(0);
                item.setIngresos(null);
                item.setFechaReferencia(null);

                datosBase.add(item);
            }
        }
    }

    @FXML
    private void onAplicarFiltros() {
        aplicarFiltros();
    }

    @FXML
    private void onLimpiarFiltros() {
        comboTipo.setValue("Todos");
        refrescarEstadosPorTipo();   // esto pone Estado = "Todos"
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String tipoSel   = comboTipo.getValue();
        String estadoSel = comboEstado.getValue();
        LocalDate desde  = dpDesde.getValue();
        LocalDate hasta  = dpHasta.getValue();

        Predicate<ItemReporte> pred = item -> true;

        // Filtro por tipo
        if (tipoSel != null && !"Todos".equalsIgnoreCase(tipoSel)) {
            if ("Películas".equalsIgnoreCase(tipoSel)) {
                pred = pred.and(i -> "Película".equalsIgnoreCase(i.getTipo()));
            } else if ("Funciones".equalsIgnoreCase(tipoSel)) {
                pred = pred.and(i -> "Función".equalsIgnoreCase(i.getTipo()));
            } else if ("Productos".equalsIgnoreCase(tipoSel)) {
                pred = pred.and(i -> "Producto".equalsIgnoreCase(i.getTipo()));
            }
        }

        // Filtro por estado
        if (estadoSel != null && !"Todos".equalsIgnoreCase(estadoSel)) {
            pred = pred.and(i -> i.getEstado() != null &&
                    i.getEstado().equalsIgnoreCase(estadoSel));
        }

        // Filtro por rango de fechas usando fechaReferencia (solo afecta a funciones por ahora)
        if (desde != null) {
            pred = pred.and(i -> i.getFechaReferencia() == null ||
                    !i.getFechaReferencia().isBefore(desde));
        }
        if (hasta != null) {
            pred = pred.and(i -> i.getFechaReferencia() == null ||
                    !i.getFechaReferencia().isAfter(hasta));
        }

        datosFiltrados.setAll(datosBase.filtered(pred));
        tablaReporte.setItems(datosFiltrados);

        long totalFilas = datosFiltrados.size();

        // Resumen simplificado: sin unidades vendidas ni ingresos
        lblResumen.setText("Registros mostrados: " + totalFilas);
    }

    // ===== helpers nvl =====
    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String nvl(String s, String def) {
        return (s == null || s.trim().isEmpty()) ? def : s;
    }

    // ===== clase interna para la tabla =====
    public static class ItemReporte {
        private String tipo;
        private String nombre;
        private String detalle;
        private String estado;
        private int cantidadInventario;
        private int cantidadVendida;
        private BigDecimal ingresos;
        private LocalDate fechaReferencia;

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDetalle() { return detalle; }
        public void setDetalle(String detalle) { this.detalle = detalle; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public int getCantidadInventario() { return cantidadInventario; }
        public void setCantidadInventario(int cantidadInventario) { this.cantidadInventario = cantidadInventario; }

        public int getCantidadVendida() { return cantidadVendida; }
        public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }

        public BigDecimal getIngresos() { return ingresos; }
        public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }

        public LocalDate getFechaReferencia() { return fechaReferencia; }
        public void setFechaReferencia(LocalDate fechaReferencia) { this.fechaReferencia = fechaReferencia; }
    }
}
