package sigmacine.ui;

import javafx.application.Application;
import javafx.stage.Stage;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.ScriptLoader;

import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.aplicacion.service.LoginService;
import sigmacine.aplicacion.facade.AuthFacade;
import sigmacine.ui.controller.ControladorControlador;
import sigmacine.aplicacion.service.RegistroService;

    //Son temporales


public class App extends Application {

    @Override
    public void start(Stage stage) {

        DatabaseConfig db = new DatabaseConfig();
        try (var conn = db.getConnection()) {
            ScriptLoader.runScripts(conn);
        } catch (Exception e) {
            System.err.println("No se pudieron ejecutar los scripts iniciales: " + e.getMessage());
            e.printStackTrace();
        }
        UsuarioRepository repo = new UsuarioRepositoryJdbc(db);
        LoginService loginService = new LoginService(repo);
        RegistroService registroService = new RegistroService(repo);
        AuthFacade authFacade = new AuthFacade(loginService, registroService); 


    ControladorControlador coordinador = new ControladorControlador(stage, authFacade);
    // Arrancar en la vista cliente_home y mostrar popup de selección de ciudad
    // Asegurar que el popup de ciudad pueda mostrarse (si antes quedó marcado como mostrado, lo removemos)
    try {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(sigmacine.ui.controller.ControladorControlador.class);
        if (prefs.getBoolean("cityPopupShown", false)) {
            prefs.remove("cityPopupShown");
        }
    } catch (Exception ignored) {}
    // Start as guest and show cliente home with city popup
    sigmacine.aplicacion.data.UsuarioDTO guest = new sigmacine.aplicacion.data.UsuarioDTO();
    guest.setId(0); // id 0 = invitado
    guest.setEmail("");
    guest.setNombre("Invitado");
    coordinador.mostrarClienteHomeConPopup(guest);


       /*  //Este codigo es para omitir el login
           // Usuario u;
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(6L);
            dto.setEmail("ClientePrueba@sigma.com");

            //Aca el rol se definiria para las pantallas
           // dto.setRol("ADMIN");
           dto.setRol("");

                dto.setNombre("Equipo Sigma Cliente");
                dto.setFechaRegistro("25-09-2025");
            
            ControladorControlador coordinador=new ControladorControlador(stage, authFacade);
            coordinador.mostrarHome(dto);*/
    }

    public static void main(String[] args) {
        launch(args);
    }
}
