package sigmacine.ui.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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

public class CombosController implements Initializable {

    @FXML private GridPane gridCombos;
    @FXML private TextField txtBuscar;

    private UsuarioDTO usuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Solo mantener el Singleton para marcar la página activa
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("combos");
        }
        
        if (txtBuscar != null) txtBuscar.setOnAction(e -> loadCombos(txtBuscar.getText()));
        if (gridCombos != null) gridCombos.getStyleClass().add("menu-grid");
        loadCombos(null);
    }

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }

    private void loadCombos(String filter) {
        try {
            gridCombos.getChildren().clear();
            List<ComboItem> items = fetchCombos(filter);
            int col = 0, row = 0;
            for (ComboItem combo : items) {
                VBox box = buildComboBox(combo);
                gridCombos.add(box, col, row);
                col++; if (col >= 2) { col = 0; row++; }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private VBox buildComboBox(ComboItem combo) {
        ImageView iv = new ImageView();
        iv.setFitWidth(520);
        iv.setFitHeight(252);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        if (combo.image != null) iv.setImage(combo.image);

        javafx.scene.layout.StackPane imgFrame = new javafx.scene.layout.StackPane();
        imgFrame.getStyleClass().add("menu-image-frame");
        imgFrame.setPrefWidth(536);
        imgFrame.setPrefHeight(268);
        imgFrame.getChildren().add(iv);

    javafx.scene.control.Label titleName = new javafx.scene.control.Label(combo.nombre != null ? combo.nombre : "");
    titleName.getStyleClass().add("menu-title");
    titleName.setWrapText(true);
    titleName.setMaxWidth(420);

    javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(combo.precio != null ? String.format("$%.2f", combo.precio.doubleValue()) : "");
    priceLabel.getStyleClass().add("menu-price");
    priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #fff;");

    javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(8, titleName, priceLabel);
    titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    titleBox.setMaxWidth(520);

    javafx.scene.control.Label desc = new javafx.scene.control.Label(combo.descripcion != null ? combo.descripcion : "");
    desc.getStyleClass().add("menu-desc");
    desc.setWrapText(true);
    desc.setMaxWidth(520);

        final IntegerProperty quantity = new SimpleIntegerProperty(1);
        
        // Crear botones con imágenes
        javafx.scene.control.Button btnMinus = new javafx.scene.control.Button();
        javafx.scene.control.Button btnPlus = new javafx.scene.control.Button();
        
        try {
            // Imagen para botón menos
            javafx.scene.image.ImageView minusIcon = new javafx.scene.image.ImageView();
            minusIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/Images/minus.png")));
            minusIcon.setFitWidth(16);
            minusIcon.setFitHeight(16);
            minusIcon.setPreserveRatio(true);
            btnMinus.setGraphic(minusIcon);
            
            // Imagen para botón más
            javafx.scene.image.ImageView plusIcon = new javafx.scene.image.ImageView();
            plusIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/Images/plus.png")));
            plusIcon.setFitWidth(16);
            plusIcon.setFitHeight(16);
            plusIcon.setPreserveRatio(true);
            btnPlus.setGraphic(plusIcon);
            
        } catch (Exception e) {
            // Si las imágenes fallan, usar texto como fallback
            btnMinus.setText("-");
            btnPlus.setText("+");
        }
        
        javafx.scene.control.Label lblQty = new javafx.scene.control.Label("1");

        btnMinus.getStyleClass().add("qty-btn");
        btnPlus.getStyleClass().add("qty-btn");
        lblQty.getStyleClass().add("qty-label");

        btnMinus.setStyle("-fx-text-fill: #222222;");
        btnPlus.setStyle("-fx-text-fill: #222222;");

        btnMinus.setOnAction(e -> {
            if (quantity.get() > 1) {
                quantity.set(quantity.get() - 1);
                lblQty.setText(String.valueOf(quantity.get()));
            }
        });

        btnPlus.setOnAction(e -> {
            if (quantity.get() < 10) {
                quantity.set(quantity.get() + 1);
                lblQty.setText(String.valueOf(quantity.get()));
            }
        });

        javafx.scene.layout.HBox qtyBox = new javafx.scene.layout.HBox(8);
        qtyBox.setAlignment(javafx.geometry.Pos.CENTER);
        qtyBox.getStyleClass().add("qty-selector");
        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);

        // Add button
        Button add = new Button("Agregar al carrito");
        add.getStyleClass().addAll("buy-btn", "menu-add-btn");
        add.setOnAction(e -> {
            
            if (!sigmacine.aplicacion.session.Session.isLoggedIn()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Iniciar Sesión Requerido");
                alert.setHeaderText("Debe iniciar sesión");
                alert.setContentText("Para agregar combos al carrito debe iniciar sesión primero.");
                
                // Aplicar CSS personalizado
                try {
                    alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
                    );
                } catch (Exception ignore) {}
                
                alert.showAndWait();
                return;
            }
            
            int qty = quantity.get();
            String itemName = combo.nombre;
            var dto = new CompraProductoDTO(combo.id, itemName, qty, combo.precio);
            CarritoService.getInstance().addItemConsolidated(dto);
            
            // Mostrar confirmación
            javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Combo añadido al carrito");
            confirmacion.setHeaderText("¡Combo agregado correctamente!");
            
            // Aplicar CSS personalizado
            try {
                confirmacion.getDialogPane().getStylesheets().add(
                    getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
                );
            } catch (Exception ignore) {}
            
            String mensaje = qty == 1 
                ? "Se añadió 1 " + itemName + " al carrito"
                : "Se añadieron " + qty + " " + itemName + " al carrito";
            
            confirmacion.setContentText(mensaje);
            confirmacion.showAndWait();
        });

        // Center quantity selector and add button together
        javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(12);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER);
        actionBox.getChildren().addAll(qtyBox, add);

    VBox box = new VBox(10, imgFrame, titleBox, desc, actionBox);
        box.setPadding(new Insets(8));
        box.getStyleClass().add("menu-item");
        box.setPrefWidth(540);
        box.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        return box;
    }

    private List<ComboItem> fetchCombos(String filter) {
        List<ComboItem> out = new ArrayList<>();
        try {
            var db = new DatabaseConfig();
            try (Connection cn = db.getConnection();
                PreparedStatement ps = cn.prepareStatement("SELECT ID, NOMBRE, DESCRIPCION, PRECIO_LISTA, IMAGEN_URL, SABORES FROM PRODUCTO WHERE ESTADO_BOOL = TRUE AND TIPO = 'COMBO' ORDER BY ID")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("ID");
                        String nombre = rs.getString("NOMBRE");
                        String descripcion = rs.getString("DESCRIPCION");
                        BigDecimal precio = rs.getBigDecimal("PRECIO_LISTA");
                        String imagenUrl = rs.getString("IMAGEN_URL");
                        String sabores = rs.getString("SABORES");
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
                        out.add(new ComboItem(id, nombre, descripcion, precio, img, sabores));
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return out;
    }

    private Image tryLoadImageFor(String nombre, long id) {
        String byName = "/Images/Combos/" + nombre.replaceAll("\\s+", "_") + ".png";
        try (InputStream is = getClass().getResourceAsStream(byName)) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) {}
        String byId = "/Images/Combos/" + id + ".png";
        try (InputStream is = getClass().getResourceAsStream(byId)) {
            if (is != null) return new Image(is);
        } catch (Exception ignore) {}
        return null;
    }

    private static class ComboItem {
        final Long id; final String nombre; final String descripcion; final BigDecimal precio; final Image image; final String sabores;
        ComboItem(Long id, String nombre, String descripcion, BigDecimal precio, Image image, String sabores) { 
            this.id = id; this.nombre = nombre; this.descripcion = descripcion; this.precio = precio; this.image = image; this.sabores = sabores; 
        }
    }
}