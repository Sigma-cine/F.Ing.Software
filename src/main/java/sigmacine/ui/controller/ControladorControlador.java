package sigmacine.ui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private LoadingOverlay loadingOverlay;
    private StackPane rootContainer;

    public ControladorControlador(Stage stage, AuthFacade authFacade) {
        this.stage = stage;
        this.authFacade = authFacade;
        instance = this;
        this.loadingOverlay = new LoadingOverlay();
    }

    public void setControllerFactory(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    public static ControladorControlador getInstance() { return instance; }

    public AuthFacade getAuthFacade() { return this.authFacade; }
    
    public Stage getMainStage() { return this.stage; }
    
    /**
     * Método helper para cargar vistas con indicador de carga
     */
    private void loadViewWithSpinner(String fxmlPath, String title, ViewConfigurer configurer) {
        // Mostrar spinner
        showLoadingOverlay();
        
        // Crear tarea en background
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                if (controllerFactory != null) loader.setControllerFactory(controllerFactory);
                Parent root = loader.load();
                
                // Configurar el controlador (si es necesario)
                if (configurer != null) {
                    configurer.configure(loader.getController(), root);
                }
                
                return root;
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            try {
                Parent root = loadTask.getValue();
                
                // Envolver en StackPane con overlay
                StackPane container = new StackPane();
                container.getChildren().addAll(root, loadingOverlay.getOverlayPane());
                rootContainer = container;
                
                // GUARDAR estado maximizado ANTES de cambiar la escena
                boolean wasMaximized = stage.isMaximized();
                
                javafx.scene.Scene current = stage.getScene();
                double w = current != null ? current.getWidth() : 900;
                double h = current != null ? current.getHeight() : 600;
                
                // Si estaba maximizado, forzar tamaño de pantalla completo antes de setScene
                if (wasMaximized) {
                    javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                    javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                    w = bounds.getWidth();
                    h = bounds.getHeight();
                }
                
                stage.setScene(new Scene(container, w > 0 ? w : 900, h > 0 ? h : 600));
                stage.setTitle(title);
                
                // Si estaba maximizado O si no hay escena previa (primera carga), maximizar
                if (wasMaximized || current == null) {
                    stage.setMaximized(true);
                }
                
                stage.show();
                
                // Forzar maximización nuevamente después de show si es necesario
                if (wasMaximized || current == null) {
                    stage.setMaximized(true);
                }
                
                hideLoadingOverlay();
            } catch (Exception e) {
                hideLoadingOverlay();
                e.printStackTrace();
            }
        });
        
        loadTask.setOnFailed(event -> {
            hideLoadingOverlay();
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            showError("Error al cargar la vista: " + ex.getMessage());
        });
        
        // Ejecutar tarea en thread separado
        new Thread(loadTask).start();
    }
    
    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }
    
    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    @FunctionalInterface
    private interface ViewConfigurer {
        void configure(Object controller, Parent root) throws Exception;
    }

    public void mostrarClienteHomeConPopup(UsuarioDTO usuario) {
        loadViewWithSpinner("/sigmacine/ui/views/pagina_inicial.fxml", "Sigma Cine - Cliente", (controller, root) -> {
            ClienteController cliente = (ClienteController) controller;
            
            // Obtener ciudad de la sesión
            String ciudad = sigmacine.aplicacion.session.Session.getSelectedCity();
            if (ciudad != null && !ciudad.isEmpty()) {
                cliente.init(usuario, ciudad);
            } else {
                cliente.init(usuario);
            }
            cliente.setCoordinador(this);
            configurarBarraEnVista(root);
            
            // Mostrar popup de ciudad DESPUÉS de cargar la pantalla
            Platform.runLater(() -> {
                mostrarPopupCiudadSimple();
            });
        });
    }
    
    private void mostrarClienteHomeConPopup_OLD(UsuarioDTO usuario) {
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

            mostrarPopupCiudad(() -> {});

        } catch (Exception e) {
            throw new RuntimeException("Error cargando cliente_home", e);
        }
    }

        private void mostrarPopupCiudadSimple() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sigmacine/ui/views/ciudad.fxml")
            );
            if (controllerFactory != null) {
                loader.setControllerFactory(controllerFactory);
            }
            Parent popupRoot = loader.load();

            Stage popupStage = new Stage(StageStyle.TRANSPARENT);
            popupStage.initModality(Modality.APPLICATION_MODAL); // Modal a TODA la aplicación
            popupStage.setResizable(false);
            popupStage.setAlwaysOnTop(true); // Siempre arriba

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

        private void mostrarPopupCiudad(Runnable onCiudadSelected) {
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
            
            if (onCiudadSelected != null) {
                Platform.runLater(onCiudadSelected);
            }

        } catch (Exception e) {
            sigmacine.aplicacion.session.Session.setSelectedCity("Bogotá");
            if (onCiudadSelected != null) {
                Platform.runLater(onCiudadSelected);
            }
        }
    }

    public void mostrarLoginConEscenaAnterior(javafx.scene.Scene previousScene) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            javafx.scene.Parent root = loader.load();
            
            LoginController controller = loader.getController();
            if (controller != null) {
                controller.setCoordinador(this);
                controller.setAuthFacade(authFacade);
                controller.setPreviousScene(previousScene);
            }
            
            boolean wasMaximized = stage.isMaximized();
            javafx.scene.Scene currentScene = stage.getScene();
            double w = currentScene != null ? currentScene.getWidth() : 800;
            double h = currentScene != null ? currentScene.getHeight() : 600;
            stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 800, h > 0 ? h : 600));
            stage.setTitle("Iniciar Sesión - Sigma Cine");
            stage.setMaximized(wasMaximized);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando login.fxml", e);
        }
    }
    
    public void mostrarLoginConContexto(Long funcionId, String titulo, String hora, String ciudad, String sede,
                                        java.util.Set<String> ocupados, java.util.Set<String> accesibles) {
        mostrarLoginConContexto(funcionId, titulo, hora, ciudad, sede, ocupados, accesibles, null);
    }
    
    public void mostrarLoginConContexto(Long funcionId, String titulo, String hora, String ciudad, String sede,
                                        java.util.Set<String> ocupados, java.util.Set<String> accesibles, String posterUrl) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/sigmacine/ui/views/login.fxml"));
            javafx.scene.Parent root = loader.load();
            
            LoginController controller = loader.getController();
            if (controller != null) {
                controller.setCoordinador(this);
                controller.setAuthFacade(authFacade);
                controller.setPendingFuncionData(funcionId, titulo, hora, ciudad, sede, ocupados, accesibles, posterUrl);
            }
            
            javafx.scene.Scene currentScene = stage.getScene();
            double w = currentScene != null ? currentScene.getWidth() : 800;
            double h = currentScene != null ? currentScene.getHeight() : 600;
            stage.setScene(new javafx.scene.Scene(root, w > 0 ? w : 800, h > 0 ? h : 600));
            stage.setTitle("Iniciar Sesión - Sigma Cine");
        } catch (Exception e) {
            throw new RuntimeException("Error cargando login.fxml", e);
        }
    }
    
    public void mostrarLogin() {
        if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
            mostrarHome(sigmacine.aplicacion.session.Session.getCurrent());
            return;
        }
        
        loadViewWithSpinner("/sigmacine/ui/views/login.fxml", "Sigma Cine - Login", (controller, root) -> {
            LoginController loginController = (LoginController) controller;
            loginController.setCoordinador(this);
            loginController.setAuthFacade(authFacade);
            
            try {
                java.lang.reflect.Method m = loginController.getClass().getMethod("bindRoot", javafx.scene.Parent.class);
                if (m != null) m.invoke(loginController, root);
            } catch (NoSuchMethodException ignore) {}
        });
    }

    public void mostrarRegistro() {
        if (sigmacine.aplicacion.session.Session.isLoggedIn()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Ya has iniciado sesión");
            a.setHeaderText(null);
            a.setContentText("Ya existe una sesión iniciada. Cierra sesión para registrar una nueva cuenta.");
            a.showAndWait();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sigmacine/ui/views/registrarse.fxml"));
            
            // Crear el controlador manualmente
            RegisterController regController = null;
            if (controllerFactory != null) {
                try {
                    Object created = controllerFactory.call(RegisterController.class);
                    if (created instanceof RegisterController) {
                        regController = (RegisterController) created;
                    }
                } catch (Exception ignored) {}
            }
            if (regController == null) {
                regController = new RegisterController(authFacade);
            }
            regController.setCoordinador(this);
            
            // Establecer el controlador ANTES de cargar
            loader.setController(regController);
            
            // Ahora cargar con spinner
            showLoadingOverlay();
            Parent root = loader.load();
            
            try {
                java.lang.reflect.Method m = regController.getClass().getMethod("bindRoot", javafx.scene.Parent.class);
                if (m != null) m.invoke(regController, root);
            } catch (ReflectiveOperationException ignore) {}
            
            StackPane container = new StackPane();
            container.getChildren().addAll(root, loadingOverlay.getOverlayPane());
            
            stage.setTitle("Sigma Cine - Registrarse");
            javafx.scene.Scene current = stage.getScene();
            double w = current != null ? current.getWidth() : 900;
            double h = current != null ? current.getHeight() : 600;
            stage.setScene(new Scene(container, w > 0 ? w : 900, h > 0 ? h : 600));
            stage.setMaximized(true);
            stage.show();
            
            hideLoadingOverlay();
        } catch (Exception e) {
            hideLoadingOverlay();
            throw new RuntimeException("Error cargando registrarse.fxml", e);
        }
    }

    public void mostrarBienvenida(UsuarioDTO usuario) {
        mostrarHome(usuario);
    }

    public void mostrarHome(UsuarioDTO usuario) {
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        String fxml = esAdmin
            ? "/sigmacine/ui/views/admin_dashboard.fxml"
            : "/sigmacine/ui/views/pagina_inicial.fxml";
        String title = esAdmin ? "Sigma Cine - Admin" : "Sigma Cine - Cliente";
        
        loadViewWithSpinner(fxml, title, (controller, root) -> {
            configurarBarraEnVista(root);

            if (esAdmin) {
                AdminController c = (AdminController) controller;
                c.init(usuario, this);
            } else {
                ClienteController c = (ClienteController) controller;
                String ciudad = sigmacine.aplicacion.session.Session.getSelectedCity();
                if (ciudad != null && !ciudad.isEmpty()) {
                    c.init(usuario, ciudad);
                } else {
                    c.init(usuario);
                }
                c.setCoordinador(this);
            }

            Label lbl = (Label) root.lookup("#welcomeLabel");
            if (lbl != null) lbl.setText("Bienvenido al Cine Sigma");
        });
    }

    public void mostrarResultadosBusqueda(String texto) {
        String textoFinal = texto != null ? texto : "";
        
        loadViewWithSpinner("/sigmacine/ui/views/resultados_busqueda.fxml", "Resultados de búsqueda", (controller, root) -> {
            configurarBarraEnVista(root);
            
            sigmacine.infraestructura.configDataBase.DatabaseConfig db = new sigmacine.infraestructura.configDataBase.DatabaseConfig();
            var repo = new sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc(db);
            var resultados = repo.buscarPorTitulo(textoFinal);
            
            ResultadosBusquedaController c = (ResultadosBusquedaController) controller;
            c.setResultados(resultados, textoFinal);
        });
    }

    public void mostrarCartelera() {
        loadViewWithSpinner("/sigmacine/ui/views/cartelera.fxml", "Sigma Cine - Cartelera", (controller, root) -> {
            configurarBarraEnVista(root);
        });
    }

    public void mostrarConfiteria() {
        loadViewWithSpinner("/sigmacine/ui/views/menu.fxml", "Sigma Cine - Confitería", (controller, root) -> {
            configurarBarraEnVista(root, "confiteria");
        });
    }

    public void mostrarSigmaCard() {
        loadViewWithSpinner("/sigmacine/ui/views/SigmaCard.fxml", "Sigma Cine - Sigma Card", (controller, root) -> {
            configurarBarraEnVista(root);
        });
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
        loadViewWithSpinner("/sigmacine/ui/views/verCompras.fxml", "Sigma Cine - Historial de Compras", (controller, root) -> {
            VerHistorialController c = (VerHistorialController) controller;
            if (c != null) {
                sigmacine.aplicacion.data.UsuarioDTO usuario = sigmacine.aplicacion.session.Session.getCurrent();
                if (usuario != null && usuario.getEmail() != null) {
                    c.setUsuarioEmail(usuario.getEmail());
                }
            }
            configurarBarraEnVista(root, "historial");
        });
    }

    public void mostrarMisBoletas() {
        loadViewWithSpinner("/sigmacine/ui/views/mis_boletas.fxml", "Sigma Cine - Mis Boletas", (controller, root) -> {
            MisBoletasController c = (MisBoletasController) controller;
            if (c != null) {
                sigmacine.aplicacion.data.UsuarioDTO usuario = sigmacine.aplicacion.session.Session.getCurrent();
                if (usuario != null && usuario.getEmail() != null) {
                    c.setUsuarioEmail(usuario.getEmail());
                }
            }
            configurarBarraEnVista(root, "misboletas");
        });
    }

    public void mostrarMiCuenta() {
        loadViewWithSpinner("/sigmacine/ui/views/mi_cuenta.fxml", "Sigma Cine - Mi Cuenta", (controller, root) -> {
            configurarBarraEnVista(root);
        });
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
        loadViewWithSpinner("/sigmacine/ui/views/pagina_inicial.fxml", "Sigma Cine - Inicio", (controller, root) -> {
            configurarBarraEnVista(root);
            
            ClienteController c = (ClienteController) controller;
            if (Session.isLoggedIn()) {
                c.init(Session.getCurrent());
            } else {
                c.init(null);
            }
            c.setCoordinador(this);
        });
    }

public void mostrarCarritoCompleto() {
        loadViewWithSpinner("/sigmacine/ui/views/verCarrito.fxml", "Sigma Cine - Carrito", (controller, root) -> {
            configurarBarraEnVista(root);
        });
    }
    
    public void mostrarConfirmacionCompra(Long compraId, String metodoPago, java.math.BigDecimal totalPagado,
                                          java.util.List<sigmacine.aplicacion.data.CompraProductoDTO> items, 
                                          java.math.BigDecimal saldoAnterior, java.math.BigDecimal saldoNuevo) {
        loadViewWithSpinner("/sigmacine/ui/views/confirmacion_compra.fxml", "Sigma Cine - Confirmaci�n de Compra", (controller, root) -> {
            configurarBarraEnVista(root);
            
            ConfirmacionCompraController c = (ConfirmacionCompraController) controller;
            c.inicializar(compraId, metodoPago, totalPagado, items, saldoAnterior, saldoNuevo);
        });
    }
}

