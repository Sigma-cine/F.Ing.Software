package sigmacine.ui.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.aplicacion.service.admi.GestionProductosService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AgregarItemController {

  @FXML
  private TableView<ProductoDTO> tblProductos;
  @FXML
  private TableColumn<ProductoDTO, String> colId;
  @FXML
  private TableColumn<ProductoDTO, String> colProducto;
  @FXML
  private TableColumn<ProductoDTO, String> colDescripcion;
  @FXML
  private TableColumn<ProductoDTO, String> colSabores;
  @FXML
  private TableColumn<ProductoDTO, String> colTipo;
  @FXML
  private TableColumn<ProductoDTO, String> colPrecio;
  @FXML
  private TableColumn<ProductoDTO, String> colEstado;

  @FXML
  private TextField campoId;

  @FXML
  private TextField campoNombreProducto;

  @FXML
  private TextArea campoDescripcionProducto;

  @FXML
  private TextField campoSabores;
  @FXML
  private ComboBox<String> comboTipo;
  @FXML
  private TextField campoPrecioLista;
  @FXML
  private ComboBox<String> comboEstado;

  @FXML
  private Label mensajeLabel;
  @FXML
  private TextField campoBusqueda;

  private GestionProductosService gestionProductosService;

  public AgregarItemController() {
  }

  public void setGestionProductosService(GestionProductosService servicio) {
    this.gestionProductosService = servicio;
    inicializarDatos();
  }

  public AgregarItemController(GestionProductosService service) {
    if (service == null)
      throw new IllegalArgumentException("GestionProductosService nulo");
    this.gestionProductosService = service;
  }

  @FXML
  public void initialize() {
    if (campoId == null)
      System.err.println("⚠ ERROR: campoId es NULL. Revisa el fx:id en SceneBuilder.");
    if (campoNombreProducto == null)
      System.err.println("⚠ ERROR: campoNombreProducto es NULL.");
    if (tblProductos == null)
      System.err.println("⚠ ERROR: tblProductos es NULL.");

    if (colId != null)
      colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(nvl(c.getValue().getProductoId()))));
    if (colProducto != null)
      colProducto.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getNombreProducto())));
    if (colDescripcion != null)
      colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getDescripcionProducto())));
    if (colSabores != null)
      colSabores.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getSabores())));
    if (colTipo != null)
      colTipo.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getTipo())));
    if (colPrecio != null)
      colPrecio.setCellValueFactory(c -> new SimpleStringProperty(
          c.getValue().getPrecioLista() == null ? "" : c.getValue().getPrecioLista().toString()));
    if (colEstado != null)
      colEstado.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getEstado())));

    if (tblProductos != null) {
      tblProductos.getSelectionModel().selectedItemProperty()
          .addListener((obs, oldVal, newVal) -> cargarEnFormulario(newVal));
    }

    if (campoId != null) {
      campoId.setEditable(false);
      campoId.setDisable(true);
    }

    if (comboTipo != null) {
      comboTipo.setItems(FXCollections.observableArrayList("COMIDA", "CONFITERIA", "BEBIDA", "COMBO", "OTRO"));
      comboTipo.setValue("SNACK");
    }

    if (comboEstado != null) {
      comboEstado.setItems(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
      comboEstado.setValue("ACTIVO");
    }

    inicializarDatos();
  }

  private void inicializarDatos() {
    if (gestionProductosService != null) {
      onListarTodos();
    }
  }

  @FXML
  public void onListarTodos() {
    if (gestionProductosService == null)
      return;
    List<ProductoDTO> lista = gestionProductosService.listarTodas();
    if (tblProductos != null)
      tblProductos.setItems(FXCollections.observableArrayList(lista));
  }

  @FXML
  private void onBuscar() {
    if (gestionProductosService == null)
      return;
    String q = campoBusqueda == null ? "" : Optional.ofNullable(campoBusqueda.getText()).orElse("").trim();
    List<ProductoDTO> lista;
    if (q.isEmpty()) {
      lista = gestionProductosService.listarTodas();
    } else {
      lista = gestionProductosService.buscarProductos(q);
    }
    if (tblProductos != null)
      tblProductos.setItems(FXCollections.observableArrayList(lista));
  }

  @FXML
  private void onNuevo() {
    limpiarFormulario();
    if (tblProductos != null)
      tblProductos.getSelectionModel().clearSelection();
  }

  @FXML
  private void onCrearNuevoProducto() {
    if (gestionProductosService == null) {
      mostrarMensaje("ERROR", "Servicio no configurado.");
      return;
    }

    ProductoDTO dto = recogerFormulario();
    if (dto.getNombreProducto() == null || dto.getNombreProducto().isEmpty()) {
      mostrarMensaje("WARN", "El nombre es obligatorio.");
      return;
    }

    long idGenerado = System.currentTimeMillis();

    dto.setProductoId(idGenerado);

    try {
      ProductoDTO creado = gestionProductosService.crear(dto);
      mostrarMensaje("OK", "Producto creado ID: " + creado.getProductoId());
      onListarTodos();
      limpiarFormulario();
    } catch (Exception ex) {
      ex.printStackTrace();
      mostrarMensaje("ERROR", "Error: " + ex.getMessage());
    }
  }

  @FXML
  private void onActualizarSeleccion() {
    if (gestionProductosService == null)
      return;
    ProductoDTO seleccion = tblProductos == null ? null : tblProductos.getSelectionModel().getSelectedItem();

    if (seleccion == null || seleccion.getProductoId() == null) {
      mostrarMensaje("WARN", "Selecciona un producto.");
      return;
    }

    ProductoDTO cambios = recogerFormulario();
    if (cambios.getNombreProducto() == null || cambios.getNombreProducto().isEmpty()) {
      mostrarMensaje("WARN", "El nombre es obligatorio.");
      return;
    }

    try {
      gestionProductosService.actualizar(seleccion.getProductoId(), cambios);
      mostrarMensaje("OK", "Producto actualizado.");
      onListarTodos();
    } catch (Exception ex) {
      ex.printStackTrace();
      mostrarMensaje("ERROR", "Error: " + ex.getMessage());
    }
  }

  @FXML
  private void onEliminarSeleccion() {
    if (gestionProductosService == null)
      return;
    ProductoDTO seleccion = tblProductos == null ? null : tblProductos.getSelectionModel().getSelectedItem();
    if (seleccion == null || seleccion.getProductoId() == null) {
      mostrarMensaje("WARN", "Selecciona un producto.");
      return;
    }

    try {
      gestionProductosService.eliminar(seleccion.getProductoId());
      mostrarMensaje("OK", "Eliminado correctamente.");
      onListarTodos();
      limpiarFormulario();
    } catch (Exception ex) {
      mostrarMensaje("ERROR", "Error: " + ex.getMessage());
    }
  }


  private ProductoDTO recogerFormulario() {
    ProductoDTO dto = new ProductoDTO();
    dto.setNombreProducto(texto(campoNombreProducto));
    dto.setDescripcionProducto(textoArea(campoDescripcionProducto));
    dto.setSabores(texto(campoSabores));
    dto.setTipo(comboTipo != null ? comboTipo.getValue() : null);
    dto.setPrecioLista(parseDecimal(campoPrecioLista));
    dto.setEstado(comboEstado != null ? comboEstado.getValue() : "ACTIVO");
    return dto;
  }

  private void cargarEnFormulario(ProductoDTO d) {
    if (d == null) {
      limpiarFormulario();
      return;
    }
    
    setText(campoId, String.valueOf(nvl(d.getProductoId())));

    setText(campoNombreProducto, d.getNombreProducto());
    setTextArea(campoDescripcionProducto, d.getDescripcionProducto());
    setText(campoSabores, d.getSabores());

    if (comboTipo != null)
      comboTipo.setValue(nvl(d.getTipo(), "SNACK"));

    setText(campoPrecioLista, d.getPrecioLista() == null ? "" : d.getPrecioLista().toString());

    if (comboEstado != null)
      comboEstado.setValue(nvl(d.getEstado(), "ACTIVO"));
  }

  private void limpiarFormulario() {
    setText(campoId, "");
    setText(campoNombreProducto, "");
    setTextArea(campoDescripcionProducto, "");
    setText(campoSabores, "");
    if (comboTipo != null)
      comboTipo.setValue("SNACK");
    setText(campoPrecioLista, "");
    if (comboEstado != null)
      comboEstado.setValue("ACTIVO");
  }

  private String texto(TextField t) {
    return t == null || t.getText() == null ? null : t.getText().trim();
  }

  private String textoArea(TextArea t) {
    return t == null || t.getText() == null ? null : t.getText().trim();
  }

  private BigDecimal parseDecimal(TextField t) {
    if (t == null || t.getText() == null)
      return null;
    String s = t.getText().trim();
    if (s.isEmpty())
      return null;
    try {
      return new BigDecimal(s);
    } catch (Exception e) {
      return null;
    }
  }

  private Long nvl(Long l) {
    return l == null ? 0L : l;
  }

  private String nvl(String s) {
    return s == null ? "" : s;
  }

  private String nvl(String s, String def) {
    return (s == null || s.trim().isEmpty()) ? def : s;
  }

  private void setText(TextField t, String v) {
    if (t != null)
      t.setText(v == null ? "" : v);
  }

  private void setTextArea(TextArea t, String v) {
    if (t != null)
      t.setText(v == null ? "" : v);
  }

  private void mostrarMensaje(String tipo, String texto) {
    if (mensajeLabel == null) {
      System.out.println(tipo + ": " + texto); 
      return;
    }
    switch (tipo) {
      case "OK" -> mensajeLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-padding: 5;");
      case "WARN" -> mensajeLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-padding: 5;");
      case "ERROR" -> mensajeLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-padding: 5;");
      default -> mensajeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
    }
    mensajeLabel.setText(texto);
    new Thread(() -> {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException ignored) {
      }
      javafx.application.Platform.runLater(() -> {
        if (mensajeLabel != null)
          mensajeLabel.setText("");
      });
    }).start();
  }
}