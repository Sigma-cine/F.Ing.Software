package sigmacine.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.facade.AuthFacade;

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
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Ya existe una sesión iniciada. Cierra sesión para registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/registrarse.fxml"));
            // Try to obtain a controller instance from the factory BEFORE loading the FXML
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

            if (esAdmin) {
                AdminController c = loader.getController();
                c.init(usuario, this);
                stage.setTitle("Sigma Cine - Admin");
            } else {
                ClienteController c = loader.getController();
                c.init(usuario);
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

    public void mostrarClienteHomeConPopup(UsuarioDTO usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
            if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();

            ClienteController cliente = loader.getController();
            cliente.init(usuario);
            cliente.setCoordinador(this);

            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
                stage.setScene(new Scene(root, w > 0 ? w : 900, h > 0 ? h : 600));
                stage.setMaximized(true);
                stage.setTitle("Sigma Cine - Cliente");
                stage.show();

            Preferences prefs = Preferences.userNodeForPackage(ControladorControlador.class);
            boolean yaMostrado = prefs.getBoolean("cityPopupShown", false);
            if (!yaMostrado && !cityPopupShownInSession) {
                FXMLLoader popup = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/Ciudad.fxml"));
                if (controllerFactory != null) popup.setControllerFactory(controllerFactory);
                Parent popupRoot = popup.load();
                CiudadController cc = popup.getController();
                cc.setOnCiudadSelected(ciudad -> {
                    cliente.init(usuario, ciudad);
                    stage.setTitle("Sigma Cine - Cliente (" + ciudad + ")");
                });

                Stage dialog = new Stage();
                dialog.initOwner(stage);
                dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
                dialog.setTitle("Seleccione su ciudad");
                dialog.setScene(new javafx.scene.Scene(popupRoot));
                dialog.setResizable(false);
                dialog.centerOnScreen();
                prefs.putBoolean("cityPopupShown", true);
                cityPopupShownInSession = true;
                dialog.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error cargando cliente_home con popup de ciudad", e);
        }
    }
}