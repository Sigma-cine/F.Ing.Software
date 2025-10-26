package sigmacine.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;        
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;     
import javafx.stage.Stage;       
import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.ui.controller.admin.AgregarPeliculaController;


public class AdminController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;

    private UsuarioDTO usuario;
    private ControladorControlador coordinador;

    // Servicio inyectado por constructor (lo provee tu controllerFactory en App.start)
    private final GestionPeliculasService gestionPeliculasService;

    public AdminController(GestionPeliculasService gestionPeliculasService) {
        this.gestionPeliculasService = gestionPeliculasService;
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
            java.net.URL fxml = AdminController.class.getResource(
                "/sigmacine/ui/views/Administrador/agregar_pelicula.fxml"
            );
            if (fxml == null) {
                throw new IllegalStateException("No se encontró agregar_pelicula.fxml en el classpath.");
             }
             System.out.println("Cargados FXML:" + fxml);
            FXMLLoader loader = new FXMLLoader(fxml);
            //Opcioanl
            loader.setControllerFactory(param -> new AgregarPeliculaController());

            Parent view = loader.load();

            AgregarPeliculaController controller = loader.getController();
            controller.setGestionPeliculasService(gestionPeliculasService);

            // Cargar dentro del centro del dashboard
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Si más adelante agregas otros módulos, crea métodos similares:
    // @FXML private void abrirGestorFunciones() { ... set contentArea ... }
    // @FXML private void abrirGestorProductos() { ... set contentArea ... }
    // @FXML private void abrirReportes()        { ... set contentArea ... }
}
