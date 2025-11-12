package sigmacine.ui.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sigmacine.aplicacion.data.FuncionDisponibleDTO;
import sigmacine.aplicacion.data.ProductoDTO;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.aplicacion.service.admi.GestionProductosService;

import java.io.File;
import java.util.List;

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

  private final ObservableList<ProductoDTO> modelo = FXCollections.observableArrayList();

  private GestionProductosService gestionProductosService;

  public void setGestionProductosService(GestionProductosService servicio) {
    this.gestionProductosService = servicio;
    onListarTodos();
  }

  public AgregarItemController(GestionProductosService service) {
    if (service == null)
      throw new IllegalArgumentException("GestionProductosService nulo");

    this.gestionProductosService = service;
  }

  @FXML
  public void initialize() {
    tblProductos.setItems(modelo);

    colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getProductoId())));

    colProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreProducto()));
    colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcionProducto()));
    colSabores.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSabores()));
    colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
    colPrecio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrecioLista().toString()));
    colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

    onListarTodos();
  }

  @FXML
  public void onListarTodos() {
    if (gestionProductosService == null) {
      System.out.println("gestion es nulo");
      return;
    }
    List<ProductoDTO> lista = gestionProductosService.listarTodas();
    if (tblProductos != null)
      tblProductos.setItems(FXCollections.observableArrayList(lista));
  }
}
