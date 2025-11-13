package sigmacine.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.Connection;

import sigmacine.infraestructura.configDataBase.DatabaseConfig;
import sigmacine.infraestructura.persistencia.jdbc.UsuarioRepositoryJdbc;
import sigmacine.infraestructura.configDataBase.ScriptLoader;

import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.aplicacion.service.LoginService;
import sigmacine.aplicacion.facade.AuthFacade;
import sigmacine.ui.controller.ControladorControlador;
import sigmacine.aplicacion.service.RegistroService;
import sigmacine.infraestructura.persistencia.jdbc.PeliculaRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.CompraRepositoryJdbc;
import sigmacine.aplicacion.service.CompraService;
import sigmacine.aplicacion.service.CarritoService;
import javafx.util.Callback;
import sigmacine.dominio.repository.admi.FuncionAdminRepository;
// Inyecciones de administrador
import sigmacine.dominio.repository.admi.PeliculaAdminRepository;
import sigmacine.dominio.repository.admi.ProductoAdminRepository;
import sigmacine.infraestructura.persistencia.jdbc.admi.FuncionAdminRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.admi.PeliculaAdminRepositoryJdbc;
import sigmacine.infraestructura.persistencia.jdbc.admi.ProductoAdminRepositoryJdbc;
import sigmacine.aplicacion.service.admi.GestionFuncionesService;
import sigmacine.aplicacion.service.admi.GestionPeliculasService;
import sigmacine.aplicacion.service.admi.GestionProductosService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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

        Map<Class<?>, Object> container = new HashMap<>();
        container.put(sigmacine.infraestructura.configDataBase.DatabaseConfig.class, db);
        container.put(sigmacine.dominio.repository.UsuarioRepository.class, repo);
        container.put(sigmacine.aplicacion.service.LoginService.class, loginService);
        container.put(sigmacine.aplicacion.service.RegistroService.class, registroService);
        container.put(sigmacine.aplicacion.facade.AuthFacade.class, authFacade);


        PeliculaRepositoryJdbc peliculaRepo = new PeliculaRepositoryJdbc(db);
        container.put(PeliculaRepositoryJdbc.class, peliculaRepo);

        CompraRepositoryJdbc compraRepo = new CompraRepositoryJdbc(db);
        container.put(CompraRepositoryJdbc.class, compraRepo);

        CompraService compraService = new CompraService(compraRepo);
        container.put(CompraService.class, compraService);

        container.put(CarritoService.class, CarritoService.getInstance());

        PeliculaAdminRepository adminRepo = new PeliculaAdminRepositoryJdbc(db);
        container.put(PeliculaAdminRepository.class, adminRepo);

        GestionPeliculasService gestionPeliculasService = new GestionPeliculasService(peliculaRepo, adminRepo);
        container.put(GestionPeliculasService.class, gestionPeliculasService);

        FuncionAdminRepository funcAdminRepo = new FuncionAdminRepositoryJdbc(db);
        container.put(FuncionAdminRepository.class, funcAdminRepo);

        GestionFuncionesService gestionFuncionesService = new GestionFuncionesService(funcAdminRepo);
        container.put(GestionFuncionesService.class, gestionFuncionesService);

        ProductoAdminRepository prodAdminRepo = new ProductoAdminRepositoryJdbc(db);
        container.put(ProductoAdminRepository.class, prodAdminRepo);

        GestionProductosService gestionProductosService = new GestionProductosService(prodAdminRepo);
        container.put(GestionProductosService.class, gestionProductosService);

    /*    var peliculaRepo = new PeliculaRepositoryJdbc(db);
        container.put(PeliculaRepositoryJdbc.class, peliculaRepo);
        var compraRepo = new CompraRepositoryJdbc(db);
        container.put(CompraRepositoryJdbc.class, compraRepo);
        var compraService = new CompraService(compraRepo);
        container.put(CompraService.class, compraService);
        container.put(CarritoService.class, CarritoService.getInstance());*/

        Callback<Class<?>, Object> controllerFactory = (Class<?> clazz) -> {
            try {
                for (Constructor<?> ctor : clazz.getConstructors()) {
                    Class<?>[] pts = ctor.getParameterTypes();
                    Object[] args = new Object[pts.length];
                    boolean ok = true;
                    for (int i = 0; i < pts.length; i++) {
                        Object candidate = null;
                        for (Map.Entry<Class<?>, Object> e : container.entrySet()) {
                            if (pts[i].isAssignableFrom(e.getKey())) {
                                candidate = e.getValue();
                                break;
                            }

                        }
                        if (candidate == null) { 
                            ok = false; 
                            break; 
                        }
                        args[i] = candidate;
                    }
                    if (ok) {
                        return ctor.newInstance(args);
                    }
                }
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("No se pudo instanciar controlador: " + clazz.getName(), ex);
            }
        };


        coordinador.setControllerFactory(controllerFactory);
        
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(sigmacine.ui.controller.ControladorControlador.class);
            if (prefs.getBoolean("cityPopupShown", false)) {
                prefs.remove("cityPopupShown");
            }
        } catch (Exception ignored) {}
        
        sigmacine.aplicacion.data.UsuarioDTO guest = new sigmacine.aplicacion.data.UsuarioDTO();
        guest.setId(0);
        guest.setEmail("");
        guest.setNombre("Invitado");
        coordinador.mostrarClienteHomeConPopup(guest);
    }

    public static void main(String[] args) {
        launch(args);
    }
}