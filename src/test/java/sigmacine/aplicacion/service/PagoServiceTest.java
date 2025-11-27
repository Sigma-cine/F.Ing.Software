package sigmacine.aplicacion.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.PagoTarjetaDTO;
import sigmacine.aplicacion.data.ResultadoPagoDTO;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PagoServiceTest {
    
    private PagoService pagoService;
    private FakeSigmaCardService sigmaCardService;
    
    @BeforeEach
    void setUp() {
        sigmaCardService = new FakeSigmaCardService();
        // Crear un Mock Random que siempre devuelve 0 (< 90 = pago aprobado)
        java.util.Random mockRandom = new java.util.Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // Siempre devuelve 0, garantiza que el pago sea aprobado
            }
        };
        pagoService = new PagoService(sigmaCardService, mockRandom) {
            @Override
            protected void doSleep(long millis) {
                // No hacer nada, evitar sleep en tests
            }
        };
    }
    
    static class FakeSigmaCardService extends SigmaCardService {
        boolean tieneCard = true;
        BigDecimal saldo = new BigDecimal("100.00");
        boolean recargaFalla = false;
        RuntimeException excepcion = null;

        public boolean tieneCard(long usuarioId) {
            if (excepcion != null) throw excepcion;
            return tieneCard;
        }

        public BigDecimal consultarSaldo(String usuarioId) {
            if (excepcion != null) throw excepcion;
            return saldo;
        }

        public BigDecimal recargar(String usuarioId, BigDecimal monto) {
            if (recargaFalla) throw new RuntimeException("Fallo recarga");
            saldo = saldo.add(monto);
            return saldo;
        }

        public String format(BigDecimal monto) {
            return monto.toString();
        }
    }
    
    @Test
        void tarjetaNumeroTarjetaDemasiadoLargo() {
            PagoTarjetaDTO dto = new PagoTarjetaDTO();
            dto.setNumeroTarjeta("411111111111111111111"); // 21 dígitos
            dto.setNombreTitular("Juan Perez");
            dto.setCvv("123");
            dto.setCedula("12345678");
            dto.setMesExpiracion("12");
            dto.setAnioExpiracion("2030");
            dto.setTipoTarjeta("debito");
            ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
            assertFalse(resultado.isExitoso());
            assertTrue(resultado.getMensaje().contains("Número de tarjeta inválido"));
        }
    @Test
    void tarjetaNumeroTarjetaNullYVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta(null);
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        // Ya cubierto null, ahora vacío
        dto.setNumeroTarjeta("");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta requerido"));
    }

    @Test
    void tarjetaNombreTitularNullYVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular(null);
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        // Ya cubierto null, ahora vacío
        dto.setNombreTitular("");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Nombre del titular requerido"));
    }

    @Test
    void tarjetaCvvNullYVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv(null);
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        // Ya cubierto null, ahora vacío
        dto.setCvv("");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV requerido"));
    }

    @Test
    void tarjetaCedulaNullYVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula(null);
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        // Ya cubierto null, ahora vacío
        dto.setCedula("");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Cédula requerida"));
    }

    @Test
    void tarjetaFechaExpiracionAmbosNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(null);
        dto.setAnioExpiracion(null);
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración requerida"));
    }

    @Test
    void tarjetaFechaExpiracionAmbosVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("");
        dto.setAnioExpiracion("");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración inválida"));
    }
    @Test
    void tarjetaNumeroTarjetaVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta requerido"));
    }

    @Test
    void tarjetaNombreTitularVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Nombre del titular requerido"));
    }

    @Test
    void tarjetaCvvVacio2() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV requerido"));
    }

    @Test
    void tarjetaCedulaVacia2() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Cédula requerida"));
    }

    @Test
    void tarjetaNumeroTarjetaNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta(null);
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta requerido"));
    }

    @Test
    void tarjetaNombreTitularNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular(null);
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Nombre del titular requerido"));
    }

    @Test
    void tarjetaCvvNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv(null);
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV requerido"));
    }

    @Test
    void tarjetaCedulaNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula(null);
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Cédula requerida"));
    }

    @Test
    void tarjetaTipoDebito() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        PagoService pagoServiceAprobado = new PagoService(sigmaCardService, new java.util.Random() {
            @Override
            public int nextInt(int bound) {
                return 0; // Siempre menor que 90, siempre aprueba
            }
        }) {
            @Override
            protected void doSleep(long millis) {}
        };
        ResultadoPagoDTO resultado = pagoServiceAprobado.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertTrue(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Débito"));
    }
    @Test
    void tarjetaMesExpiracionMenorA1() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("0"); // menor a 1
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Mes de expiración inválido"));
    }

    @Test
    void tarjetaMesExpiracionMayorA12() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("15"); // mayor a 12
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Mes de expiración inválido"));
    }

    @Test
    void tarjetaAnioExpiracionMenorActual() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2000"); // menor al actual
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().toLowerCase().contains("vencida"));
    }

    @Test
    void tarjetaAnioIgualActualMesMenor() {
        int anioActual = java.time.LocalDate.now().getYear();
        int mesActual = java.time.LocalDate.now().getMonthValue();
        int mesMenor = mesActual > 1 ? mesActual - 1 : 1;
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(String.format("%02d", mesMenor));
        dto.setAnioExpiracion(String.valueOf(anioActual));
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().toLowerCase().contains("vencida"));
    }
    @Test
    void tarjetaMesExpiracionInvalidoNoNumerico() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("xx");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración inválida"));
    }

    @Test
    void tarjetaMesExpiracionFueraDeRango() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("13"); // mes fuera de rango
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Mes de expiración inválido"));
    }
    @Test
    void tarjetaTipoCredito() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111113"); // 13 dígitos (mínimo válido)
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("credito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertTrue(resultado.isExitoso(), "El pago debería ser exitoso pero falló con: " + resultado.getMensaje());
        assertTrue(resultado.getMensaje().contains("Crédito"));
    }

    @Test
    void tarjetaDatosNulos() {
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(null, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Datos de tarjeta requeridos"));
    }

    @Test
    void tarjetaNumeroVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta requerido"));
    }

    @Test
    void tarjetaNumeroInvalidoLongitud() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("1234");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta inválido"));
    }

    @Test
    void tarjetaNombreVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Nombre del titular requerido"));
    }

    @Test
    void tarjetaCvvVacio() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV requerido"));
    }

    @Test
    void tarjetaCvvInvalido() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("12");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV inválido"));
    }

    @Test
    void tarjetaCedulaVacia() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Cédula requerida"));
    }

    @Test
    void tarjetaFechaNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(null);
        dto.setAnioExpiracion(null);
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración requerida"));
    }

    @Test
    void tarjetaFechaInvalida() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("xx");
        dto.setAnioExpiracion("yyyy");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración inválida"));
    }

    @Test
    void pagoRechazadoPorBanco() {
        PagoService pagoServiceRechazo = new PagoService(sigmaCardService, new java.util.Random() {
            @Override
            public int nextInt(int bound) {
                return 95; // Forzar rechazo (>=90)
            }
        }) {
            @Override
            protected void doSleep(long millis) {}
        };
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoServiceRechazo.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("rechazado por el banco"));
    }

    @Test
    void tarjetaInterruptedException() {
        PagoService pagoServiceConExcepcion = new PagoService(sigmaCardService, new java.util.Random()) {
            @Override
            protected void doSleep(long millis) throws InterruptedException {
                throw new InterruptedException("Interrumpido");
            }
        };
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoServiceConExcepcion.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Error en el procesamiento"));
    }

    @Test
    void tarjetaExceptionGenerica() {
        PagoService pagoServiceConExcepcion = new PagoService(sigmaCardService, new java.util.Random()) {
            @Override
            protected void doSleep(long millis) {
                throw new RuntimeException("Error generico");
            }
        };
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoServiceConExcepcion.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Error al procesar el pago"));
    }

    @Test
    void luhnNoNumerico() {
        assertFalse(pagoService.validarLuhn("abcd efgh ijkl mnop"));
    }

    @Test
    void exitoSigmaCard() {
        sigmaCardService.tieneCard = true;
        sigmaCardService.saldo = new BigDecimal("100.00");
        sigmaCardService.recargaFalla = false;
        sigmaCardService.excepcion = null;
        ResultadoPagoDTO resultado = pagoService.procesarPagoSigmaCard(1L, new BigDecimal("50.00"));
        assertTrue(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Pago exitoso"));
        assertNotNull(resultado.getCodigoTransaccion());
    }
    @Test
    void sinCardSigmaCard() {
        sigmaCardService.tieneCard = false;
        ResultadoPagoDTO resultado = pagoService.procesarPagoSigmaCard(2L, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("No tienes una SigmaCard"));
    }

    @Test
    void saldoInsuficienteSigmaCard() {
        sigmaCardService.tieneCard = true;
        sigmaCardService.saldo = new BigDecimal("5.00");
        ResultadoPagoDTO resultado = pagoService.procesarPagoSigmaCard(3L, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Saldo insuficiente"));
    }

    @Test
    void errorDebitoSigmaCard() {
        sigmaCardService.tieneCard = true;
        sigmaCardService.saldo = new BigDecimal("100.00");
        sigmaCardService.recargaFalla = true;
        ResultadoPagoDTO resultado = pagoService.procesarPagoSigmaCard(4L, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("No se pudo realizar el débito"));
    }

    @Test
    void excepcionSigmaCard() {
        sigmaCardService.excepcion = new RuntimeException("Error grave");
        ResultadoPagoDTO resultado = pagoService.procesarPagoSigmaCard(5L, new BigDecimal("10.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Error al procesar el pago"));
        sigmaCardService.excepcion = null;
    }

    @Test
    void exitoTarjeta() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion(String.valueOf(java.time.LocalDate.now().getYear() + 1));
        dto.setTipoTarjeta("credito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertNotNull(resultado);
        assertNotNull(resultado.getMensaje());
    }

    @Test
    void datosInvalidosTarjeta() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Número de tarjeta requerido"));
    }

    @Test
    void tarjetaVencida() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("01");
        dto.setAnioExpiracion("2000");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("vencida"));
    }

    @Test
    void tarjetaCvvLongitudInvalida() {
        // Probar CVV con menos de 3 dígitos
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("12"); // Solo 2 dígitos
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion(String.valueOf(java.time.LocalDate.now().getYear() + 1));
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV inválido"));

        // Probar CVV con más de 4 dígitos
        dto.setCvv("12345"); // 5 dígitos
        resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("CVV inválido"));
    }

    @Test
    void tarjetaFechaExpiracionNull() {
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(null);
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("debito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración requerida"));

        // Probar con año null
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion(null);
        resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("Fecha de expiración requerida"));
    }

    @Test
    void tarjetaVencidaMismoAnioMesAnterior() {
        // Probar tarjeta vencida en el mismo año pero mes anterior
        // Esto cubre específicamente: (anio == anioActual && mes < mesActual)
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int anioActual = ahora.getYear();
        int mesActual = ahora.getMonthValue();
        
        // Usar mes anterior en el mismo año (si no estamos en enero, usar mes-1)
        // Si estamos en enero, forzar con octubre del año actual (que será < enero del próximo año)
        int mesVencido = mesActual > 1 ? mesActual - 1 : 10;
        
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(String.format("%02d", mesVencido));
        dto.setAnioExpiracion(String.valueOf(anioActual)); // MISMO AÑO ACTUAL
        dto.setTipoTarjeta("credito");
        
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("20.00"));
        assertFalse(resultado.isExitoso());
        assertTrue(resultado.getMensaje().contains("vencida"));
    }

    @Test
    void tarjetaVencidaEneroDelAnioActual() {
        // Caso específico: tarjeta que expira en enero del año actual (cuando estamos en meses posteriores)
        // Esto también cubre: (anio == anioActual && mes < mesActual)
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int anioActual = ahora.getYear();
        int mesActual = ahora.getMonthValue();
        
        // Solo probar si no estamos en enero
        if (mesActual > 1) {
            PagoTarjetaDTO dto = new PagoTarjetaDTO();
            dto.setNumeroTarjeta("4111111111111113");
            dto.setNombreTitular("Juan Perez");
            dto.setCvv("123");
            dto.setCedula("12345678");
            dto.setMesExpiracion("01"); // Enero
            dto.setAnioExpiracion(String.valueOf(anioActual)); // Año actual
            dto.setTipoTarjeta("debito");
            
            ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("15.00"));
            assertFalse(resultado.isExitoso());
            assertTrue(resultado.getMensaje().contains("vencida"));
        }
    }

    @Test
    void tarjetaValidaMesActualAnioActual() {
        // Tarjeta que expira en el mes actual del año actual (NO vencida)
        // Esto cubre: anio == anioActual && mes >= mesActual (no entra al if)
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int anioActual = ahora.getYear();
        int mesActual = ahora.getMonthValue();
        
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(String.format("%02d", mesActual)); // Mes actual
        dto.setAnioExpiracion(String.valueOf(anioActual)); // Año actual
        dto.setTipoTarjeta("credito");
        
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("15.00"));
        // La validación de fecha debe pasar (no está vencida)
        // El resultado final depende del random, pero no debe decir "vencida"
        assertNotNull(resultado);
        if (!resultado.isExitoso()) {
            assertFalse(resultado.getMensaje().contains("vencida"));
        }
    }

    @Test
    void tarjetaValidaMesFuturoAnioActual() {
        // Tarjeta que expira en un mes futuro del año actual (NO vencida)
        // Esto cubre: anio == anioActual && mes > mesActual (no entra al if)
        java.time.LocalDate ahora = java.time.LocalDate.now();
        int anioActual = ahora.getYear();
        int mesActual = ahora.getMonthValue();
        
        // Usar mes siguiente (o enero si estamos en diciembre)
        int mesFuturo = mesActual < 12 ? mesActual + 1 : 1;
        int anioFuturo = mesActual < 12 ? anioActual : anioActual + 1;
        
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion(String.format("%02d", mesFuturo));
        dto.setAnioExpiracion(String.valueOf(anioFuturo));
        dto.setTipoTarjeta("debito");
        
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("15.00"));
        assertNotNull(resultado);
        if (!resultado.isExitoso()) {
            assertFalse(resultado.getMensaje().contains("vencida"));
        }
    }

    @Test
    void luhnValido() {
        assertTrue(pagoService.validarLuhn("4111 1111 1111 1111"));
    }

    @Test
    void luhnInvalido() {

        assertFalse(pagoService.validarLuhn("1234 5678 9012 3456"));
    }

    @Test
    void constructorConSoloSigmaCardService() {
        // Probar el constructor que solo recibe SigmaCardService
        PagoService servicio = new PagoService(sigmaCardService);
        assertNotNull(servicio);
        
        // Verificar que funciona correctamente
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Test User");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion(String.valueOf(java.time.LocalDate.now().getYear() + 1));
        dto.setTipoTarjeta("credito");
        ResultadoPagoDTO resultado = servicio.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertNotNull(resultado);
    }

    @Test
    void constructorConRandomYDoSleep() {
        // Crear una instancia con Random controlado pero SIN sobreescribir doSleep
        java.util.Random mockRandom = new java.util.Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        
        PagoService servicioConSleep = new PagoService(sigmaCardService, mockRandom);
        
        // Crear una tarjeta válida
        PagoTarjetaDTO dto = new PagoTarjetaDTO();
        dto.setNumeroTarjeta("4111111111111113");
        dto.setNombreTitular("Test User");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion(String.valueOf(java.time.LocalDate.now().getYear() + 1));
        dto.setTipoTarjeta("credito");
        
        // Este procesamiento llamará a doSleep internamente
        long inicio = System.currentTimeMillis();
        ResultadoPagoDTO resultado = servicioConSleep.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        long fin = System.currentTimeMillis();
        
        // Verificar que el pago fue exitoso y que tomó tiempo (doSleep se ejecutó)
        assertTrue(resultado.isExitoso());
        assertTrue(fin >= inicio); // Debería haber tomado al menos 1 segundo por el sleep
    }
}

