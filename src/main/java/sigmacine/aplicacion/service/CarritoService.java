package sigmacine.aplicacion.service;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import sigmacine.aplicacion.data.CompraProductoDTO;
import sigmacine.dominio.entity.Boleto;
import sigmacine.dominio.entity.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de carrito en memoria.
 * - API observable basada en DTOs para la vista (getItems, addItem, getTotal, addListener).
 * - Métodos legados (addBoleto/addProducto/getBoletos/getProductos) mantienen compatibilidad
 *   mapeando internamente a la lista observable para que la UI se actualice.
 */
public class CarritoService {
    private static final CarritoService INSTANCE = new CarritoService();

    // Lista observable usada por la UI (verCarrito.fxml)
    private final ObservableList<CompraProductoDTO> items = FXCollections.observableArrayList();

    // Listas legadas para compatibilidad con código existente
    private final List<Boleto> boletos = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();

    private CarritoService() {}

    public static CarritoService getInstance() { return INSTANCE; }

    // ---- API nueva basada en DTO ----
    public ObservableList<CompraProductoDTO> getItems() { return items; }

    public void addItem(CompraProductoDTO item) {
        if (item == null) return;
        items.add(item);
    }

    public void removeItem(CompraProductoDTO item) { if (item != null) items.remove(item); }

    public void clear() {
        items.clear();
        synchronized (this) { boletos.clear(); productos.clear(); }
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void addListener(ListChangeListener<? super CompraProductoDTO> l) { items.addListener(l); }

    // ---- API LEGADA: mantiene compatibilidad y alimenta la lista observable ----
    public synchronized void addBoleto(Boleto b) {
        if (b == null) throw new IllegalArgumentException("Boleto nulo");
        boletos.add(b);
        // Mapear a DTO visible para el overlay (precio en unidades monetarias)
        BigDecimal precio = BigDecimal.valueOf(b.getPrecio()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String nombre = "Boleto: " + safe(b.getPelicula()) + (b.getHorario() != null ? (" — " + b.getHorario()) : "");
        items.add(new CompraProductoDTO(null, nombre, 1, precio));
    }

    public synchronized void addProducto(Producto p) {
        if (p == null) throw new IllegalArgumentException("Producto nulo");
        productos.add(p);
        BigDecimal precio = BigDecimal.valueOf(p.getPrecio()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        items.add(new CompraProductoDTO(p.getId(), p.getNombre(), 1, precio));
    }

    public synchronized List<Boleto> getBoletos() { return Collections.unmodifiableList(new ArrayList<>(boletos)); }

    public synchronized List<Producto> getProductos() { return Collections.unmodifiableList(new ArrayList<>(productos)); }

    private static String safe(String s) { return s != null ? s : ""; }
}
