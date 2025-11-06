package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.repository.UsuarioRepository;

import java.util.List;

public class DetalleCompraController {

    @FXML private Label lblId;
    @FXML private Label lblFecha;
    @FXML private Label lblTotal;
    @FXML private Label lblSede;
    @FXML private Label lblFuncion;
    @FXML private Label lblBoletos;
    @FXML private Label lblProductos;
    @FXML private VBox boletosList;
    @FXML private VBox productosList;

    private final HistorialCompraDTO dto;
    private final UsuarioRepository repo;

    public DetalleCompraController(HistorialCompraDTO dto, UsuarioRepository repo) {
        this.dto = dto;
        this.repo = repo;
    }

    @FXML
    public void initialize() {
        if (dto == null) return;
        lblId.setText(dto.getCompraId() != null ? dto.getCompraId().toString() : "N/A");
        lblFecha.setText(dto.getCompraFecha() != null ? dto.getCompraFecha().toString() : "N/A");
        lblTotal.setText(dto.getTotal() != null ? String.format("%.2f", dto.getTotal().doubleValue()) : "0.00");
        lblSede.setText(dto.getSedeCiudad() != null ? dto.getSedeCiudad() : "N/A");
        lblFuncion.setText((dto.getFuncionFecha() != null ? dto.getFuncionFecha().toString() : "-") + " " + (dto.getFuncionHora() != null ? dto.getFuncionHora().toString() : ""));
        if (repo != null && dto.getCompraId() != null) {
            List<Boleto> boletos = repo.obtenerBoletosPorCompra(dto.getCompraId());
            List<CompraProductoDTO> productos = repo.obtenerProductosPorCompra(dto.getCompraId());
            lblBoletos.setText(String.valueOf(boletos.size()));
            lblProductos.setText(String.valueOf(productos.size()));
            if (boletosList != null) {
                boletosList.getChildren().clear();
                for (Boleto b : boletos) {
                    HBox row = new HBox(10);
                    row.getStyleClass().add("line-item");
                    String asientoVal = b.getAsiento();
                    if (asientoVal == null || asientoVal.isBlank() || "Sin asignar".equals(asientoVal)) asientoVal = "Sin asignar";
                    Label seat = new Label("Asiento: " + asientoVal);
                    seat.getStyleClass().add("small-muted");
                    Label precio = new Label(String.format("$ %.2f", (double) b.getPrecio()));
                    precio.getStyleClass().add("venue-box");
                    row.getChildren().addAll(seat, new Region(), precio);
                    HBox.setHgrow(row.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
                    boletosList.getChildren().add(row);
                }
            }
            if (productosList != null) {
                productosList.getChildren().clear();
                for (CompraProductoDTO p : productos) {
                    HBox row = new HBox(10);
                    row.getStyleClass().add("line-item");
                    Label nombre = new Label(p.getNombre() != null ? p.getNombre() : "Producto");
                    nombre.getStyleClass().add("small-muted");
                    Label cantidad = new Label("x" + p.getCantidad());
                    cantidad.getStyleClass().add("small-muted");
                    Label precio = new Label(String.format("$ %.2f", p.getPrecioUnitario() != null ? p.getPrecioUnitario().doubleValue() : 0.0));
                    precio.getStyleClass().add("venue-box");
                    row.getChildren().addAll(nombre, cantidad, new Region(), precio);
                    HBox.setHgrow(row.getChildren().get(2), javafx.scene.layout.Priority.ALWAYS);
                    productosList.getChildren().add(row);
                }
            }
        } else {
            lblBoletos.setText(String.valueOf(dto.getCantBoletos()));
            lblProductos.setText(String.valueOf(dto.getCantProductos()));
        }
    }
}
