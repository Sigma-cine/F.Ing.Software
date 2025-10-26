package sigmacine.aplicacion.session;

import sigmacine.aplicacion.data.UsuarioDTO;

/**
 * Simple application session holder. Not persisted across runs.
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
