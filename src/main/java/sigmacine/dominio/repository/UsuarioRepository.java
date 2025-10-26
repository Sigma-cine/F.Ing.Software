package sigmacine.dominio.repository;

import java.util.List;
import java.util.Optional;

import sigmacine.aplicacion.data.HistorialCompraDTO;
import sigmacine.dominio.entity.Compra;
import sigmacine.dominio.entity.Usuario;
import sigmacine.dominio.valueobject.Email;
import sigmacine.dominio.valueobject.PasswordHash;

public interface UsuarioRepository {
    //Optional<Usuario> buscarPorEmail(Email email);
    void guardar(Usuario usuario);
    int crearCliente(Email email, PasswordHash passwordHash, String nombre);
    Usuario buscarPorEmail(Email email);
    Usuario buscarPorId(int id);
    //List<Compra> verHistorial(String emailPlano);
    List<HistorialCompraDTO> verHistorial(String emailPlano);
    List<sigmacine.dominio.entity.Boleto> obtenerBoletosPorCompra(Long compraId);
    List<sigmacine.aplicacion.data.CompraProductoDTO> obtenerProductosPorCompra(Long compraId);


}
