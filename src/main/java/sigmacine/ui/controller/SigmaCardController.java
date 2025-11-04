package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sigmacine.aplicacion.service.SigmaCardService;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.session.Session;

import java.math.BigDecimal;

public class SigmaCardController {

	@FXML private TextField txtIdentificacion;
	@FXML private PasswordField txtPin;
	@FXML private Button btnRegistrarme;
	@FXML private Button btnRecargar;
	@FXML private Button btnVerMonto;
	@FXML private Label lblFeedback;

	private final SigmaCardService service = new SigmaCardService();

	@FXML
	private void initialize() {
		if (btnRegistrarme != null) btnRegistrarme.setOnAction(e -> onRegistrarme());
		if (btnRecargar != null) btnRecargar.setOnAction(e -> onRecargar());
		if (btnVerMonto != null) btnVerMonto.setOnAction(e -> onVerMonto());
		if (lblFeedback != null) lblFeedback.setText("");
	}

	private String getIdInput() {
		return txtIdentificacion != null ? txtIdentificacion.getText().trim() : "";
	}

	private String getPinInput() {
		return txtPin != null ? txtPin.getText().trim() : "";
	}

	public void onRegistrarme() {
		try {
			String id = getIdInput();
			String pin = getPinInput();
			if (id.isBlank() || pin.isBlank()) {
				if (lblFeedback != null) {
					lblFeedback.setStyle("-fx-text-fill: #e53935;");
					lblFeedback.setText("Completa identificación y PIN");
				}
				return;
			}
			boolean created = service.registrarCard(id, pin);
			if (created) {
				if (lblFeedback != null) {
					lblFeedback.setStyle("-fx-text-fill: #2e7d32;");
					lblFeedback.setText("SigmaCard registrada correctamente.");
				}
			} else {
				if (lblFeedback != null) {
					lblFeedback.setStyle("-fx-text-fill: #e53935;");
					lblFeedback.setText("La SigmaCard ya existe.");
				}
			}
		} catch (Exception ex) {
			if (lblFeedback != null) {
				lblFeedback.setStyle("-fx-text-fill: #e53935;");
				lblFeedback.setText("Error: " + ex.getMessage());
			}
		}
	}

	public void onRecargar() {
		try {
			String id = getIdInput();
			if (id.isBlank()) { if (lblFeedback != null) lblFeedback.setText("Ingresa identificación para recargar"); return; }
			javafx.scene.control.TextInputDialog d = new javafx.scene.control.TextInputDialog();
			d.setTitle("Recargar SigmaCard");
			d.setHeaderText(null);
			d.setContentText("Monto (ej. 10.00):");
			var res = d.showAndWait();
			if (res.isEmpty()) return;
			String montoStr = res.get();
			BigDecimal monto = new BigDecimal(montoStr.trim());
			BigDecimal nuevo = service.recargar(id, monto);
			if (lblFeedback != null) {
				lblFeedback.setStyle("-fx-text-fill: #2e7d32;");
				lblFeedback.setText("Recargado. Nuevo saldo: " + service.format(nuevo));
			}
		} catch (NumberFormatException nfe) {
			if (lblFeedback != null) { lblFeedback.setStyle("-fx-text-fill: #e53935;"); lblFeedback.setText("Monto inválido"); }
		} catch (Exception ex) {
			if (lblFeedback != null) { lblFeedback.setStyle("-fx-text-fill: #e53935;"); lblFeedback.setText("Error: " + ex.getMessage()); }
		}
	}

	public void onVerMonto() {
		try {
			String id = getIdInput();
			if (id.isBlank()) { if (lblFeedback != null) lblFeedback.setText("Ingresa identificación para ver monto"); return; }
			BigDecimal saldo = service.consultarSaldo(id);
			String text = "Saldo: " + service.format(saldo);
			Alert a = new Alert(Alert.AlertType.INFORMATION, text);
			a.setHeaderText(null);
			a.setTitle("Saldo SigmaCard");
			a.showAndWait();
		} catch (Exception ex) {
			if (lblFeedback != null) { lblFeedback.setStyle("-fx-text-fill: #e53935;"); lblFeedback.setText("Error: " + ex.getMessage()); }
		}
	}

	public void initializeWithSession() {
		try {
			UsuarioDTO cur = Session.getCurrent();
			if (cur != null && txtIdentificacion != null && (txtIdentificacion.getText() == null || txtIdentificacion.getText().isBlank())) {
				txtIdentificacion.setText(String.valueOf(cur.getId()));
			}
		} catch (Exception ignore) {}
	}

	@FXML
	private void onBrandClick() {
		try {
			javafx.stage.Window w = null;
			try { if (btnRegistrarme != null && btnRegistrarme.getScene() != null) w = btnRegistrarme.getScene().getWindow(); } catch (Exception ignore) {}
			if (w instanceof javafx.stage.Stage s) {
				try {
					java.net.URL url = SigmaCardController.class.getResource("/sigmacine/ui/views/cliente_home.fxml");
					if (url != null) {
						javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
						javafx.scene.Parent root = loader.load();
						Object ctrl = loader.getController();
						if (ctrl != null) {
							try {
								var method = ctrl.getClass().getMethod("initializeWithSession");
								method.invoke(ctrl);
							} catch (NoSuchMethodException ignore) {
							} catch (Exception ex) {
							}
						}
						double wdt = s.getWidth() > 0 ? s.getWidth() : 900;
						double hgt = s.getHeight() > 0 ? s.getHeight() : 600;
						s.setScene(new javafx.scene.Scene(root, wdt, hgt));
						s.setTitle("Cliente - Sigma Cine");
						return;
					}
				} catch (Exception ignore) {
				}
				s.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void openModal(javafx.stage.Window owner) {
		javafx.application.Platform.runLater(() -> {
			try {
				java.net.URL url = SigmaCardController.class.getResource("/sigmacine/ui/views/SigmaCard.fxml");
				if (url == null) {
					javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No se encontró SigmaCard.fxml");
					a.setHeaderText(null);
					a.showAndWait();
					return;
				}

                
				javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
				Parent root = loader.load();
				Object ctrl = loader.getController();
				if (ctrl instanceof SigmaCardController scc) {
					try { scc.initializeWithSession(); } catch (Exception ignore) {}
				}
				javafx.stage.Stage stage = new javafx.stage.Stage();
				if (owner != null) stage.initOwner(owner);
				stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.setScene(new javafx.scene.Scene(root));
				stage.setTitle("Mi SigmaCard");
				stage.showAndWait();
			} catch (Exception ex) {
				ex.printStackTrace();
				javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No se pudo abrir SigmaCard: " + ex.getMessage());
				a.setHeaderText(null);
				a.showAndWait();
			}
		});
	}

	public static void openAsScene(javafx.stage.Stage stage) {
		if (stage == null) return;
		try {
			java.net.URL url = SigmaCardController.class.getResource("/sigmacine/ui/views/SigmaCard.fxml");
			if (url == null) {
				javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No se encontró SigmaCard.fxml");
				a.setHeaderText(null);
				a.showAndWait();
				return;
			}
			javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
			Parent root = loader.load();
			Object ctrl = loader.getController();
			if (ctrl instanceof SigmaCardController scc) {
				try { scc.initializeWithSession(); } catch (Exception ignore) {}
			}
			javafx.scene.Scene current = stage.getScene();
			double w = current != null ? current.getWidth() : 900;
			double h = current != null ? current.getHeight() : 600;
			stage.setScene(new javafx.scene.Scene(root, w, h));
			stage.setTitle("Mi SigmaCard");
			stage.setMaximized(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No se pudo abrir SigmaCard: " + ex.getMessage());
			a.setHeaderText(null);
			a.showAndWait();
		}
	}
}
