package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Side;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.session.Session;

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
    @FXML private StackPane stackSesion;
    @FXML private Button btnIniciarSesion;
    @FXML private MenuButton menuPerfil;
    @FXML private MenuItem miHistorial;
    @FXML private MenuItem miCerrarSesion;

    private ContextMenu carritoDropdown;

    @FXML
    public void initialize() {
        configurarEventos();
        actualizarEstadoSesion();
        configurarCarritoDropdown();
    }

    private void configurarEventos() {
        logoImage.setOnMouseClicked(this::onLogoClick);

        btnCartelera.setOnAction(e -> navegarACartelera());
        btnConfiteria.setOnAction(e -> navegarAConfiteria());
        btnSigmaCard.setOnAction(e -> navegarASigmaCard());

        btnBuscar.setOnAction(e -> realizarBusqueda());
        txtBuscar.setOnAction(e -> realizarBusqueda());

        btnCart.setOnAction(e -> navegarACarrito());

        btnRegistrarse.setOnAction(e -> navegarARegistro());
        btnIniciarSesion.setOnAction(e -> navegarALogin());

        miCerrarSesion.setOnAction(e -> cerrarSesion());
        miHistorial.setOnAction(e -> navegarAHistorial());
        
        configurarMenuPerfil();
    }

    private void configurarCarritoDropdown() {
        carritoDropdown = new ContextMenu();
        carritoDropdown.setStyle("-fx-pref-width: 300; -fx-padding: 10;");
        
        Label titulo = new Label("Carrito de Compras");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 5 0 10 0;");
        
        Separator separator = new Separator();
        
        Label vacio = new Label("🛒 Tu carrito está vacío");
        vacio.setStyle("-fx-padding: 15 0; -fx-text-fill: #666; -fx-alignment: center;");
        
        Button btnVerCarrito = new Button("Ver carrito completo");
        btnVerCarrito.setStyle("-fx-background-color: #8A2F24; -fx-text-fill: white; -fx-pref-width: 100%;");
        btnVerCarrito.setOnAction(e -> {
            carritoDropdown.hide();
            navegarACarritoCompleto();
        });
        
        CustomMenuItem tituloItem = new CustomMenuItem(titulo, false);
        CustomMenuItem separatorItem = new CustomMenuItem(separator, false);
        CustomMenuItem vacioItem = new CustomMenuItem(vacio, false);
        CustomMenuItem botonItem = new CustomMenuItem(btnVerCarrito, false);
        
        tituloItem.setHideOnClick(false);
        separatorItem.setHideOnClick(false);
        vacioItem.setHideOnClick(false);
        botonItem.setHideOnClick(false);
        
        carritoDropdown.getItems().addAll(tituloItem, separatorItem, vacioItem, botonItem);
    }

    private void configurarMenuPerfil() {
        for (MenuItem item : menuPerfil.getItems()) {
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
                break;
            case "Confitería":
                coordinador.mostrarConfiteria();
                break;
            case "Promociones":
                mostrarMensajeProximamente("Promociones");
                break;
            case "Información Personal":
                coordinador.mostrarMiCuenta();
                break;
            case "SigmaCard":
                coordinador.mostrarSigmaCard();
                break;
            case "Historial de compras":
                coordinador.mostrarHistorialCompras();
                break;
            case "Notificaciones":
                mostrarMensajeProximamente("Notificaciones");
                break;
            case "Cartelera":
                coordinador.mostrarCartelera();
                break;
        }
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

    @FXML
    private void onLogoClick(MouseEvent event) {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarPaginaInicial();
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
                }
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
        }

        actualizarEstadoSesion();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarLogin();
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sesión cerrada");
        alert.setHeaderText(null);
        alert.setContentText("Has cerrado sesión correctamente");
        alert.showAndWait();
    }

    public void actualizarEstadoSesion() {
        boolean loggedIn = Session.isLoggedIn();
        
        btnIniciarSesion.setVisible(!loggedIn);
        btnRegistrarse.setVisible(!loggedIn);
        menuPerfil.setVisible(loggedIn);

        if (loggedIn) {
            String nombreUsuario = Session.getCurrent().getNombre();
            if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                menuPerfil.setText("Hola, " + nombreUsuario.split(" ")[0]);
            } else {
                menuPerfil.setText("Mi Cuenta");
            }
        } else {
            menuPerfil.setText("Mi Cuenta");
        }
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