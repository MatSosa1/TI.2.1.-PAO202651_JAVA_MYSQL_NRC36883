package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dobles.TransporteFalso;
import ec.edu.monster.modelo.ClienteRest;
import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.ServiciosRest;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Servicios REST contra un transporte falso: URL, método, encabezados, cuerpo y lectura del JSON. */
class ServiciosRestTest {

    private static final String BASE = "http://servidor:8080/WS_APIPAISES_JAVA_REST/api";
    private static final String TOKEN = "a".repeat(64);
    private static final String ECUADOR = "{\"nombre\":\"Ecuador\",\"capital\":\"Quito\",\"region\":\"América\","
            + "\"poblacion\":18001000,\"moneda\":\"United States dollar (USD)\"}";

    private final TransporteFalso red = new TransporteFalso();
    private final ServiciosRest servicios = new ServiciosRest(new ClienteRest(BASE + "/", red));

    private void responder(int codigo, String estado, String mensaje, String datos) {
        red.codigo = codigo;
        red.respuesta = "{\"datos\":" + datos + ",\"estado\":\"" + estado + "\",\"mensaje\":\"" + mensaje + "\"}";
    }

    @Test
    void loginSinTokenYConCuerpoJson() throws ServicioException {
        responder(200, "OK", "Ingreso de sesión exitoso", "{\"token\":\"" + TOKEN + "\",\"usuario\":\"Administrador\"}");

        DatosSesion datos = servicios.iniciarSesion("admin", "admin123");

        assertEquals("POST", red.metodo);
        assertEquals(BASE + "/Login/login", red.url);
        assertEquals("{\"usuario\":\"admin\",\"clave\":\"admin123\"}".length(), red.cuerpo.length());
        assertTrue(red.cuerpo.contains("\"usuario\":\"admin\""));
        assertFalse(red.cabeceras.containsKey("Authorization"));
        assertEquals(TOKEN, datos.token());
        assertEquals("Administrador", datos.usuario());
    }

    @Test
    void credencialesIncorrectas401NoEsSesionNoValida() {
        responder(401, "ERROR", "Credenciales incorrectas", "null");

        ServicioException error = assertThrows(ServicioException.class,
                () -> servicios.iniciarSesion("admin", "mala"));
        assertEquals("Credenciales incorrectas", error.getMessage());
        assertFalse(error.isSesionNoValida());
    }

    @Test
    void conTokenUn401EsSesionNoValida() {
        responder(401, "ERROR", "Token inválido o sesión no activa", "null");

        ServicioException error = assertThrows(ServicioException.class, () -> servicios.listar(TOKEN));
        assertTrue(error.isSesionNoValida());
        assertEquals(ServicioException.SESION_NO_VALIDA, error.getMessage());
        assertEquals(TOKEN, red.cabeceras.get("Authorization"), "el token va sin prefijo");
    }

    @Test
    void listarLeeLosPaises() throws ServicioException {
        responder(200, "OK", "Países obtenidos correctamente", "[" + ECUADOR + "]");

        Resultado<List<Pais>> resultado = servicios.listar(TOKEN);

        assertEquals("GET", red.metodo);
        assertEquals(BASE + "/Paises", red.url);
        assertNull(red.cuerpo);
        assertEquals(18_001_000L, resultado.datos().get(0).poblacion());
        assertFalse(resultado.datos().get(0).guardado());
    }

    @Test
    void buscarCodificaLosParametros() throws ServicioException {
        responder(200, "OK", "Búsqueda realizada correctamente", "{\"paises\":[" + ECUADOR + "],\"total\":1}");

        Resultado<ResultadoBusqueda> resultado = servicios.buscar(TOKEN, "san josé", null, "América");

        assertEquals(BASE + "/Paises/buscar?nombre=san%20jos%C3%A9&region=Am%C3%A9rica", red.url);
        assertEquals(1, resultado.datos().total());
    }

    @Test
    void rutasConNombreYMetodos() throws ServicioException {
        responder(200, "OK", "ok", "[" + ECUADOR + "]");
        servicios.consultarApi(TOKEN, "Perú");
        assertEquals(BASE + "/Paises/externo/Per%C3%BA", red.url);

        responder(200, "OK", "ok", ECUADOR);
        servicios.actualizar(TOKEN, "Países Bajos");
        assertEquals("PUT", red.metodo);
        assertEquals(BASE + "/Paises/Pa%C3%ADses%20Bajos", red.url);

        servicios.eliminar(TOKEN, "Narnia");
        assertEquals("DELETE", red.metodo);

        servicios.registrar(TOKEN, "germany", "Alemania");
        assertEquals("POST", red.metodo);
        assertEquals(BASE + "/Paises", red.url);
        assertTrue(red.cuerpo.contains("\"consulta\":\"germany\""));
        assertEquals("application/json; charset=utf-8", red.cabeceras.get("Content-Type"));

        servicios.importarTodos(TOKEN);
        assertEquals(BASE + "/Paises/importar/todos", red.url);
        assertEquals(ClienteRest.ESPERA_IMPORTACION, red.espera);
    }

    @Test
    void estadoErrorLanzaElMensajeDelServidor() {
        responder(409, "ERROR", "Ya existe un país con ese nombre", "null");
        ServicioException error = assertThrows(ServicioException.class,
                () -> servicios.registrar(TOKEN, "ecuador", "Ecuador"));
        assertEquals("Ya existe un país con ese nombre", error.getMessage());

        // Informes sin países: ERROR con HTTP 200 (diagrama de secuencia)
        responder(200, "ERROR", "No hay países registrados para generar informes", "null");
        error = assertThrows(ServicioException.class, () -> servicios.generar(TOKEN));
        assertEquals("No hay países registrados para generar informes", error.getMessage());
    }

    @Test
    void informeConLasTresSecciones() throws ServicioException {
        responder(200, "OK", "Informes generados correctamente", "{\"paisMasPoblado\":" + ECUADOR
                + ",\"paisesPorRegion\":[{\"cantidad\":1,\"region\":\"América\"}],"
                + "\"promedioPorRegion\":[{\"promedioPoblacion\":18001000,\"region\":\"América\"}]}");

        Informe informe = servicios.generar(TOKEN).datos();

        assertEquals("Ecuador", informe.paisMasPoblado().nombre());
        assertEquals(1, informe.paisesPorRegion().get(0).cantidad());
        assertEquals(18_001_000L, informe.promedioPorRegion().get(0).promedioPoblacion());
    }

    @Test
    void sinRedORespuestaIlegible() {
        red.sinRed = true;
        assertEquals(ServicioException.SIN_SERVIDOR,
                assertThrows(ServicioException.class, () -> servicios.listar(TOKEN)).getMessage());

        red.sinRed = false;
        for (String cuerpo : List.of("<html>404</html>", "", "{}")) {
            red.codigo = 404;
            red.respuesta = cuerpo;
            assertEquals(ServicioException.RESPUESTA_INCOMPLETA,
                    assertThrows(ServicioException.class, () -> servicios.listar(TOKEN)).getMessage(), cuerpo);
        }
    }
}
