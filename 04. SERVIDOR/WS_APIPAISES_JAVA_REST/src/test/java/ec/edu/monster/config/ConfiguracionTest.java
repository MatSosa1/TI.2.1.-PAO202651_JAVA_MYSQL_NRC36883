package ec.edu.monster.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ConfiguracionTest {

    private static Configuracion con(Properties archivo, Map<String, String> sistema, Map<String, String> entorno) {
        return new Configuracion(archivo, sistema::get, entorno::get);
    }

    @Test
    void sinNadaConfiguradoUsaLosValoresDelTaller() {
        Configuracion configuracion = con(new Properties(), Map.of(), Map.of());

        assertEquals("root", configuracion.getDbUsuario());
        assertEquals("12345", configuracion.getDbClave());
        assertEquals("https://api.restcountries.com/countries/v5", configuracion.getApiUrl());
        assertEquals("", configuracion.getApiKey());
        assertEquals(15, configuracion.getApiTimeoutSegundos());
    }

    @Test
    void prioridadSistemaEntornoArchivo() {
        Properties archivo = new Properties();
        archivo.setProperty(Configuracion.API_KEY, "del-archivo");
        archivo.setProperty(Configuracion.DB_CLAVE, "del-archivo");
        archivo.setProperty(Configuracion.DB_USUARIO, "del-archivo");

        Configuracion configuracion = con(archivo,
                Map.of(Configuracion.API_KEY, "del-sistema"),
                Map.of("RESTCOUNTRIES_APIKEY", "del-entorno", "DB_CLAVE", "del-entorno"));

        assertEquals("del-sistema", configuracion.getApiKey());
        assertEquals("del-entorno", configuracion.getDbClave());
        assertEquals("del-archivo", configuracion.getDbUsuario());
    }

    @Test
    void valoresEnBlancoSeIgnoranYLaUrlPierdeLaBarraFinal() {
        Properties archivo = new Properties();
        archivo.setProperty(Configuracion.API_URL, "http://localhost:9999/countries/v5/");
        archivo.setProperty(Configuracion.API_TIMEOUT, "no-es-numero");

        Configuracion configuracion = con(archivo, Map.of(Configuracion.API_URL, "  "), Map.of());

        assertEquals("http://localhost:9999/countries/v5", configuracion.getApiUrl());
        assertEquals(15, configuracion.getApiTimeoutSegundos());
    }
}
