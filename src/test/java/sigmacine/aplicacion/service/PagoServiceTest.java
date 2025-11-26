        
package sigmacine.aplicacion.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sigmacine.aplicacion.data.PagoTarjetaDTO;
import sigmacine.aplicacion.data.ResultadoPagoDTO;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import sigmacine.aplicacion.service.PagoService;
import sigmacine.aplicacion.service.SigmaCardService;

class PagoServiceTest {
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
    void tarjetaFechaExpiracionNull2() {
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
    void tarjetaFechaExpiracionNull() {
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
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setCvv("123");
        dto.setCedula("12345678");
        dto.setMesExpiracion("12");
        dto.setAnioExpiracion("2030");
        dto.setTipoTarjeta("credito");
        ResultadoPagoDTO resultado = pagoService.procesarPagoTarjeta(dto, new BigDecimal("10.00"));
        assertTrue(resultado.isExitoso());
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

        @BeforeEach
        void setUp() {
            sigmaCardService = new FakeSigmaCardService();
            pagoService = new PagoService(sigmaCardService);
        }
    private PagoService pagoService;
    private FakeSigmaCardService sigmaCardService;


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
    void luhnValido() {
        assertTrue(pagoService.validarLuhn("4111 1111 1111 1111"));
    }

    @Test
    void luhnInvalido() {

        assertFalse(pagoService.validarLuhn("1234 5678 9012 3456"));
    }
}

