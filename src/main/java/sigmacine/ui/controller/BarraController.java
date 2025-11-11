package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.geometry.Side;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.session.Session;
import sigmacine.aplicacion.service.CarritoService;
import sigmacine.aplicacion.data.CompraProductoDTO;

public class BarraController {

    @FXML private HBox barra;
    @FXML private ImageView logoImage;
    @FXML private Button btnCartelera;
    @FXML private Button btnConfiteria;
    @FXML private Button btnSigmaCard;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnCart;
    @FXML private Button btnRegistrarse;
    @FXML private Button btnIniciarSesion;
    
    // MenuButton único que incluye ícono + nombre
    @FXML private MenuButton menuUsuario;
    @FXML private Label lblNombreUsuario;
    
    @FXML private MenuItem miHistorial;
    @FXML private MenuItem miCerrarSesion;

    private ContextMenu carritoDropdown;
    private static BarraController instance;

    @FXML
    public void initialize() {
        instance = this;
        configurarEventos();
        actualizarEstadoSesion();
        configurarCarritoDropdown();
    }

    public static BarraController getInstance() {
        return instance;
    }

    private void configurarEventos() {
        logoImage.setOnMouseClicked(this::onLogoClick);

        btnCartelera.setOnAction(e -> {
            navegarACartelera();
            marcarBotonActivo("cartelera");
        });
        
        btnConfiteria.setOnAction(e -> {
            navegarAConfiteria();
            marcarBotonActivo("confiteria");
        });
        
        btnSigmaCard.setOnAction(e -> {
            navegarASigmaCard();
            marcarBotonActivo("sigmacard");
        });

        btnBuscar.setOnAction(e -> realizarBusqueda());
        txtBuscar.setOnAction(e -> realizarBusqueda());

        btnRegistrarse.setOnAction(e -> {
            if (!Session.isLoggedIn()) {
                navegarARegistro();
                marcarBotonActivo("registro");
            }
        });
        
        btnIniciarSesion.setOnAction(e -> {
            if (Session.isLoggedIn()) {
                // Ya no navega a Mi Cuenta, el menú se abre al hacer clic
            } else {
                navegarALogin();
                marcarBotonActivo("login");
            }
        });

        miCerrarSesion.setOnAction(e -> cerrarSesion());
        miHistorial.setOnAction(e -> navegarAHistorial());
        
        configurarMenuUsuario();
    }

    private void configurarMenuUsuario() {
        for (MenuItem item : menuUsuario.getItems()) {
            if (item instanceof SeparatorMenuItem) continue;
            
            if (item != miHistorial && item != miCerrarSesion) {
                item.setOnAction(e -> manejarItemMenu(item.getText()));
            }
        }
    }

    private void manejarItemMenu(String textoItem) {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador == null) return;

        switch (textoItem) {
            case "Mi Cuenta":
                coordinador.mostrarMiCuenta();
                break;
            case "Buscar Tiquetes":
                coordinador.mostrarCartelera();
                marcarBotonActivo("cartelera");
                break;
            case "Confitería":
                coordinador.mostrarConfiteria();
                marcarBotonActivo("confiteria");
                break;
            case "Promociones":
                mostrarMensajeProximamente("Promociones");
                break;
            case "Información Personal":
                coordinador.mostrarMiCuenta();
                break;
            case "SigmaCard":
                coordinador.mostrarSigmaCard();
                marcarBotonActivo("sigmacard");
                break;
            case "Historial de compras":
                coordinador.mostrarHistorialCompras();
                break;
            case "Notificaciones":
                mostrarMensajeProximamente("Notificaciones");
                break;
            case "Cartelera":
                coordinador.mostrarCartelera();
                marcarBotonActivo("cartelera");
                break;
        }
    }

    public void marcarBotonActivo(String botonId) {
        resetearEstilosBotones();
        
        switch (botonId) {
            case "cartelera":
                aplicarEstiloActivo(btnCartelera);
                break;
            case "confiteria":
                aplicarEstiloActivo(btnConfiteria);
                break;
            case "sigmacard":
                aplicarEstiloActivo(btnSigmaCard);
                break;
            case "login":
                aplicarEstiloActivo(btnIniciarSesion);
                break;
            case "registro":
                if (!Session.isLoggedIn()) {
                    aplicarEstiloActivo(btnRegistrarse);
                }
                break;
        }
    }

    private void aplicarEstiloActivo(Button boton) {
        if (boton != null) {
            boton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); " +
                          "-fx-text-fill: white; " +
                          "-fx-border-radius: 4px; " +
                          "-fx-background-radius: 4px; " +
                          "-fx-padding: 8 12 8 12;");
        }
        aplicarEstiloRegistrarse();
    }

    private void resetearEstilosBotones() {
        Button[] botones = {btnCartelera, btnConfiteria, btnSigmaCard, btnIniciarSesion};
        
        for (Button boton : botones) {
            if (boton != null) {
                boton.setStyle("-fx-background-color: transparent; " +
                              "-fx-text-fill: white; " +
                              "-fx-padding: 8 12 8 12;");
            }
        }
        
        aplicarEstiloRegistrarse();
    }

    private void aplicarEstiloRegistrarse() {
        if (Session.isLoggedIn()) {
            // Cuando hay sesión: botón OPACO pero VISIBLE
            btnRegistrarse.setStyle("-fx-background-color: transparent; " +
                                   "-fx-text-fill: rgba(255, 255, 255, 0.5); " +
                                   "-fx-padding: 8 12 8 12;");
            btnRegistrarse.setDisable(true);
        } else {
            // Cuando no hay sesión: botón normal
            btnRegistrarse.setStyle("-fx-background-color: transparent; " +
                                   "-fx-text-fill: white; " +
                                   "-fx-padding: 8 12 8 12;");
            btnRegistrarse.setDisable(false);
        }
    }

    private void configurarCarritoDropdown() {
        carritoDropdown = new ContextMenu();
        carritoDropdown.setStyle("-fx-pref-width: 300; -fx-padding: 10;");
        
        // Actualizar el contenido cada vez que se abre
        btnCart.setOnAction(e -> {
            actualizarCarritoDropdown();
            carritoDropdown.show(btnCart, Side.BOTTOM, 0, 0);
        });
    }
    
    private void actualizarCarritoDropdown() {
        carritoDropdown.getItems().clear();
        
        Label titulo = new Label("Carrito de Compras");
        titulo.setStyle("-fx-font-size: 14; -fx-padding: 5 0 10 0;");
        
        Separator separator = new Separator();
        
        CustomMenuItem tituloItem = new CustomMenuItem(titulo, false);
        CustomMenuItem separatorItem = new CustomMenuItem(separator, false);
        tituloItem.setHideOnClick(false);
        separatorItem.setHideOnClick(false);
        
        carritoDropdown.getItems().addAll(tituloItem, separatorItem);
        
        // Obtener items del carrito real
        CarritoService carrito = CarritoService.getInstance();
        var items = carrito.getItems();
        
        if (items.isEmpty()) {
            Label vacio = new Label("Tu carrito está vacío");
            vacio.setStyle("-fx-padding: 15 0; -fx-text-fill: #666; -fx-alignment: center;");
            CustomMenuItem vacioItem = new CustomMenuItem(vacio, false);
            vacioItem.setHideOnClick(false);
            carritoDropdown.getItems().add(vacioItem);
        } else {
            // Mostrar cada item del carrito
            for (CompraProductoDTO item : items) {
                String texto = String.format("%s x%d - $%.2f", 
                    item.getNombre() != null ? item.getNombre() : "Item", 
                    item.getCantidad(),
                    item.getPrecioUnitario().doubleValue() * item.getCantidad());
                
                Label lblItem = new Label(texto);
                lblItem.setStyle("-fx-padding: 3 0; -fx-text-fill: #333;");
                CustomMenuItem itemMenu = new CustomMenuItem(lblItem, false);
                itemMenu.setHideOnClick(false);
                carritoDropdown.getItems().add(itemMenu);
            }
            
            // Mostrar total
            Label total = new Label(String.format("Total: $%.2f", carrito.getTotal().doubleValue()));
            total.setStyle("-fx-padding: 10 0 5 0; -fx-font-weight: bold;");
            CustomMenuItem totalItem = new CustomMenuItem(total, false);
            totalItem.setHideOnClick(false);
            carritoDropdown.getItems().add(totalItem);
        }
        
        Button btnVerCarrito = new Button("Ver carrito completo");
        btnVerCarrito.setStyle("-fx-background-color: #8A2F24; -fx-text-fill: white; -fx-pref-width: 100%;");
        btnVerCarrito.setOnAction(e -> {
            carritoDropdown.hide();
            navegarACarritoCompleto();
        });
        
        CustomMenuItem botonItem = new CustomMenuItem(btnVerCarrito, false);
        botonItem.setHideOnClick(false);
        carritoDropdown.getItems().add(botonItem);
    }

    private void navegarACartelera() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarCartelera();
        }
    }

    private void navegarAConfiteria() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarConfiteria();
        }
    }

    private void navegarASigmaCard() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarSigmaCard();
        }
    }

    private void navegarACarrito() {
        // Este método ya no se necesita porque el clic se maneja en configurarCarritoDropdown()
        // pero lo mantenemos para compatibilidad
        actualizarCarritoDropdown();
        if (carritoDropdown != null) {
            carritoDropdown.show(btnCart, Side.BOTTOM, 0, 0);
        }
    }

    private void navegarACarritoCompleto() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarCarritoCompleto();
        }
    }

    private void navegarAHistorial() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarHistorialCompras();
        }
    }

    private void navegarARegistro() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarRegistro();
        }
    }

    private void navegarALogin() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarLogin();
        }
    }

    private void navegarAMiCuenta() {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarMiCuenta();
        }
    }

    @FXML
    private void onLogoClick(MouseEvent event) {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarPaginaInicial();
            resetearEstilosBotones();
        }
    }

    private void realizarBusqueda() {
        String textoBusqueda = txtBuscar.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            ControladorControlador coordinador = ControladorControlador.getInstance();
            if (coordinador != null) {
                coordinador.mostrarResultadosBusqueda(textoBusqueda);
            }
        }
    }

    private void cerrarSesion() {
        try {
            java.lang.reflect.Method m = Session.class.getMethod("logout");
            m.invoke(null);
        } catch (NoSuchMethodException e1) {
            try {
                java.lang.reflect.Method m2 = Session.class.getMethod("setCurrent", sigmacine.aplicacion.data.UsuarioDTO.class);
                m2.invoke(null, (Object) null);
            } catch (NoSuchMethodException e2) {
                try {
                    java.lang.reflect.Field f = Session.class.getDeclaredField("current");
                    f.setAccessible(true);
                    f.set(null, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        actualizarEstadoSesion();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarPaginaInicial();
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sesión cerrada");
        alert.setHeaderText(null);
        alert.setContentText("Has cerrado sesión correctamente");
        alert.showAndWait();
    }

    public void actualizarEstadoSesion() {
        boolean loggedIn = Session.isLoggedIn();
        
        // REGISTRARSE SIEMPRE VISIBLE - pero opaco cuando hay sesión
        btnRegistrarse.setVisible(true);
        btnRegistrarse.setManaged(true);

        if (loggedIn) {
            // Cuando hay sesión: ocultar "Iniciar Sesión", mostrar MenuButton con nombre
            btnIniciarSesion.setVisible(false);
            btnIniciarSesion.setManaged(false);
            menuUsuario.setVisible(true);
            menuUsuario.setManaged(true);
            
            // Obtener y mostrar NOMBRE COMPLETO del usuario en el MenuButton
            UsuarioDTO usuario = Session.getCurrent();
            if (usuario != null) {
                String nombreCompleto = usuario.getNombre();
                if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                    // Mostrar el NOMBRE COMPLETO en el Label dentro del MenuButton
                    lblNombreUsuario.setText(nombreCompleto);
                } else {
                    // Si no hay nombre, mostrar el email o "Usuario"
                    String email = usuario.getEmail();
                    if (email != null && !email.isEmpty()) {
                        lblNombreUsuario.setText(email);
                    } else {
                        lblNombreUsuario.setText("Usuario");
                    }
                }
            }
            
        } else {
            // Cuando no hay sesión: mostrar "Iniciar Sesión", ocultar MenuButton
            btnIniciarSesion.setVisible(true);
            btnIniciarSesion.setManaged(true);
            menuUsuario.setVisible(false);
            menuUsuario.setManaged(false);
            btnIniciarSesion.setText("Iniciar Sesión");
            lblNombreUsuario.setText("Usuario");
        }
        
        // Aplicar estilo correcto al botón Registrarse (opaco cuando hay sesión)
        aplicarEstiloRegistrarse();
    }

    public void refrescarBarra() {
        actualizarEstadoSesion();
    }

    public void limpiarBusqueda() {
        txtBuscar.clear();
    }

    public TextField getCampoBusqueda() {
        return txtBuscar;
    }

    private void mostrarMensajeProximamente(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Próximamente");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad estará disponible próximamente.");
        alert.showAndWait();
    }
}