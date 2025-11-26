package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class MenuController implements Initializable {

    @FXML private GridPane gridMenu;
    @FXML private TextField txtBuscar;
    @FXML private Button btnVerCombos;

    private UsuarioDTO usuario;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (txtBuscar != null) txtBuscar.setOnAction(e -> loadProducts(txtBuscar.getText()));
        if (btnVerCombos != null) btnVerCombos.setOnAction(e -> showCombos());
        if (gridMenu != null) gridMenu.getStyleClass().add("menu-grid");
        loadProducts(null);
        
        javafx.application.Platform.runLater(() -> {
            BarraController barraController = BarraController.getInstance();
            if (barraController != null) {
                barraController.marcarBotonActivo("confiteria");
            }
        });
    }

    public void setUsuario(UsuarioDTO u) { this.usuario = u; }

    private void showCombos() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVerCombos.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/combos.fxml"));
            Parent root = loader.load();
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 1000;
            double h = current != null ? current.getHeight() : 700;
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle("Sigma Cine - Combos");
            stage.setMaximized(true);
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
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
    iv.setFitWidth(520);
    iv.setFitHeight(252);
    iv.setPreserveRatio(false);
        iv.setSmooth(true);
        if (p.image != null) iv.setImage(p.image);

        javafx.scene.layout.StackPane imgFrame = new javafx.scene.layout.StackPane();
        imgFrame.getStyleClass().add("menu-image-frame");
    imgFrame.setPrefWidth(536);
    imgFrame.setPrefHeight(268);
        imgFrame.getChildren().add(iv);

    javafx.scene.control.Label titleName = new javafx.scene.control.Label(p.nombre != null ? p.nombre : "");
    titleName.getStyleClass().add("menu-title");
        titleName.setWrapText(true);
        titleName.setMaxWidth(420);

    javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(p.precio != null ? String.format("$%.2f", p.precio.doubleValue()) : "");
    priceLabel.getStyleClass().add("menu-price");
    priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #fff;");

    javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(8, titleName, priceLabel);
    titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    titleBox.setMaxWidth(520);

    javafx.scene.control.Label desc = new javafx.scene.control.Label(p.descripcion != null ? p.descripcion : "");
    desc.getStyleClass().add("menu-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(520);

    javafx.scene.layout.HBox selectors = new javafx.scene.layout.HBox(12);
    selectors.setAlignment(javafx.geometry.Pos.CENTER);
    selectors.getStyleClass().add("menu-selectors");
    selectors.setMaxWidth(520);
    selectors.setSpacing(20);
    selectors.setPickOnBounds(false);
    final IntegerProperty quantity = new SimpleIntegerProperty(1);
    
    // Crear botones con imágenes
    javafx.scene.control.Button btnMinus = new javafx.scene.control.Button();
    javafx.scene.control.Label lblQty = new javafx.scene.control.Label("1");
    javafx.scene.control.Button btnPlus = new javafx.scene.control.Button();
    
    // Configurar imagen para botón menos
    try {
        javafx.scene.image.ImageView minusIcon = new javafx.scene.image.ImageView();
        minusIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/Images/minus.png")));
        minusIcon.setFitWidth(16);
        minusIcon.setFitHeight(16);
        minusIcon.setPreserveRatio(true);
        btnMinus.setGraphic(minusIcon);
    } catch (Exception e) {
        // Fallback a texto si no se encuentra la imagen
        btnMinus.setText("−");
    }
    
    // Configurar imagen para botón más
    try {
        javafx.scene.image.ImageView plusIcon = new javafx.scene.image.ImageView();
        plusIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/Images/plus.png")));
        plusIcon.setFitWidth(16);
        plusIcon.setFitHeight(16);
        plusIcon.setPreserveRatio(true);
        btnPlus.setGraphic(plusIcon);
    } catch (Exception e) {
        // Fallback a texto si no se encuentra la imagen
        btnPlus.setText("+");
    }
    
    btnMinus.getStyleClass().add("qty-btn");
    btnPlus.getStyleClass().add("qty-btn");
    lblQty.getStyleClass().add("qty-label");
    
    btnMinus.setStyle("-fx-font-family: 'System'; -fx-font-size: 18px;");
    btnPlus.setStyle("-fx-font-family: 'System'; -fx-font-size: 18px;");

    btnMinus.setMinWidth(45);
    btnMinus.setPrefWidth(45);
    btnPlus.setMinWidth(45);
    btnPlus.setPrefWidth(45);
    btnMinus.setPickOnBounds(false);
    btnPlus.setPickOnBounds(false);
    
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
    qtyBox.setPickOnBounds(false);
    qtyBox.setMinWidth(160);
    qtyBox.setPrefWidth(160);
    qtyBox.setMaxWidth(160);
    qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);

    javafx.scene.control.ComboBox<String> cbOpt = new javafx.scene.control.ComboBox<>();
    if (p.sabores != null && !p.sabores.trim().isEmpty()) {
        String[] parts = p.sabores.split(",");
        for (String s : parts) {
            String v = s.trim().replaceAll("_", " ");
            if (!v.isEmpty()) cbOpt.getItems().add(v);
        }
        cbOpt.setPromptText("Sabor");
    } else {
        cbOpt.getItems().addAll("Original");
        cbOpt.setPromptText("Sabor");
    }

    cbOpt.getStyleClass().add("menu-select");
    cbOpt.setPrefWidth(180);
    cbOpt.setMaxWidth(180); 
    cbOpt.setMinWidth(180);
    
    // Deshabilitar completamente la interacción del ComboBox
    cbOpt.setMouseTransparent(true);
    cbOpt.setFocusTraversable(false);
    cbOpt.setEditable(false);
    
    // Crear contenedor StackPane para superponer la imagen DENTRO del ComboBox
    javafx.scene.layout.StackPane comboContainer = new javafx.scene.layout.StackPane();
    comboContainer.setPrefWidth(180);
    comboContainer.setMaxWidth(180);
    
    // Agregar ComboBox al contenedor
    comboContainer.getChildren().add(cbOpt);
    
    try {
        // Crear imagen clickeable que estará DENTRO del ComboBox
        javafx.scene.image.ImageView comboIcon = new javafx.scene.image.ImageView();
        comboIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/Images/combobox.png")));
        comboIcon.setFitWidth(16);
        comboIcon.setFitHeight(16);
        comboIcon.setPreserveRatio(true);
        
        // Posicionar la imagen dentro del área del ComboBox, lado derecho
        javafx.scene.layout.StackPane.setAlignment(comboIcon, javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.layout.StackPane.setMargin(comboIcon, new javafx.geometry.Insets(0, 8, 0, 0));
        
        // Hacer la imagen clickeable para abrir el dropdown
        comboIcon.setOnMouseClicked(e -> {
            // Temporalmente habilitar el ComboBox para mostrar opciones
            cbOpt.setMouseTransparent(false);
            if (!cbOpt.isShowing()) {
                cbOpt.show();
            } else {
                cbOpt.hide();
            }
            // Inmediatamente volver a deshabilitar
            javafx.application.Platform.runLater(() -> cbOpt.setMouseTransparent(true));
        });
        
        // Estilo visual para indicar que es clickeable
        comboIcon.setStyle("-fx-cursor: hand;");
        
        // Agregar imagen al contenedor (se superpondrá DENTRO del ComboBox)
        comboContainer.getChildren().add(comboIcon);
        
    } catch (Exception e) {
        System.err.println("Error cargando imagen del ComboBox: " + e.getMessage());
    }
    
    selectors.getChildren().addAll(qtyBox, comboContainer);

    Button add = new Button("Agregar al carrito");
    add.getStyleClass().addAll("buy-btn", "menu-add-btn");
    add.setOnAction(e -> {
        if (!sigmacine.aplicacion.session.Session.isLoggedIn()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Iniciar Sesión Requerido");
            alert.setHeaderText("Debe iniciar sesión");
            alert.setContentText("Para agregar productos al carrito debe iniciar sesión primero.");
            
            // Aplicar CSS personalizado
            try {
                alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
                );
            } catch (Exception ignore) {}
            
            alert.showAndWait();
            return;
        }
        
        String selectedSabor = null;
        try { selectedSabor = cbOpt.getSelectionModel().getSelectedItem(); } catch (Exception ignore) {}
        
        // Validar que se haya seleccionado un sabor si el producto tiene sabores disponibles
        if (p.sabores != null && !p.sabores.trim().isEmpty()) {
            if (selectedSabor == null || selectedSabor.trim().isEmpty()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Sabor Requerido");
                alert.setHeaderText("Debe seleccionar un sabor");
                alert.setContentText("Por favor selecciona un sabor antes de añadir el producto al carrito.");
                
                // Aplicar CSS personalizado
                try {
                    alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
                    );
                } catch (Exception ignore2) {}
                
                alert.showAndWait();
                return;
            }
        }
        
        int qty = quantity.get();
        String itemName = p.nombre;
        
        // Crear el DTO con o sin sabor
        var dto = (selectedSabor != null && !selectedSabor.equalsIgnoreCase("Sabor") && !selectedSabor.equalsIgnoreCase("Original") && !selectedSabor.trim().isEmpty())
            ? new CompraProductoDTO(p.id, itemName + " (" + selectedSabor + ")", qty, p.precio, selectedSabor)
            : new CompraProductoDTO(p.id, itemName, qty, p.precio);
        
        // Setear la imagen URL si existe
        if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
            dto.setImageUrl(p.imageUrl);
        }
        
        // Añadir al carrito con lógica de consolidación
        CarritoService.getInstance().addItemConsolidated(dto);
        
        javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        confirmacion.setTitle("Producto añadido al carrito");
        confirmacion.setHeaderText("¡Producto agregado correctamente!");
        
        // Aplicar CSS personalizado
        try {
            confirmacion.getDialogPane().getStylesheets().add(
                getClass().getResource("/sigmacine/ui/views/sigma.css").toExternalForm()
            );
        } catch (Exception ignore) {}
        
        String mensaje = qty == 1 
            ? "Se añadió 1 " + itemName + " al carrito"
            : "Se añadieron " + qty + " " + itemName + " al carrito";
        
        if (selectedSabor != null && !selectedSabor.equalsIgnoreCase("Sabor") && !selectedSabor.equalsIgnoreCase("Original")) {
            mensaje += "\nSabor: " + selectedSabor;
        }
        
        confirmacion.setContentText(mensaje);
        confirmacion.showAndWait();
    });

    VBox box = new VBox(10, imgFrame, titleBox, desc, selectors, add);
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
                PreparedStatement ps = cn.prepareStatement("SELECT ID, NOMBRE, DESCRIPCION, PRECIO_LISTA, IMAGEN_URL, SABORES FROM PRODUCTO WHERE ESTADO_BOOL = TRUE AND TIPO IN ('COMIDA','BEBIDA') ORDER BY ID")) {
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
                        out.add(new ProductItem(id, nombre, descripcion, precio, img, sabores, imagenUrl));
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
        final Long id; final String nombre; final String descripcion; final BigDecimal precio; final Image image; final String sabores; final String imageUrl;
        ProductItem(Long id, String nombre, String descripcion, BigDecimal precio, Image image, String sabores, String imageUrl) { this.id = id; this.nombre = nombre; this.descripcion = descripcion; this.precio = precio; this.image = image; this.sabores = sabores; this.imageUrl = imageUrl; }
    }
}
