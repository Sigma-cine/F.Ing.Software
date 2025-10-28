package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.infraestructura.configDataBase.DatabaseConfig;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML private GridPane gridMenu;
    @FXML private TextField txtBuscar;
    @FXML private Button btnVerCarrito;

    private UsuarioDTO usuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (txtBuscar != null) txtBuscar.setOnAction(e -> loadProducts(txtBuscar.getText()));
        if (btnVerCarrito != null) btnVerCarrito.setOnAction(e -> toggleCarrito());
        if (gridMenu != null) gridMenu.getStyleClass().add("menu-grid");
        loadProducts(null);
    }

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }

    private void toggleCarrito() {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            var root = loader.load();
            var stage = new javafx.stage.Stage();
            stage.initOwner(gridMenu.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.NONE);
            stage.setResizable(false);
            stage.setScene(new javafx.scene.Scene((Parent) root));
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadProducts(String filter) {
        try {
            gridMenu.getChildren().clear();
            List<ProductItem> items = fetchProducts(filter);
            int col = 0, row = 0;
            for (ProductItem p : items) {
                VBox box = buildProductBox(p);
                gridMenu.add(box, col, row);
                col++; if (col >= 2) { col = 0; row++; }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private VBox buildProductBox(ProductItem p) {
    ImageView iv = new ImageView();
    // make the image fill the frame completely (no inner gaps)
    // inner area = frame size minus 2*borderWidth (borderWidth 8)
    iv.setFitWidth(524); // 540 - 16
    iv.setFitHeight(204); // 220 - 16
    iv.setPreserveRatio(false);
        iv.setSmooth(true);
        if (p.image != null) iv.setImage(p.image);

        // image frame
        javafx.scene.layout.StackPane imgFrame = new javafx.scene.layout.StackPane();
        imgFrame.getStyleClass().add("menu-image-frame");
    imgFrame.setPrefWidth(540);
    imgFrame.setPrefHeight(220);
        imgFrame.getChildren().add(iv);

    javafx.scene.control.Label title = new javafx.scene.control.Label(p.nombre != null ? p.nombre : "");
    title.getStyleClass().add("menu-title");
        title.setWrapText(true);
        title.setMaxWidth(520);
        title.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    javafx.scene.control.Label desc = new javafx.scene.control.Label(p.descripcion != null ? p.descripcion : "");
    desc.getStyleClass().add("menu-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(520);
        desc.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    javafx.scene.control.Label price = new javafx.scene.control.Label("$" + p.precio.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    price.getStyleClass().add("menu-price");
        price.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    javafx.scene.layout.HBox selectors = new javafx.scene.layout.HBox(12);
    selectors.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    javafx.scene.control.ComboBox<String> cbSize = new javafx.scene.control.ComboBox<>();
    cbSize.getItems().addAll("Tamaño", "Pequeño", "Mediano", "Grande");
    cbSize.getSelectionModel().selectFirst();
    javafx.scene.control.ComboBox<String> cbOpt = new javafx.scene.control.ComboBox<>();
    cbOpt.getItems().addAll("Sabor", "Original", "Dulce", "Picante");
    cbOpt.getSelectionModel().selectFirst();
    cbSize.getStyleClass().add("menu-select");
    cbOpt.getStyleClass().add("menu-select");
    selectors.getChildren().addAll(cbSize, cbOpt);


    Button add = new Button("Agregar al carrito");
    add.getStyleClass().addAll("buy-btn", "menu-add-btn");
        add.setOnAction(e -> {
            var dto = new CompraProductoDTO(p.id, p.nombre, 1, p.precio);
            CarritoService.getInstance().addItem(dto);
        });

        // Arrange: image frame, title, selectors, price, add button, description
        VBox box = new VBox(10, imgFrame, title, selectors, price, add, desc);
        box.setPadding(new Insets(8));
        box.getStyleClass().add("menu-item");
        box.setPrefWidth(540);
        box.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        return box;
    }

    private List<ProductItem> fetchProducts(String filter) {
        List<ProductItem> out = new ArrayList<>();
        try {
            var db = new DatabaseConfig();
            try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement("SELECT ID, NOMBRE, DESCRIPCION, PRECIO_LISTA, IMAGEN_URL FROM PRODUCTO WHERE ESTADO_BOOL = TRUE ORDER BY ID")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("ID");
                        String nombre = rs.getString("NOMBRE");
                        String descripcion = rs.getString("DESCRIPCION");
                        BigDecimal precio = rs.getBigDecimal("PRECIO_LISTA");
                        String imagenUrl = rs.getString("IMAGEN_URL");
                        if (filter != null && !filter.isBlank()) {
                            String f = filter.toLowerCase();
                            if (!(nombre != null && nombre.toLowerCase().contains(f)) && !(descripcion != null && descripcion.toLowerCase().contains(f))) continue;
                        }
                        Image img = null;
                        if (imagenUrl != null && !imagenUrl.isBlank()) {
                            try (InputStream is = getClass().getResourceAsStream(imagenUrl)) {
                                if (is != null) img = new Image(is);
                            } catch (Exception ignore) {}
                        }
                        if (img == null) img = tryLoadImageFor(nombre, id);
                        out.add(new ProductItem(id, nombre, descripcion, precio, img));
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return out;
    }

    private Image tryLoadImageFor(String nombre, long id) {
    String byName = "/Images/Menu/" + nombre.replaceAll("\\s+", "_") + ".png";
        try (InputStream is = getClass().getResourceAsStream(byName)) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) {}
        String byId = "/Images/Menu/" + id + ".png";
        try (InputStream is = getClass().getResourceAsStream(byId)) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) {}
        return null;
    }

    private static class ProductItem {
        final Long id; final String nombre; final String descripcion; final BigDecimal precio; final Image image;
        ProductItem(Long id, String nombre, String descripcion, BigDecimal precio, Image image) { this.id = id; this.nombre = nombre; this.descripcion = descripcion; this.precio = precio; this.image = image; }
    }
}
