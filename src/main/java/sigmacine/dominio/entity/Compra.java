package sigmacine.dominio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private Long id;
    private Usuario cliente;
    private List<Boleto> boletos = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private long total = 0;
    private double totalDecimal = 0.0;
    private LocalDate fecha;
    private Pago pago;

    // Constructor que inicializa una nueva compra con un cliente
    public Compra(Long id, Usuario cliente) {
        this.id = id;
        this.cliente = cliente;
    }

    public void agregarBoleto(Boleto boleto) {
        if (boleto == null) {
            throw new IllegalArgumentException("El boleto no puede ser nulo.");
        }
        this.boletos.add(boleto);
        this.total += boleto.getPrecio();
    }

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        this.productos.add(producto);
        this.total += producto.getPrecio();
    }
    
    // Método para asociar un pago a la compra
    public void setPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        this.pago = pago;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }

    public void setTotalDecimal(double totalDecimal) {
        this.totalDecimal = totalDecimal;
    }

    public double getTotalDecimal() { return totalDecimal; }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFecha() { return fecha; }
    public Long getId() {
        return id;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public long getTotal() {
        return total;
    }

    public List<Boleto> getBoletos() {
        return boletos;
    }
    
    public List<Producto> getProductos() {
        return productos;
    }
    
    public Pago getPago(){
        return pago;
    }
}