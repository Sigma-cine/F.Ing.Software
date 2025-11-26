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
		BarraController barraController = BarraController.getInstance();
		if (barraController != null) {
			barraController.marcarBotonActivo("sigmacard");
		}
		UsuarioDTO usuario = Session.getCurrent();
		if (usuario == null) {
			if (lblFeedback != null) lblFeedback.setStyle("-fx-text-fill: #e53935;");
			if (lblFeedback != null) lblFeedback.setText("Debes iniciar sesión para ver tu SigmaCard.");
			if (btnRegistrarme != null) btnRegistrarme.setDisable(true);
			if (btnRecargar != null) btnRecargar.setDisable(true);
			if (btnVerMonto != null) btnVerMonto.setDisable(true);
			if (txtIdentificacion != null) txtIdentificacion.setDisable(true);
			if (txtPin != null) txtPin.setDisable(true);
			return;
		}
		// Setear automáticamente el id
		if (txtIdentificacion != null) {
			txtIdentificacion.setText(String.valueOf(usuario.getId()));
			txtIdentificacion.setDisable(true);
		}
		// Consultar si el usuario tiene SigmaCard en la BD
		SigmaCardService sigmaCardService = new SigmaCardService();
		boolean tieneCard = sigmaCardService.tieneCard(usuario.getId());

		if (tieneCard) {
			// Mostrar solo recargar y ver saldo
			if (btnRegistrarme != null) btnRegistrarme.setVisible(false);
			if (txtPin != null) txtPin.setVisible(false);
			if (btnRecargar != null) btnRecargar.setVisible(true);
			if (btnVerMonto != null) btnVerMonto.setVisible(true);
			if (lblFeedback != null) lblFeedback.setStyle("-fx-text-fill: #2e7d32;");
			if (lblFeedback != null) lblFeedback.setText("Tarjeta activa. Puedes recargar o consultar saldo.");
		} else {
			// Mostrar solo registrar y pin
			if (btnRegistrarme != null) btnRegistrarme.setVisible(true);
			if (txtPin != null) txtPin.setVisible(true);
			if (btnRecargar != null) btnRecargar.setVisible(false);
			if (btnVerMonto != null) btnVerMonto.setVisible(false);
			if (lblFeedback != null) lblFeedback.setText("Registra tu SigmaCard para comenzar a usarla.");
		}

		if (btnRegistrarme != null) btnRegistrarme.setOnAction(e -> {
			// Al registrar, ocultar registro y mostrar recargar/ver saldo
			if (txtPin != null && !txtPin.getText().isBlank()) {
				boolean ok = sigmaCardService.registrarCard(String.valueOf(usuario.getId()), txtPin.getText());
				if (ok) {
					btnRegistrarme.setVisible(false);
					txtPin.setVisible(false);
					if (btnRecargar != null) btnRecargar.setVisible(true);
					if (btnVerMonto != null) btnVerMonto.setVisible(true);
					if (lblFeedback != null) lblFeedback.setStyle("-fx-text-fill: #2e7d32;");
					if (lblFeedback != null) lblFeedback.setText("¡Tarjeta registrada! Ahora puedes recargar o consultar saldo.");
				} else {
					if (lblFeedback != null) lblFeedback.setStyle("-fx-text-fill: #e53935;");
					if (lblFeedback != null) lblFeedback.setText("No se pudo registrar la tarjeta. Intenta de nuevo.");
				}
			} else {
				if (lblFeedback != null) lblFeedback.setStyle("-fx-text-fill: #e53935;");
				if (lblFeedback != null) lblFeedback.setText("Debes ingresar un PIN para registrar la tarjeta.");
			}
		});
		if (btnRecargar != null) btnRecargar.setOnAction(e -> onRecargar());
		if (btnVerMonto != null) btnVerMonto.setOnAction(e -> onVerMonto());
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
			
			// Crear popup personalizado con los colores de la app
			javafx.stage.Stage popup = new javafx.stage.Stage();
			popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			popup.setTitle("💳 Recargar SigmaCard");
			
			javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(20);
			content.setAlignment(javafx.geometry.Pos.CENTER);
			content.setStyle("-fx-padding: 30; -fx-background-color: #1a1a1a;");
			
			javafx.scene.control.Label titulo = new javafx.scene.control.Label("💰 Recargar tu SigmaCard");
			titulo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20; -fx-font-weight: bold;");
			
			javafx.scene.control.Label instruccion = new javafx.scene.control.Label("Ingresa el monto a recargar:");
			instruccion.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
			
			javafx.scene.control.TextField txtMonto = new javafx.scene.control.TextField();
			txtMonto.setPromptText("Ejemplo: 50000");
			txtMonto.setMaxWidth(250);
			txtMonto.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #666666; -fx-font-size: 14; -fx-padding: 10;");
			
			javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(15);
			botones.setAlignment(javafx.geometry.Pos.CENTER);
			
			javafx.scene.control.Button btnAceptar = new javafx.scene.control.Button("Recargar");
			btnAceptar.setStyle("-fx-background-color: #8B2E21; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 25; -fx-background-radius: 5; -fx-font-weight: bold;");
			
			javafx.scene.control.Button btnCancelar = new javafx.scene.control.Button("Cancelar");
			btnCancelar.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 25; -fx-background-radius: 5;");
			
			final boolean[] recargado = {false};
			
			btnAceptar.setOnAction(e -> {
				try {
					String montoStr = txtMonto.getText().trim();
					if (montoStr.isEmpty()) {
						javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
						alert.setTitle("Campo vacío");
						alert.setHeaderText(null);
						alert.setContentText("Debes ingresar un monto");
						alert.showAndWait();
						return;
					}
					BigDecimal monto = new BigDecimal(montoStr);
					BigDecimal nuevo = service.recargar(id, monto);
					recargado[0] = true;
					if (lblFeedback != null) {
						lblFeedback.setStyle("-fx-text-fill: #4CAF50;");
						lblFeedback.setText("✓ Recargado. Nuevo saldo: " + service.format(nuevo));
					}
					popup.close();
				} catch (NumberFormatException nfe) {
					javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
					alert.setTitle("Monto inválido");
					alert.setHeaderText(null);
					alert.setContentText("Ingresa un número válido");
					alert.showAndWait();
				} catch (Exception ex) {
					javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText("Error al recargar: " + ex.getMessage());
					alert.showAndWait();
				}
			});
			
			btnCancelar.setOnAction(e -> popup.close());
			
			botones.getChildren().addAll(btnAceptar, btnCancelar);
			content.getChildren().addAll(titulo, instruccion, txtMonto, botones);
			
			javafx.scene.Scene scene = new javafx.scene.Scene(content, 400, 250);
			popup.setScene(scene);
			popup.setResizable(false);
			popup.showAndWait();
			
		} catch (Exception ex) {
			if (lblFeedback != null) { lblFeedback.setStyle("-fx-text-fill: #e53935;"); lblFeedback.setText("Error: " + ex.getMessage()); }
		}
	}

	public void onVerMonto() {
		try {
			String id = getIdInput();
			if (id.isBlank()) { if (lblFeedback != null) lblFeedback.setText("Ingresa identificación para ver monto"); return; }
			BigDecimal saldo = service.consultarSaldo(id);
			
			// Crear popup personalizado con los colores de la app
			javafx.stage.Stage popup = new javafx.stage.Stage();
			popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			popup.setTitle("💳 Saldo SigmaCard");
			
			javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(25);
			content.setAlignment(javafx.geometry.Pos.CENTER);
			content.setStyle("-fx-padding: 40; -fx-background-color: #1a1a1a;");
			
			javafx.scene.control.Label titulo = new javafx.scene.control.Label("💰 Tu Saldo Actual");
			titulo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20; -fx-font-weight: bold;");
			
			// Contenedor con estilo para el saldo
			javafx.scene.layout.VBox saldoBox = new javafx.scene.layout.VBox(10);
			saldoBox.setAlignment(javafx.geometry.Pos.CENTER);
			saldoBox.setStyle("-fx-background-color: #8B2E21; -fx-padding: 30; -fx-background-radius: 10;");
			
			javafx.scene.control.Label lblSaldo = new javafx.scene.control.Label(service.format(saldo));
			lblSaldo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 36; -fx-font-weight: bold;");
			
			javafx.scene.control.Label lblSubtitulo = new javafx.scene.control.Label("Saldo disponible");
			lblSubtitulo.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14;");
			
			saldoBox.getChildren().addAll(lblSaldo, lblSubtitulo);
			
			javafx.scene.control.Button btnCerrar = new javafx.scene.control.Button("Cerrar");
			btnCerrar.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 30; -fx-background-radius: 5;");
			btnCerrar.setOnAction(e -> popup.close());
			
			content.getChildren().addAll(titulo, saldoBox, btnCerrar);
			
			javafx.scene.Scene scene = new javafx.scene.Scene(content, 400, 350);
			popup.setScene(scene);
			popup.setResizable(false);
			popup.showAndWait();
			
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
