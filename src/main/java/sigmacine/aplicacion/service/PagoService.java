package sigmacine.aplicacion.service;

import sigmacine.aplicacion.data.PagoTarjetaDTO;
import sigmacine.aplicacion.data.ResultadoPagoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

/**
 * Servicio para procesar diferentes métodos de pago
 */
public class PagoService {

    private final SigmaCardService sigmaCardService;
    private final Random random;

    public PagoService(SigmaCardService sigmaCardService) {
        this(sigmaCardService, new Random());
    }

    public PagoService(SigmaCardService sigmaCardService, Random random) {
        this.sigmaCardService = sigmaCardService;
        this.random = random;
    }

    /**
     * Método protegido para simular sleep, sobreescribible en tests
     */
    protected void doSleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    /**
     * Procesa un pago con SigmaCard
     */
    public ResultadoPagoDTO procesarPagoSigmaCard(long usuarioId, BigDecimal monto) {
        try {
            // 1. Verificar que el usuario tenga SigmaCard
            if (!sigmaCardService.tieneCard(usuarioId)) {
                return new ResultadoPagoDTO(false, "No tienes una SigmaCard registrada. Por favor, registra tu tarjeta primero.");
            }

            // 2. Verificar que tenga saldo suficiente
            BigDecimal saldoActual = sigmaCardService.consultarSaldo(String.valueOf(usuarioId));
            if (saldoActual.compareTo(monto) < 0) {
                return new ResultadoPagoDTO(false, 
                    String.format("Saldo insuficiente. Saldo actual: %s, Monto requerido: %s", 
                        sigmaCardService.format(saldoActual), 
                        sigmaCardService.format(monto)));
            }

            // 3. Debitar el monto de la SigmaCard
            boolean debitado = debitarSigmaCard(usuarioId, monto);
            if (!debitado) {
                return new ResultadoPagoDTO(false, "No se pudo realizar el débito de la SigmaCard. Por favor, intenta nuevamente.");
            }

            // 4. Generar código de transacción
            String codigoTransaccion = "SC-" + System.currentTimeMillis();
            BigDecimal nuevoSaldo = sigmaCardService.consultarSaldo(String.valueOf(usuarioId));

            return new ResultadoPagoDTO(true, 
                String.format("Pago exitoso con SigmaCard. Nuevo saldo: %s", sigmaCardService.format(nuevoSaldo)), 
                codigoTransaccion);

        } catch (Exception e) {
            return new ResultadoPagoDTO(false, "Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Procesa un pago con tarjeta de crédito/débito (Pasarela falsa)
     */
    public ResultadoPagoDTO procesarPagoTarjeta(PagoTarjetaDTO datosTarjeta, BigDecimal monto) {
        try {
            // Validaciones
            String errorValidacion = validarDatosTarjeta(datosTarjeta);
            if (errorValidacion != null) {
                return new ResultadoPagoDTO(false, errorValidacion);
            }

            // Simular procesamiento de pasarela de pago
            // En un sistema real, aquí se comunicaría con la pasarela de pago real
            doSleep(1000 + random.nextInt(2000)); // Simular tiempo de procesamiento

            // Simular aprobación aleatoria (90% de éxito)
            boolean aprobado = random.nextInt(100) < 90;

            if (aprobado) {
                String codigoTransaccion = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String tipoTarjetaTexto = datosTarjeta.getTipoTarjeta().equalsIgnoreCase("credito") ? "Crédito" : "Débito";
                return new ResultadoPagoDTO(true, 
                    String.format("Pago aprobado con tarjeta de %s terminada en %s", 
                        tipoTarjetaTexto, 
                        datosTarjeta.getNumeroTarjeta().substring(Math.max(0, datosTarjeta.getNumeroTarjeta().length() - 4))), 
                    codigoTransaccion);
            } else {
                return new ResultadoPagoDTO(false, "Pago rechazado por el banco. Por favor, verifica los datos e intenta nuevamente.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultadoPagoDTO(false, "Error en el procesamiento: " + e.getMessage());
        } catch (Exception e) {
            return new ResultadoPagoDTO(false, "Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Valida los datos de la tarjeta
     */
    private String validarDatosTarjeta(PagoTarjetaDTO datos) {
        if (datos == null) {
            return "Datos de tarjeta requeridos";
        }

        if (datos.getNumeroTarjeta() == null || datos.getNumeroTarjeta().trim().isEmpty()) {
            return "Número de tarjeta requerido";
        }

        // Validar longitud del número de tarjeta (entre 13 y 19 dígitos)
        String numeroLimpio = datos.getNumeroTarjeta().replaceAll("\\s+", "");
        if (numeroLimpio.length() < 13 || numeroLimpio.length() > 19) {
            return "Número de tarjeta inválido";
        }

        if (datos.getNombreTitular() == null || datos.getNombreTitular().trim().isEmpty()) {
            return "Nombre del titular requerido";
        }

        if (datos.getCvv() == null || datos.getCvv().trim().isEmpty()) {
            return "CVV requerido";
        }

        if (datos.getCvv().length() < 3 || datos.getCvv().length() > 4) {
            return "CVV inválido (debe tener 3 o 4 dígitos)";
        }

        if (datos.getCedula() == null || datos.getCedula().trim().isEmpty()) {
            return "Cédula requerida";
        }

        // Validar fecha de expiración
        if (datos.getMesExpiracion() == null || datos.getAnioExpiracion() == null) {
            return "Fecha de expiración requerida";
        }

        try {
            int mes = Integer.parseInt(datos.getMesExpiracion());
            int anio = Integer.parseInt(datos.getAnioExpiracion());
            
            if (mes < 1 || mes > 12) {
                return "Mes de expiración inválido";
            }

            LocalDate ahora = LocalDate.now();
            int anioActual = ahora.getYear();
            int mesActual = ahora.getMonthValue();

            if (anio < anioActual || (anio == anioActual && mes < mesActual)) {
                return "La tarjeta está vencida";
            }

        } catch (NumberFormatException e) {
            return "Fecha de expiración inválida";
        }

        return null; // Validación exitosa
    }

    /**
     * Débita el monto de la SigmaCard del usuario
     */
    private boolean debitarSigmaCard(long usuarioId, BigDecimal monto) {
        try {
            BigDecimal saldoActual = sigmaCardService.consultarSaldo(String.valueOf(usuarioId));
            BigDecimal nuevoSaldo = saldoActual.subtract(monto);
            
            // Recargar con valor negativo para efectuar el débito
            sigmaCardService.recargar(String.valueOf(usuarioId), monto.negate());
            return true;
        } catch (Exception e) {
            System.err.println("Error al debitar SigmaCard: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida el formato de Luhn para números de tarjeta
     * (Algoritmo estándar de la industria para validar números de tarjeta)
     */
    public boolean validarLuhn(String numeroTarjeta) {
        String numero = numeroTarjeta.replaceAll("\\s+", "");
        
        if (!numero.matches("\\d+")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        
        for (int i = numero.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numero.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
}
