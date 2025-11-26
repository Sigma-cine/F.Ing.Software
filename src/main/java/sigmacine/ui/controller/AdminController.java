    
package sigmacine.ui.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.admi.GestionFuncionesService;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.aplicacion.service.admi.GestionProductosService;
import sigmacine.aplicacion.session.Session;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.ui.controller.admin.AgregarItemController;
import sigmacine.ui.controller.admin.AgregarPeliculaController;
import sigmacine.ui.controller.admin.GestionFuncionesController;

import java.net.URL;

public class AdminController {
    @FXML
    private ImageView bienvenidaAdminImg;

    @FXML
    private Label welcomeLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    private ProgressIndicator progressSpinner;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    private final GestionPeliculasService gestionPeliculasService;
    private final GestionProductosService gestionProductosService;
    private final GestionFuncionesService gestionFuncionesService;
    private final PeliculaRepositoryJdbc peliculaRepo;

    public AdminController(GestionPeliculasService gestionPeliculasService,
                           GestionFuncionesService gestionFuncionesService,
                           PeliculaRepositoryJdbc peliculaRepo,
                           GestionProductosService productosService) {
        this.gestionPeliculasService = gestionPeliculasService;
        this.gestionFuncionesService = gestionFuncionesService;
        this.peliculaRepo = peliculaRepo;
        this.gestionProductosService = productosService;
    }

    @FXML
    public void initialize() {
        // Asignar imagen de bienvenida admin y hacerla grande
        if (bienvenidaAdminImg != null) {
            try {
                var url = getClass().getResource("/Images/Bienvenida-admin.png");
                if (url != null) {
                    bienvenidaAdminImg.setImage(new Image(url.toExternalForm()));
                    bienvenidaAdminImg.setPreserveRatio(true);
                    bienvenidaAdminImg.setFitWidth(900);
                    bienvenidaAdminImg.setFitHeight(600);
                } else {
                    System.err.println("No se encontró la imagen de bienvenida admin en /Images/Bienvenida-admin.png");
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen de bienvenida admin: " + e.getMessage());
            }
        }
    }

    public void init(UsuarioDTO usuario, ControladorControlador coordinador) {
        this.usuario = usuario;
        this.coordinador = coordinador;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenido al Cine Sigma");
        }
        mostrarInicio();
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Session.setSelectedCity(null);
        usuario = null;
        if (coordinador != null) {
            coordinador.mostrarPaginaInicial();
        }
    }

    @FXML
    private void mostrarInicio() {
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
        hideLoading();
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
            loadingOverlay.toFront();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        }
    }

    @FXML
    private void abrirAgregarPelicula() {
        showLoading();

        Task<Parent> task = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                String ruta = "/sigmacine/ui/views/Administrador/agregar_pelicula.fxml";
                URL fxml = AdminController.class.getResource(ruta);
                if (fxml == null) {
                    throw new IllegalStateException("No se encontró " + ruta);
                }

                FXMLLoader loader = new FXMLLoader(fxml);
                loader.setControllerFactory(param -> new AgregarPeliculaController());
                Parent view = loader.load();

                Object controller = loader.getController();
                if (controller instanceof AgregarPeliculaController) {
                    AgregarPeliculaController c = (AgregarPeliculaController) controller;
                    c.setGestionPeliculasService(gestionPeliculasService);
                }

                return view;
            }
        };

        task.setOnSucceeded(event -> {
            hideLoading();
            Parent view = task.getValue();
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        });

        task.setOnFailed(event -> {
            hideLoading();
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void abrirGestionFunciones() {
        showLoading();

        Task<Parent> task = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                String ruta = "/sigmacine/ui/views/Administrador/gestion_funciones.fxml";
                URL fxml = AdminController.class.getResource(ruta);
                        // Asignar imagen de bienvenida admin
                        if (bienvenidaAdminImg != null) {
                            try {
                                bienvenidaAdminImg.setImage(new Image(getClass().getResource("/Images/Bienvenida-admin.png").toExternalForm()));
                            } catch (Exception e) {
                                System.err.println("No se pudo cargar la imagen de bienvenida admin: " + e.getMessage());
                            }
                        }
                if (fxml == null) {
                    throw new IllegalStateException("No se encontró " + ruta);
                }

                FXMLLoader loader = new FXMLLoader(fxml);
                loader.setControllerFactory(param ->
                        new GestionFuncionesController(
                                gestionFuncionesService,
                                peliculaRepo
                        )
                );
                Parent view = loader.load();
                return view;
            }
        };

        task.setOnSucceeded(event -> {
            hideLoading();
            Parent view = task.getValue();
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        });

        task.setOnFailed(event -> {
            hideLoading();
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void abrirGestionProductos() {
        showLoading();

        Task<Parent> task = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                String ruta = "/sigmacine/ui/views/Administrador/AgregarItem.fxml";
                URL fxml = AdminController.class.getResource(ruta);
                if (fxml == null) {
                    throw new IllegalStateException("No se encontró " + ruta);
                }

                FXMLLoader loader = new FXMLLoader(fxml);
                loader.setControllerFactory(param ->
                        new AgregarItemController(
                                gestionProductosService
                        )
                );
                Parent view = loader.load();
                return view;
            }
        };

        task.setOnSucceeded(event -> {
            hideLoading();
            Parent view = task.getValue();
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        });

        task.setOnFailed(event -> {
            hideLoading();
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}
