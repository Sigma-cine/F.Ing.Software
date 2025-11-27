package sigmacine.ui.controller;

import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    @FXML private Button btnIniciarSesion;
    
    // MenuButton único que incluye ícono + nombre
    @FXML private MenuButton menuUsuario;
    @FXML private Label lblNombreUsuario;
    
    @FXML private MenuItem miMisBoletas;
    @FXML private MenuItem miHistorial;
    @FXML private MenuItem miCerrarSesion;
    @FXML private MenuItem miMiCuenta;

    private static BarraController instance;
    private javafx.stage.Stage carritoStage;

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
        // El evento del logo ya está configurado en el FXML con onMouseClicked="#onLogoClick"

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
        
        if (miMisBoletas != null) {
            miMisBoletas.setOnAction(e -> navegarAMisBoletas());
        }
        
        if (miHistorial != null) {
            miHistorial.setOnAction(e -> navegarAHistorial());
        }

        if (miMiCuenta != null) {
            miMiCuenta.setOnAction(e -> navegarAMiCuenta());
        }
        
        configurarMenuUsuario();
    }

   private void configurarMenuUsuario() {
    // Limpiamos items actuales
    menuUsuario.getItems().clear();

    // Crear "Mi Cuenta"
    MenuItem miCuenta = new MenuItem("Mi Cuenta");
    miCuenta.setOnAction(e -> {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarMiCuenta(); // Carga infoPersonal.fxml en el contenedor central
        }
    });

    // Crear "Mis Boletas"
    MenuItem misBoletas = new MenuItem("Mis Boletas");
    misBoletas.setOnAction(e -> {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) coordinador.mostrarMisBoletas();
    });

    // Crear "Historial"
    MenuItem historial = new MenuItem("Historial");
    historial.setOnAction(e -> {
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) coordinador.mostrarHistorialCompras();
    });

    // Crear "Cerrar Sesión"
    MenuItem cerrarSesion = new MenuItem("Cerrar Sesión");
    cerrarSesion.setOnAction(e -> cerrarSesion());

    // Agregar todos los items al MenuButton
    menuUsuario.getItems().addAll(miCuenta, misBoletas, historial, new SeparatorMenuItem(), cerrarSesion);
}

    public void marcarBotonActivo(String botonId) {
        resetearEstilosBotones();

        // Mapear id a botones
        Map<String, Button> botonesMap = Map.of(
            "cartelera", btnCartelera,
            "confiteria", btnConfiteria,
            "sigmacard", btnSigmaCard,
            "login", btnIniciarSesion,
            "registro", btnRegistrarse,
            "micuenta", btnIniciarSesion
        );

        Button boton = botonesMap.get(botonId);
        if (boton != null) {
            aplicarEstiloActivo(boton);
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
        // Configurar para mostrar popup FXML debajo del botón
        btnCart.setOnAction(e -> {
            mostrarCarritoPopup();
        });
    }
    
    private void navegarACartelera() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarCartelera();
        }
    }

    private void navegarAConfiteria() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarConfiteria();
        }
    }

    private void navegarASigmaCard() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarSigmaCard();
        }
    }

    private void mostrarCarritoPopup() {
        try {
            // Si ya está abierto, cerrarlo
            if (carritoStage != null && carritoStage.isShowing()) {
                carritoStage.close();
                return;
            }
            
            // Cargar el FXML del carrito
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            javafx.scene.Parent carritoRoot = loader.load();
            
            // Crear un Stage flotante (no Popup)
            carritoStage = new javafx.stage.Stage();
            carritoStage.initOwner(btnCart.getScene().getWindow());
            carritoStage.initModality(javafx.stage.Modality.NONE); // no bloquea
            carritoStage.setResizable(false);
            carritoStage.setTitle("Carrito");
            carritoStage.setScene(new javafx.scene.Scene(carritoRoot));
            
            // Agregar listener para cerrar con ESC
            carritoStage.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
                if (ev.getCode() == javafx.scene.input.KeyCode.ESCAPE) carritoStage.close();
            });
            
            // Obtener la posición del botón del carrito
            javafx.geometry.Bounds bounds = btnCart.localToScreen(btnCart.getBoundsInLocal());
            
            // Posicionar debajo del botón del carrito
            carritoStage.setX(bounds.getMinX() - 250);
            carritoStage.setY(bounds.getMaxY() + 5);
            
            carritoStage.show();
            carritoStage.toFront();
        } catch (Exception ex) {
            System.err.println("Error al mostrar carrito: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void navegarAHistorial() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarHistorialCompras();
        }
    }

    private void navegarAMisBoletas() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarMisBoletas();
        }
    }

    private void navegarARegistro() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            coordinador.mostrarRegistro();
        }
    }

    private void navegarALogin() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            // Guardar la escena actual antes de ir al login
            javafx.stage.Stage stage = coordinador.getMainStage();
            if (stage != null) {
                javafx.scene.Scene currentScene = stage.getScene();
                coordinador.mostrarLoginConEscenaAnterior(currentScene);
            } else {
                coordinador.mostrarLogin();
            }
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
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            // Verificar si hay sesión activa
            boolean haySession = Session.isLoggedIn();
            UsuarioDTO usuario = Session.getCurrent();
            
            if (haySession && usuario != null) {
                coordinador.mostrarHome(usuario);
            } else {
                coordinador.mostrarPaginaInicial();
            }
            marcarBotonActivo("inicio");
        }
    }

    private void realizarBusqueda() {
        // Detener cualquier trailer que se esté reproduciendo
        VerDetallePeliculaController.stopCurrentGlobalPlayer();
        
        String textoBusqueda = txtBuscar.getText().trim();
        ControladorControlador coordinador = ControladorControlador.getInstance();
        if (coordinador != null) {
            // Si está vacío, muestra todas las películas; si tiene texto, filtra
            coordinador.mostrarResultadosBusqueda(textoBusqueda);
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
        
        // Crear popup personalizado con los colores de la app
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("✓ Sesión Cerrada");
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(25);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-padding: 40; -fx-background-color: #1a1a1a;");
        
        // Ícono de éxito
        javafx.scene.control.Label icono = new javafx.scene.control.Label("✓");
        icono.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 60; -fx-font-weight: bold;");
        
        // Contenedor con borde para el ícono
        javafx.scene.layout.VBox iconoContainer = new javafx.scene.layout.VBox();
        iconoContainer.setAlignment(javafx.geometry.Pos.CENTER);
        iconoContainer.setStyle("-fx-background-color: #2d5016; -fx-background-radius: 50; -fx-pref-width: 100; -fx-pref-height: 100;");
        iconoContainer.getChildren().add(icono);
        
        javafx.scene.control.Label titulo = new javafx.scene.control.Label("Sesión Cerrada");
        titulo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24; -fx-font-weight: bold;");
        
        javafx.scene.control.Label mensaje = new javafx.scene.control.Label("Has cerrado sesión correctamente");
        mensaje.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(300);
        mensaje.setAlignment(javafx.geometry.Pos.CENTER);
        
        javafx.scene.control.Button btnAceptar = new javafx.scene.control.Button("Aceptar");
        btnAceptar.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 12 40; -fx-background-radius: 5; -fx-font-weight: bold;");
        btnAceptar.setOnAction(e -> popup.close());
        
        content.getChildren().addAll(iconoContainer, titulo, mensaje, btnAceptar);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(content, 400, 400);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.showAndWait();
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

    public static void setInstance(BarraController instance) {
    }

}


