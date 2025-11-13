package sigmacine.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.util.Callback;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.facade.AuthFacade;
import sigmacine.aplicacion.session.Session;

public class ControladorControlador {

    private final Stage stage;
    private final AuthFacade authFacade;
    private Callback<Class<?>, Object> controllerFactory;
    private static ControladorControlador instance;
    private static boolean cityPopupShownInSession = false;

    public ControladorControlador(Stage stage, AuthFacade authFacade) {
        this.stage = stage;
        this.authFacade = authFacade;
        instance = this;
    }

    public void setControllerFactory(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    public static ControladorControlador getInstance() { return instance; }

    public AuthFacade getAuthFacade() { return this.authFacade; }

    public void mostrarClienteHomeConPopup(UsuarioDTO usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();

            ClienteController cliente = loader.getController();
            cliente.init(usuario);
            cliente.setCoordinador(this);

            configurarBarraEnVista(root);

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.setTitle("Sigma Cine - Cliente");
            stage.show();

            mostrarPopupCiudad(usuario);

        } catch (Exception e) {
            throw new RuntimeException("Error cargando cliente_home", e);
        }
    }

        private void mostrarPopupCiudad(UsuarioDTO usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sigmacine/ui/views/ciudad.fxml")
            );
            if (controllerFactory != null) {
                loader.setControllerFactory(controllerFactory);
            }
            Parent popupRoot = loader.load();

            Stage popupStage = new Stage(StageStyle.TRANSPARENT);
            if (stage != null) {
                popupStage.initOwner(stage);
                popupStage.initModality(Modality.WINDOW_MODAL);
            } else {
                popupStage.initModality(Modality.APPLICATION_MODAL);
            }
            popupStage.setResizable(false);

            Scene scene = new Scene(popupRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupStage.setScene(scene);

            scene.setOnKeyPressed(ev -> {
                switch (ev.getCode()) {
                    case ESCAPE -> popupStage.close();
                    default -> { }
                }
            });

            Object controller = loader.getController();
            if (controller instanceof CiudadController ciudadController) {
                ciudadController.setOnCiudadSelected(ciudad -> {
                    sigmacine.aplicacion.session.Session.setSelectedCity(ciudad);
                    popupStage.close();
                });
            }

            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            sigmacine.aplicacion.session.Session.setSelectedCity("Bogotá");
        }
    }

    public void mostrarLogin() {
        try {
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
                mostrarHome(sigmacine.aplicacion.session.Session.getCurrent());
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setCoordinador(this);
            controller.setAuthFacade(authFacade);
            
            try {
                java.lang.reflect.Method m = controller.getClass().getMethod("bindRoot", javafx.scene.Parent.class);
                if (m != null) m.invoke(controller, root);
            } catch (NoSuchMethodException ignore) {}

            stage.setTitle("Sigma Cine - Login");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando login.fxml", e);
        }
    }

    public void mostrarRegistro() {
        try {
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Ya has iniciado sesión");
                a.setHeaderText(null);
                a.setContentText("Ya existe una sesión iniciada. Cierra sesión para registrar una nueva cuenta.");
                a.showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/registrarse.fxml"));
            RegisterController controller = null;
            if (controllerFactory != null) {
                try {
                    Object created = controllerFactory.call(RegisterController.class);
                    if (created instanceof RegisterController) controller = (RegisterController) created;
                } catch (Exception ignored) {}
            }
            if (controller == null) {
                controller = new RegisterController(authFacade);
            }
            controller.setCoordinador(this);
            loader.setController(controller);
            Parent root = loader.load();
            try {
                java.lang.reflect.Method m = controller.getClass().getMethod("bindRoot", javafx.scene.Parent.class);
                if (m != null) m.invoke(controller, root);
            } catch (NoSuchMethodException ignore) {}
            stage.setTitle("Sigma Cine - Registrarse");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando registrarse.fxml", e);
        }
    }

    public void mostrarBienvenida(UsuarioDTO usuario) {
        mostrarHome(usuario);
    }

    public void mostrarHome(UsuarioDTO usuario) {
        try {
            boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
            String fxml = esAdmin
                ? "/sigmacine/ui/views/admin_dashboard.fxml"
                : "/sigmacine/ui/views/pagina_inicial.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();

            configurarBarraEnVista(root);

            if (esAdmin) {
                AdminController c = loader.getController();
                c.init(usuario, this);
                stage.setTitle("Sigma Cine - Admin");
            } else {
                ClienteController c = loader.getController();
                // Obtener la ciudad de la sesión
                String ciudad = sigmacine.aplicacion.session.Session.getSelectedCity();
                if (ciudad != null && !ciudad.isEmpty()) {
                    c.init(usuario, ciudad);
                } else {
                    c.init(usuario);
                }
                c.setCoordinador(this);
                stage.setTitle("Sigma Cine - Cliente");
            }

            Label lbl = (Label) root.lookup("#welcomeLabel");
            if (lbl != null) lbl.setText("Bienvenido al Cine Sigma");

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando vista home por rol", e);
        }
    }

    public void mostrarResultadosBusqueda(String texto) {
        try {
            sigmacine.infraestructura.configDataBase.DatabaseConfig db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto != null ? texto : "");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            configurarBarraEnVista(root);
            
            ResultadosBusquedaController controller = loader.getController();
            controller.setResultados(resultados, texto != null ? texto : "");

            stage.setTitle("Resultados de búsqueda");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error mostrando resultados de búsqueda", e);
        }
    }

    public void mostrarCartelera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/cartelera.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            configurarBarraEnVista(root);
            
            stage.setTitle("Sigma Cine - Cartelera");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando cartelera.fxml", e);
        }
    }

    public void mostrarConfiteria() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/menu.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            configurarBarraEnVista(root, "confiteria");
            
            stage.setTitle("Sigma Cine - Confitería");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando menu.fxml", e);
        }
    }

    public void mostrarSigmaCard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/SigmaCard.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            configurarBarraEnVista(root);
            
            stage.setTitle("Sigma Cine - Sigma Card");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando sigma_card.fxml", e);
        }
    }

    public void mostrarCarrito() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            Stage carritoStage = new Stage();
            carritoStage.initOwner(stage);
            carritoStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            carritoStage.setTitle("Carrito de Compras");
            carritoStage.setScene(new Scene(root));
            carritoStage.setResizable(false);
            carritoStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando verCarrito.fxml", e);
        }
    }

    public void mostrarHistorialCompras() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCompras.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            VerHistorialController controller = loader.getController();
            if (controller != null) {
                sigmacine.aplicacion.data.UsuarioDTO usuario = sigmacine.aplicacion.session.Session.getCurrent();
                if (usuario != null && usuario.getEmail() != null) {
                    controller.setUsuarioEmail(usuario.getEmail());
                }
            }
            
            configurarBarraEnVista(root, "historial");
            
            stage.setTitle("Sigma Cine - Historial de Compras");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando historial de compras", e);
        }
    }

    public void mostrarMiCuenta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/mi_cuenta.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            
            configurarBarraEnVista(root);
            
            stage.setTitle("Sigma Cine - Mi Cuenta");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error cargando mi_cuenta.fxml", e);
        }
    }

    private void configurarBarraEnVista(Parent root) {
        configurarBarraEnVista(root, null);
    }
    
    private void configurarBarraEnVista(Parent root, String botonActivo) {
        try {
            BarraController barraController = obtenerBarraController(root);
            if (barraController != null && botonActivo != null) {
                barraController.marcarBotonActivo(botonActivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BarraController obtenerBarraController(Parent root) {
        try {
            HBox barra = (HBox) root.lookup("#barra");
            if (barra != null) {
                Object controller = barra.getUserData();
                if (controller instanceof BarraController) {
                    return (BarraController) controller;
                }
                
                controller = barra.getProperties().get("controller");
                if (controller instanceof BarraController) {
                    return (BarraController) controller;
                }
            }
            
            return buscarBarraControllerRecursivamente(root);
        } catch (Exception e) {
            return null;
        }
    }

    private BarraController buscarBarraControllerRecursivamente(Parent parent) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof HBox && "barra".equals(node.getId())) {
                Object controller = ((HBox) node).getUserData();
                if (controller instanceof BarraController) {
                    return (BarraController) controller;
                }
                controller = ((HBox) node).getProperties().get("controller");
                if (controller instanceof BarraController) {
                    return (BarraController) controller;
                }
            }
            if (node instanceof Parent) {
                BarraController result = buscarBarraControllerRecursivamente((Parent) node);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public void mostrarPaginaInicial() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
        if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
        Parent root = loader.load();
        
        configurarBarraEnVista(root);
        
        ClienteController controller = loader.getController();
        if (Session.isLoggedIn()) {
            controller.init(Session.getCurrent());
        } else {
            // Llamar init con null para que cargue el carrusel sin usuario
            controller.init(null);
        }
        controller.setCoordinador(this);
        
        stage.setTitle("Sigma Cine - Inicio");
        javafx.scene.Scene current = stage.getScene();
        double w = current != null ? current.getWidth() : 900;
        double h = current != null ? current.getHeight() : 600;
        stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
        stage.setMaximized(true);
        stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando pagina_inicial.fxml", e);
    }
}

public void mostrarCarritoCompleto() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/verCarrito.fxml"));
        if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
        Parent root = loader.load();
        
        configurarBarraEnVista(root);
        
        stage.setTitle("Sigma Cine - Carrito");
        javafx.scene.Scene current = stage.getScene();
        double w = current != null ? current.getWidth() : 900;
        double h = current != null ? current.getHeight() : 600;
        stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
        stage.setMaximized(true);
        stage.show();
    } catch (Exception e) {
        throw new RuntimeException("Error cargando verCarrito.fxml", e);
    }
    }
}