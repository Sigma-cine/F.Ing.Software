package sigmacine.ui.controller;

import javafx.fxml.FXMLLoader;
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
    // Global instance helper so other controllers can delegate to the app coordinator
    private static ControladorControlador instance;
    // Guard para la sesión actual: evita mostrar el popup repetidas veces durante la misma ejecución
    private static boolean cityPopupShownInSession = false;

    public ControladorControlador(Stage stage, AuthFacade authFacade) {
        this.stage = stage;
        this.authFacade = authFacade;
        instance = this;
    }

    public static ControladorControlador getInstance() { return instance; }

    public AuthFacade getAuthFacade() { return this.authFacade; }

    public void mostrarLogin() {
        // if already logged in, show home instead
        try {
            if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
                mostrarHome(sigmacine.aplicacion.session.Session.getCurrent());
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setCoordinador(this);
            controller.setAuthFacade(authFacade);

            // If controller provides a bindRoot method, call it so controllers can attach
            // handlers/lookups for FXMLs that use absolute layouts or were not injecting fields.
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
            // already logged in: show info dialog instead of registration
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Ya existe una sesión iniciada. Cierra sesión para registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/registrarse.fxml"));

        // Instanciamos el controller pasando AuthFacade
        RegisterController controller = new RegisterController(authFacade);
        controller.setCoordinador(this); // pásale el coordinador
        loader.setController(controller); // <-- clave

    Parent root = loader.load();
    // call bindRoot on controller if available so RegisterController can lookup nodes
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
            Parent root = loader.load();

            if (esAdmin) {
                AdminController c = loader.getController();
                c.init(usuario, this);
                stage.setTitle("Sigma Cine - Admin");
            } else {
                ClienteController c = loader.getController();
                // Use init(usuario) so the controller triggers cargarPeliculasInicio() and
                // the posters are loaded for the home screen.
                c.init(usuario);
                c.setCoordinador(this);
                stage.setTitle("Sigma Cine - Cliente");
            }

            // Mensaje de bienvenida estándar (si la vista tiene el label)
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

    /** Busca películas por título y muestra la vista de resultados de búsqueda */
    public void mostrarResultadosBusqueda(String texto) {
        try {
            // crear repo temporal para búsqueda
            sigmacine.infraestructura.configDataBase.DatabaseConfig db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(texto != null ? texto : "");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/resultados_busqueda.fxml"));
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

    /** Carga la vista cliente_home y abre un popup modal para seleccionar la ciudad al inicio */
    public void mostrarClienteHomeConPopup(UsuarioDTO usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/pagina_inicial.fxml"));
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

            // Mostrar el popup de ciudad solo la primera vez que se ejecuta la aplicación
            Preferences prefs = Preferences.userNodeForPackage(ControladorControlador.class);
            boolean yaMostrado = prefs.getBoolean("cityPopupShown", false);
            if (!yaMostrado && !cityPopupShownInSession) {
                FXMLLoader popup = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/ciudad.fxml"));
                Parent popupRoot = popup.load();
                CiudadController cc = popup.getController();
                cc.setOnCiudadSelected(ciudad -> {
                    // cuando se seleccione la ciudad, informa al cliente
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
                // marcar como mostrado para no volver a abrir en ejecuciones posteriores
                prefs.putBoolean("cityPopupShown", true);
                cityPopupShownInSession = true;
                dialog.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error cargando cliente_home con popup de ciudad", e);
        }
    }
}