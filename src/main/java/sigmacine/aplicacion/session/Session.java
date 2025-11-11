package sigmacine.aplicacion.session;

import sigmacine.aplicacion.data.UsuarioDTO;

/**
 * Acá se guarda la sesión actual del usuario. Es durante toda la ejecución de la aplicación. 
 * Ejemplo: cuando sale el nombre del usuario en la barra superior.
 */
public final class Session {
    private static UsuarioDTO current;
    private static String selectedCity;

    private Session() {}

    public static UsuarioDTO getCurrent() { return current; }
    public static boolean isLoggedIn() { return current != null; }
    public static void setCurrent(UsuarioDTO u) { current = u; }
    public static void clear() { current = null; }

    public static String getSelectedCity() { return selectedCity; }
    public static void setSelectedCity(String city) { selectedCity = city; }
}
