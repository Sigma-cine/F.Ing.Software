package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.facade.AuthFacade;
import sigmacine.aplicacion.session.Session;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordFieldVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Hyperlink registrarLink;
    @FXML private Label feedback;

    private ControladorControlador coordinador;
    private AuthFacade authFacade;
    private Runnable onSuccess;
    
    // Contexto para volver a la pantalla anterior
    private javafx.scene.Scene previousScene;
    private Long pendingFuncionId;
    private String pendingTitulo;
    private String pendingHora;
    private String pendingCiudad;
    private String pendingSede;
    private String pendingPosterUrl;
    private java.util.Set<String> pendingOcupados;
    private java.util.Set<String> pendingAccesibles;

    public void setCoordinador(ControladorControlador coordinador) { this.coordinador = coordinador; }
    public void setAuthFacade(AuthFacade authFacade) { this.authFacade = authFacade; }
    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }
    
    public void setPreviousScene(javafx.scene.Scene scene) { 
        this.previousScene = scene; 
        // Guardar el estado de maximización cuando se guarda la escena
        if (scene != null && scene.getWindow() instanceof javafx.stage.Stage) {
            ((javafx.stage.Stage) scene.getWindow()).isMaximized();
        }
    }
    
    public void setPendingFuncionData(Long funcionId, String titulo, String hora, String ciudad, String sede, 
                                      java.util.Set<String> ocupados, java.util.Set<String> accesibles) {
        this.pendingFuncionId = funcionId;
        this.pendingTitulo = titulo;
        this.pendingHora = hora;
        this.pendingCiudad = ciudad;
        this.pendingSede = sede;
        this.pendingOcupados = ocupados;
        this.pendingAccesibles = accesibles;
        this.pendingPosterUrl = null; // Por compatibilidad con código antiguo
    }
    
    public void setPendingFuncionData(Long funcionId, String titulo, String hora, String ciudad, String sede, 
                                      java.util.Set<String> ocupados, java.util.Set<String> accesibles, String posterUrl) {
        this.pendingFuncionId = funcionId;
        this.pendingTitulo = titulo;
        this.pendingHora = hora;
        this.pendingCiudad = ciudad;
        this.pendingSede = sede;
        this.pendingOcupados = ocupados;
        this.pendingAccesibles = accesibles;
        this.pendingPosterUrl = posterUrl;
    }

    @FXML
    private void initialize() {
        BarraController barraController = BarraController.getInstance();
        if (barraController != null) {
            barraController.marcarBotonActivo("login");
        }
        if (loginButton != null) loginButton.setOnAction(e -> { onLogin(); });
        if (cancelButton != null) cancelButton.setOnAction(e -> { onVolver(); });
        if (registrarLink != null) registrarLink.setOnAction(e -> { if (coordinador != null) coordinador.mostrarRegistro(); });
        
        // Configurar el botón de mostrar/ocultar contraseña
        if (togglePasswordBtn != null && passwordField != null && passwordFieldVisible != null) {
            // Sincronizar los textos
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!passwordFieldVisible.getText().equals(newVal)) {
                    passwordFieldVisible.setText(newVal);
                }
            });
            passwordFieldVisible.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!passwordField.getText().equals(newVal)) {
                    passwordField.setText(newVal);
                }
            });
            
            // Acción del botón
            togglePasswordBtn.setOnAction(e -> {
                boolean showPassword = !passwordFieldVisible.isVisible();
                passwordField.setVisible(!showPassword);
                passwordFieldVisible.setVisible(showPassword);
                togglePasswordBtn.setText(showPassword ? "🔒" : "👁");
            });
        }
    }
    @FXML
    public void onIrARegistro() {
        coordinador.mostrarRegistro();
    }

    @FXML
    public void onVolver() {
        if (coordinador != null) {
            coordinador.mostrarPaginaInicial();
        }
    }


    public void onLogin() {
        String email = emailField.getText();
        String pass  = passwordField.getText();

        if (email == null || email.isBlank() || pass == null || pass.isBlank()) {
            feedback.setStyle("-fx-text-fill: #d00;");
            feedback.setText("Completa correo y contraseña.");
            return;
        }

        try {
            new sigmacine.dominio.valueobject.Email(email);
        } catch (IllegalArgumentException ex) {
            feedback.setStyle("-fx-text-fill: #d00;");
            feedback.setText("Email inválido.");
            return;
        }

        UsuarioDTO usuario = authFacade.login(email, pass);
        if (usuario == null) {
            feedback.setStyle("-fx-text-fill: #d00;");
            feedback.setText("Credenciales inválidas.");
            return;
        }

        feedback.setStyle("-fx-text-fill: #090;");
        feedback.setText("Bienvenido al Cine Sigma");
        Session.setCurrent(usuario);
        
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
        if (coordinador != null) {
            coordinador.mostrarHome(usuario);  
        }
        return;
    }

        // Si hay un callback onSuccess, ejecutarlo
        if (onSuccess != null) {
            try { onSuccess.run(); } catch (Exception ex) { ex.printStackTrace(); }
            return;
        }
        
        // Si hay datos de función pendiente, ir a selección de asientos
        if (pendingFuncionId != null && pendingTitulo != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
                loader.setLocation(getClass().getResource("/sigmacine/ui/views/asientos.fxml"));
                javafx.scene.Parent root = loader.load();
                
                AsientosController controller = loader.getController();
                if (controller != null) {
                    controller.setCoordinador(coordinador);
                    controller.setUsuario(usuario);
                    controller.setFuncion(pendingTitulo, pendingHora, pendingOcupados, 
                                         pendingAccesibles, pendingFuncionId, pendingCiudad, pendingSede);
                    if (pendingPosterUrl != null) {
                        controller.setPosterUrl(pendingPosterUrl);
                    }
                }
                
                javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
                javafx.scene.Scene currentScene = stage.getScene();
                double w = currentScene != null ? currentScene.getWidth() : 900;
                double h = currentScene != null ? currentScene.getHeight() : 600;
                stage.setScene(new javafx.scene.Scene(root, w, h));
                stage.setTitle("Selección de Asientos - Sigma Cine");
                stage.setMaximized(true);
                return;
            } catch (Exception ex) {
                System.err.println("Error navegando a asientos: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        // Si hay escena anterior, restaurarla
        if (previousScene != null) {
            try {
                javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
                
                // Obtener las dimensiones de la pantalla
                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                
                // Configurar dimensiones de la ventana ANTES de cambiar la escena
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
                
                // Restaurar la escena
                stage.setScene(previousScene);
                stage.setMaximized(true);
                
                // Asegurar maximización en Platform.runLater
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                    
                    // Actualizar la barra de navegación después de restaurar la escena
                    BarraController barraController = BarraController.getInstance();
                    if (barraController != null) {
                        barraController.actualizarEstadoSesion();
                    }
                });
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // Por defecto, ir al home
        if (coordinador != null) {
            coordinador.mostrarHome(usuario);
        }
    }

    public void bindRoot(Parent root) {
        if (root == null) return;
        try {
            if (emailField == null) {
                Node n = root.lookup("#emailField");
                if (n instanceof TextField) emailField = (TextField) n;
            }
            if (passwordField == null) {
                Node n = root.lookup("#passwordField");
                if (n instanceof PasswordField) passwordField = (PasswordField) n;
            }
            if (loginButton == null) {
                Node n = root.lookup("#loginButton");
                if (n instanceof Button) loginButton = (Button) n;
                else {
                    for (Node cand : root.lookupAll(".button")) {
                        if (cand instanceof Button && "Iniciar Sesión".equals(((Button)cand).getText())) {
                            loginButton = (Button)cand; break;
                        }
                    }
                }
            }
            if (registrarLink == null) {
                Node n = root.lookup("#registrarLink");
                if (n instanceof Hyperlink) registrarLink = (Hyperlink) n;
            }
            if (feedback == null) {
                Node n = root.lookup("#feedback");
                if (n instanceof Label) feedback = (Label) n;
            }

            if (loginButton != null) loginButton.setOnAction(e -> { onLogin(); });
            if (registrarLink != null) registrarLink.setOnAction(e -> { if (coordinador != null) coordinador.mostrarRegistro(); });
        } catch (Exception ex) {
            System.err.println("Error binding LoginController root: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
