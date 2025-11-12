package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import sigmacine.aplicacion.facade.AuthFacade;
import javafx.application.Platform;
import java.util.ArrayDeque;
import java.util.Deque;


public class RegisterController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label feedback;

    private final AuthFacade auth;
    private ControladorControlador coordinador; 

    public RegisterController(AuthFacade auth) {
        this.auth = auth;
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            try {
                if (txtNombre == null) return;
                Parent root = txtNombre.getScene() != null ? txtNombre.getScene().getRoot() : null;
                if (root == null) return;

                Button bReg = findButtonByText(root, "Registrar");
                if (bReg != null) {
                    bReg.setOnAction(e -> onRegistrar());
                }
                Button bCan = findButtonByText(root, "Cancelar");
                if (bCan != null) {
                    bCan.setOnAction(e -> onCancelar());
                }
            } catch (Exception ex) {
            }
        });
    }

    private Button findButtonByText(Parent root, String text) {
        Deque<Node> dq = new ArrayDeque<>();
        dq.add(root);
        while (!dq.isEmpty()) {
            Node n = dq.poll();
            if (n instanceof Button) {
                Button b = (Button) n;
                if (text.equals(b.getText())) return b;
            }
            if (n instanceof Parent) {
                for (Node child : ((Parent) n).getChildrenUnmodifiable()) dq.add(child);
            }
        }
        return null;
    }

    public void setCoordinador(ControladorControlador c) {
        this.coordinador = c;
    }

    @FXML
    public void onRegistrar() {
        String nombre = txtNombre.getText().trim();
        String email  = txtEmail.getText().trim();
        String pass   = txtPassword.getText();

        try {
            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("Completa todos los campos");
            }
            int id = auth.registrar(nombre, email, pass);

            feedback.setStyle("-fx-text-fill: #2e7d32;");  // verde éxito
            feedback.setText("¡Registro exitoso! ID = " + id);

            if (coordinador != null) coordinador.mostrarLogin(); 
        } catch (IllegalArgumentException ex) {
                    feedback.setStyle("-fx-text-fill: #e53935;");  // rojo error
                    feedback.setText(ex.getMessage());
        } catch (Exception ex) {
        
            feedback.setStyle("-fx-text-fill: #e53935;");
            feedback.setText("Error registrando: " + ex.getMessage());
    }
}

    @FXML
    public void onCancelar() {
        if (coordinador != null) {
            sigmacine.aplicacion.data.UsuarioDTO guest = new sigmacine.aplicacion.data.UsuarioDTO();
            guest.setId(0);
            guest.setEmail("");
            guest.setNombre("Invitado");
            coordinador.mostrarHome(guest);
        }
    }

    public void bindRoot(Parent root) {
        if (root == null) return;
        try {
            if (txtNombre == null) {
                Node n = root.lookup("#txtNombre");
                if (n instanceof TextField) txtNombre = (TextField) n;
            }
            if (txtEmail == null) {
                Node n = root.lookup("#txtEmail");
                if (n instanceof TextField) txtEmail = (TextField) n;
            }
            if (txtPassword == null) {
                Node n = root.lookup("#txtPassword");
                if (n instanceof PasswordField) txtPassword = (PasswordField) n;
            }
            if (feedback == null) {
                Node n = root.lookup("#feedback");
                if (n instanceof Label) feedback = (Label) n;
            }

            Node registrarBtn = root.lookup("[onAction='#onRegistrar']");
            if (registrarBtn == null) {
                for (Node cand : root.lookupAll(".button")) {
                    if (cand instanceof Button && "Registrar".equals(((Button)cand).getText())) {
                        registrarBtn = cand; break;
                    }
                }
            }
            if (registrarBtn instanceof Button) ((Button)registrarBtn).setOnAction(e -> onRegistrar());

            Node cancelarBtn = root.lookup("[onAction='#onCancelar']");
            if (cancelarBtn == null) {
                for (Node cand : root.lookupAll(".button")) {
                    if (cand instanceof Button && "Cancelar".equals(((Button)cand).getText())) {
                        cancelarBtn = cand; break;
                    }
                }
            }
            if (cancelarBtn instanceof Button) ((Button)cancelarBtn).setOnAction(e -> onCancelar());

        } catch (Exception ex) {
            System.err.println("Error binding RegisterController root: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
