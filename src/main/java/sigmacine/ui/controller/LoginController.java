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
    @FXML private Button loginButton;
    @FXML private Hyperlink registrarLink;
    @FXML private Label feedback;

    private ControladorControlador coordinador;
    private AuthFacade authFacade;
    // Optional callback to run when login succeeds. If set, it takes precedence
    // over the default behaviour of navigating to home via the coordinator.
    private Runnable onSuccess;

    public void setCoordinador(ControladorControlador coordinador) { this.coordinador = coordinador; }
    public void setAuthFacade(AuthFacade authFacade) { this.authFacade = authFacade; }
    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }

    @FXML
    private void initialize() {
        if (loginButton != null) loginButton.setOnAction(e -> { onLogin(); });
        if (registrarLink != null) registrarLink.setOnAction(e -> { if (coordinador != null) coordinador.mostrarRegistro(); });
    }
    @FXML
public void onIrARegistro() {
    coordinador.mostrarRegistro();
}


    public void onLogin() {
        String email = emailField.getText();
        String pass  = passwordField.getText();

        // Basic empty check
        if (email == null || email.isBlank() || pass == null || pass.isBlank()) {
            feedback.setStyle("-fx-text-fill: #d00;");
            feedback.setText("Completa correo y contraseña.");
            return;
        }

        // Validate email format early to give clear feedback
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
        // store in session so other controllers know user is logged in
        Session.setCurrent(usuario);
        // If a custom success callback is provided (e.g. the caller opened login as a modal),
        // let it handle post-login navigation/cleanup. Otherwise fall back to the coordinator.
        if (onSuccess != null) {
            try { onSuccess.run(); } catch (Exception ex) { ex.printStackTrace(); }
            return;
        }
        if (coordinador != null) {
            // Después de autenticarse, mostrar la vista principal para el usuario
            // Ir directamente al home del usuario (no volver a abrir el popup de ciudad)
            coordinador.mostrarHome(usuario);
        }
    }

    /**
     * Bind controls by looking them up in the provided root. Useful when the FXML
     * is not injecting fields (e.g., controller was set programmatically or fx:ids
     * differ). This will only set references that are currently null.
     */
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
                    // fallback: find button by text
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

            // Ensure handlers are attached
            if (loginButton != null) loginButton.setOnAction(e -> { onLogin(); });
            if (registrarLink != null) registrarLink.setOnAction(e -> { if (coordinador != null) coordinador.mostrarRegistro(); });
        } catch (Exception ex) {
            System.err.println("Error binding LoginController root: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
