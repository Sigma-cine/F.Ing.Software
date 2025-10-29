package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.admi.GestionFuncionesService;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;

public class AdminController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    private final GestionPeliculasService gestionPeliculasService;
    private final GestionFuncionesService gestionFuncionesService;
    private final PeliculaRepositoryJdbc peliculaRepo;

    public AdminController(GestionPeliculasService gestionPeliculasService,
                           GestionFuncionesService gestionFuncionesService,
                           PeliculaRepositoryJdbc peliculaRepo) {
        this.gestionPeliculasService = gestionPeliculasService;
        this.gestionFuncionesService = gestionFuncionesService;
        this.peliculaRepo = peliculaRepo;
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
        if (coordinador != null) {
            coordinador.mostrarLogin();
        }
    }

    @FXML
    private void mostrarInicio() {
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
    }

    @FXML
    private void abrirAgregarPelicula() {
        try {
            var fxml = AdminController.class.getResource("/sigmacine/ui/views/Administrador/agregar_pelicula.fxml");
            if (fxml == null) throw new IllegalStateException("No se encontró agregar_pelicula.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setControllerFactory(param -> new sigmacine.ui.controller.admin.AgregarPeliculaController());
            Parent view = loader.load();
            var controller = loader.getController();
            if (controller instanceof sigmacine.ui.controller.admin.AgregarPeliculaController c) {
                c.setGestionPeliculasService(gestionPeliculasService);
            }
            if (contentArea != null) contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirGestionFunciones() {
        try {
            final String ruta = "/sigmacine/ui/views/Administrador/gestion_funciones.fxml";
            var fxml = AdminController.class.getResource(ruta);
            if (fxml == null) throw new IllegalStateException("No se encontró " + ruta);

            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setControllerFactory(param ->
                new sigmacine.ui.controller.admin.GestionFuncionesController(
                    this.gestionFuncionesService,
                    this.peliculaRepo
                )
            );

            Parent view = loader.load();
            if (contentArea != null) contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo abrir la pantalla de gestión de funciones", e);
        }
    }
}
