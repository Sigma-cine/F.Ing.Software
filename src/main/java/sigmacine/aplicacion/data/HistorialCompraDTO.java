package sigmacine.aplicacion.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class HistorialCompraDTO {
    private Long compraId;
    private LocalDate compraFecha;   // fecha de la compra
    private BigDecimal total;        // total exacto en moneda
    private String sedeCiudad;       // null si no hubo boletos
    private LocalDate funcionFecha;  // null si no hubo boletos
    private LocalTime funcionHora;   // null si no hubo boletos
    private int cantBoletos;
    private int cantProductos;

    public HistorialCompraDTO(Long compraId,
                              LocalDate compraFecha,
                              BigDecimal total,
                              String sedeCiudad,
                              LocalDate funcionFecha,
                              LocalTime funcionHora,
                              int cantBoletos,
                              int cantProductos) {
        this.compraId = compraId;
        this.compraFecha = compraFecha;
        this.total = total;
        this.sedeCiudad = sedeCiudad;
        this.funcionFecha = funcionFecha;
        this.funcionHora = funcionHora;
        this.cantBoletos = cantBoletos;
        this.cantProductos = cantProductos;
    }

    public Long getCompraId() { return compraId; }
    public LocalDate getCompraFecha() { return compraFecha; }
    public BigDecimal getTotal() { return total; }
    public String getSedeCiudad() { return sedeCiudad; }
    public LocalDate getFuncionFecha() { return funcionFecha; }
    public LocalTime getFuncionHora() { return funcionHora; }
    public int getCantBoletos() { return cantBoletos; }
    public int getCantProductos() { return cantProductos; }

    // opcional: toString para debug
    @Override public String toString() {
        return "HistorialCompraDTO{id=%d, fecha=%s, total=%s, sede=%s, func=%s %s, boletos=%d, productos=%d}"
                .formatted(compraId, compraFecha, total, sedeCiudad,
                           funcionFecha, funcionHora, cantBoletos, cantProductos);
    }
}
