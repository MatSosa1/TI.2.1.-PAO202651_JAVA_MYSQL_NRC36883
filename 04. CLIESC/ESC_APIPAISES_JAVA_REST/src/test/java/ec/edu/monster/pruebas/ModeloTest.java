package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.Formatos;
import ec.edu.monster.modelo.Sesion;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Configuración, sesión, formatos y estado del servidor. */
class ModeloTest {

    @Test
    void configuracionDelClasspathApuntaAlServidorLocal() {
        assertEquals("http://localhost:8080/WS_APIPAISES_JAVA_REST/api", Configuracion.desdeClasspath().getUrlBase());
    }

    @Test
    void elArchivoJuntoAlJarTienePrioridad(@TempDir Path carpeta) throws IOException {
        Files.writeString(carpeta.resolve(Configuracion.ARCHIVO), "ws.host=192.168.1.20\nws.port=9090\nws.context=APP\n");
        assertEquals("http://192.168.1.20:9090/APP/api", Configuracion.cargar(carpeta).getUrlBase());
    }

    @Test
    void configuracionIncompletaOConPuertoInvalido() {
        Properties sinHost = new Properties();
        sinHost.setProperty("ws.port", "8080");
        sinHost.setProperty("ws.context", "X");
        assertThrows(IllegalStateException.class, () -> new Configuracion(sinHost).getUrlBase());

        Properties puerto = new Properties();
        puerto.setProperty("ws.host", "localhost");
        puerto.setProperty("ws.port", "ocho");
        puerto.setProperty("ws.context", "X");
        assertThrows(IllegalStateException.class, () -> new Configuracion(puerto).getUrlBase());
    }

    @Test
    void sesionIniciaYCierra() {
        Sesion sesion = new Sesion();
        assertFalse(sesion.estaActiva());

        sesion.iniciar("admin", new DatosSesion("tok", "Administrador"));
        assertTrue(sesion.estaActiva());
        assertEquals("Administrador", sesion.getNombre());

        sesion.cerrar();
        assertFalse(sesion.estaActiva());
        assertNull(sesion.getToken());
    }

    @Test
    void formatos() {
        assertEquals("18.001.000", Formatos.poblacion(18_001_000));
        assertEquals("1 país encontrado", Formatos.total(1));
        assertEquals("3 países encontrados", Formatos.total(3));
        assertEquals("", Formatos.region("Todas"));
        assertEquals("Europa", Formatos.region("Europa"));
        assertEquals(Formatos.INGRESE_NOMBRE_PAIS, Formatos.errorNombre(" "));
        assertNull(Formatos.errorNombre("x".repeat(100)));
        assertEquals(6, Formatos.REGIONES.size());
    }

    @Test
    void servidorApagadoNoEstaDisponible() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }
        assertFalse(new EstadoServidor(URI.create("http://127.0.0.1:" + puertoLibre + "/api/Paises")).disponible());
    }
}
