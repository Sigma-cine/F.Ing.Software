package sigmacine.aplicacion.service;

import sigmacine.aplicacion.data.UsuarioDTO;
import sigmacine.dominio.repository.UsuarioRepository;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;

public class LoginService {

    private final UsuarioRepository usuarios;

    public LoginService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public UsuarioDTO autenticar(String emailRaw, String passwordRaw) {
        try {
            Email email = new Email(emailRaw);

            Usuario u = usuarios.buscarPorEmail(email);
            if (u == null) return null;

            if (!u.autenticar(passwordRaw)) return null;

            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail().value());
            dto.setRol(u.getRol().name());  
            dto.setNombre(u.getNombre());

            if (u.getFechaRegistro() != null) {
                dto.setFechaRegistro(u.getFechaRegistro().toString());
            } else {
                dto.setFechaRegistro(null);
            }

            return dto;

        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

